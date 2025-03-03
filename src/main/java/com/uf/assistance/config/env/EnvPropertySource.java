package com.uf.assistance.config.env;

import com.uf.assistance.service.EnvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 데이터베이스에서 환경 설정을 읽어 Spring Environment에 제공하는 PropertySource
 */
public class EnvPropertySource extends EnumerablePropertySource<EnvService> {

    private static final Logger logger = LoggerFactory.getLogger(EnvPropertySource.class);

    // 캐시된 속성 (동시성 지원)
    private final Map<String, String> propertiesCache = new ConcurrentHashMap<>();

    // 정규화된 이름 매핑 (OPENAI_API_KEY -> openai.api.key)
    private final Map<String, String> normalizedKeyMapping = new ConcurrentHashMap<>();

    // 마지막 새로고침 시간
    private long lastRefreshed = 0;

    // 새로고침 중인지 여부 (동시 갱신 방지)
    private volatile boolean refreshing = false;

    /**
     * 생성자
     * @param envService 환경 설정 서비스
     */
    public EnvPropertySource(EnvService envService) {
        super("envPropertySource", envService);
        refreshProperties();
    }

    /**
     * 데이터베이스에서 모든 설정 다시 로드
     * @return 로드된 속성 수
     */
    public synchronized int refreshProperties() {
        if (refreshing) {
            logger.debug("이미 새로고침 중입니다. 중복 호출 무시됨.");
            return 0;
        }

        refreshing = true;

        try {
            long startTime = System.currentTimeMillis();

            // 임시 맵에 속성 로드
            Map<String, String> newProperties = new HashMap<>();
            Map<String, String> newNormalizedMapping = new HashMap<>();

            // 모든 설정 조회
            List<EnvEntry> settings = getSource().getAllSettings();

            // 설정 값을 캐시에 저장
            for (EnvEntry setting : settings) {
                String settingKey = setting.getSettingKey();
                String settingValue = setting.getSettingValue();

                if (settingValue != null) {
                    // 원래 키로 저장
                    newProperties.put(settingKey, settingValue);

                    // 정규화된 키로도 저장
                    String normalizedKey = normalizeKey(settingKey);
                    if (!settingKey.equals(normalizedKey)) {
                        newProperties.put(normalizedKey, settingValue);
                        newNormalizedMapping.put(normalizedKey, settingKey);
                    }
                }
            }

            // 기존 캐시 교체 (atomic 연산)
            int propertyCount = newProperties.size();
            propertiesCache.clear();
            propertiesCache.putAll(newProperties);

            normalizedKeyMapping.clear();
            normalizedKeyMapping.putAll(newNormalizedMapping);

            lastRefreshed = System.currentTimeMillis();
            long elapsed = lastRefreshed - startTime;

            logger.debug("데이터베이스에서 {}개의 설정을 로드했습니다. ({}ms 소요)", propertyCount, elapsed);
            return propertyCount;
        } catch (Exception e) {
            logger.error("설정 로드 중 오류 발생: {}", e.getMessage(), e);
            return 0;
        } finally {
            refreshing = false;
        }
    }

    /**
     * 특정 설정 키 갱신
     * @param settingKey 설정 키
     * @return 갱신 성공 여부
     */
    public boolean refreshProperty(String settingKey) {
        try {
            String settingValue = getSource().getSettingValueByKey(settingKey);

            if (settingValue != null) {
                // 원래 키로 저장
                propertiesCache.put(settingKey, settingValue);

                // 정규화된 키로도 저장
                String normalizedKey = normalizeKey(settingKey);
                if (!settingKey.equals(normalizedKey)) {
                    propertiesCache.put(normalizedKey, settingValue);
                    normalizedKeyMapping.put(normalizedKey, settingKey);
                }

                logger.debug("설정 '{}' 갱신됨: {}", settingKey, maskSensitiveValue(settingKey, settingValue));
                return true;
            } else {
                // 값이 null이면 캐시에서 제거
                propertiesCache.remove(settingKey);

                String normalizedKey = normalizeKey(settingKey);
                if (!settingKey.equals(normalizedKey)) {
                    propertiesCache.remove(normalizedKey);
                    normalizedKeyMapping.remove(normalizedKey);
                }

                logger.debug("설정 '{}' 제거됨", settingKey);
                return true;
            }
        } catch (Exception e) {
            logger.error("설정 '{}' 갱신 중 오류 발생: {}", settingKey, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 설정 키 정규화 (표준 형식으로 변환)
     * 예: OPENAI_API_KEY -> openai.api.key
     */
    private String normalizeKey(String settingKey) {
        if (!StringUtils.hasText(settingKey)) {
            return settingKey;
        }

        // 대문자와 언더스코어를 소문자와 점으로 변환
        return settingKey.toLowerCase().replace('_', '.');
    }

    /**
     * 민감한 정보 마스킹 처리
     */
    private String maskSensitiveValue(String settingKey, String settingValue) {
        if (settingValue == null) {
            return null;
        }

        // 민감한 정보가 포함된 키인지 확인
        String lowerKey = settingKey.toLowerCase();
        if (lowerKey.contains("key") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("password") ||
                lowerKey.contains("token")) {

            return "*****";
        }

        return settingValue;
    }

    /**
     * 속성값 조회
     */
    @Override
    public Object getProperty(String propertyName) {
        // 캐시에서 값 조회
        String settingValue = propertiesCache.get(propertyName);

        // 캐시에 없는 경우 원본 키 찾기 시도
        if (settingValue == null && normalizedKeyMapping.containsKey(propertyName)) {
            String originalKey = normalizedKeyMapping.get(propertyName);
            settingValue = propertiesCache.get(originalKey);
        }

        return settingValue;
    }

    /**
     * 모든 속성 이름 조회
     */
    @Override
    public String[] getPropertyNames() {
        return propertiesCache.keySet().toArray(new String[0]);
    }

    /**
     * 특정 접두사로 시작하는 모든 속성 이름 조회 (그룹 구분에 사용)
     */
    public Set<String> getPropertyNamesByPrefix(String prefix) {
        String normalizedPrefix = prefix.toLowerCase();

        return propertiesCache.keySet().stream()
                .filter(propertyName -> propertyName.toLowerCase().startsWith(normalizedPrefix))
                .collect(Collectors.toSet());
    }

    /**
     * 특정 패턴과 일치하는 모든 속성 이름 조회
     */
    public Set<String> getPropertyNamesByPattern(String pattern) {
        return propertiesCache.keySet().stream()
                .filter(propertyName -> propertyName.matches(pattern))
                .collect(Collectors.toSet());
    }

    /**
     * 마지막 새로고침 시간 조회
     */
    public long getLastRefreshed() {
        return lastRefreshed;
    }

    /**
     * 속성 추가 또는 업데이트
     */
    public void updateProperty(String settingKey, String settingValue, String description, String updatedBy) {
        // 데이터베이스에 저장
        EnvEntry entry = new EnvEntry(settingKey, settingValue, description, updatedBy);
        getSource().saveSetting(entry);

        // 캐시 업데이트
        refreshProperty(settingKey);
    }

    /**
     * 속성 일괄 업데이트
     */
    public void updateProperties(Map<String, EnvEntry> entries) {
        // 데이터베이스에 일괄 저장
        getSource().saveAllSettings(entries.values().stream().collect(Collectors.toList()));

        // 캐시 새로고침
        refreshProperties();
    }

    /**
     * 속성 삭제
     */
    public void removeProperty(String settingKey) {
        // 데이터베이스에서 삭제
        getSource().deleteSetting(settingKey);

        // 캐시에서 제거
        propertiesCache.remove(settingKey);

        String normalizedKey = normalizeKey(settingKey);
        if (!settingKey.equals(normalizedKey)) {
            propertiesCache.remove(normalizedKey);
            normalizedKeyMapping.remove(normalizedKey);
        }

        logger.debug("설정 '{}' 삭제됨", settingKey);
    }
}