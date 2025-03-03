package com.uf.assistance.config.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 환경 설정에 쉽게 접근하기 위한 헬퍼 클래스
 */
@Component
public class EnvHelper {

    private static final Logger logger = LoggerFactory.getLogger(EnvHelper.class);

    private final Environment environment;
    private final EnvPropertySource propertySource;

    // 타입별 값 캐싱 (성능 최적화)
    private final Map<String, Object> valueCache = new ConcurrentHashMap<>();

    @Autowired
    public EnvHelper(Environment environment, EnvPropertySource propertySource) {
        this.environment = environment;
        this.propertySource = propertySource;
    }

    /**
     * 문자열 값 가져오기
     */
    public String getString(String settingKey) {
        return environment.getProperty(settingKey);
    }

    /**
     * 문자열 값 가져오기 (기본값 지원)
     */
    public String getString(String settingKey, String defaultSettingValue) {
        return environment.getProperty(settingKey, defaultSettingValue);
    }

    /**
     * 정수 값 가져오기
     */
    public Integer getInteger(String settingKey) {
        return getInteger(settingKey, null);
    }

    /**
     * 정수 값 가져오기 (기본값 지원)
     */
    public Integer getInteger(String settingKey, Integer defaultSettingValue) {
        String cacheKey = "int:" + settingKey;
        if (valueCache.containsKey(cacheKey)) {
            return (Integer) valueCache.get(cacheKey);
        }

        Integer settingValue = environment.getProperty(settingKey, Integer.class, defaultSettingValue);
        if (settingValue != null) {
            valueCache.put(cacheKey, settingValue);
        }
        return settingValue;
    }

    /**
     * Long 값 가져오기
     */
    public Long getLong(String settingKey) {
        return getLong(settingKey, null);
    }

    /**
     * Long 값 가져오기 (기본값 지원)
     */
    public Long getLong(String settingKey, Long defaultSettingValue) {
        String cacheKey = "long:" + settingKey;
        if (valueCache.containsKey(cacheKey)) {
            return (Long) valueCache.get(cacheKey);
        }

        Long settingValue = environment.getProperty(settingKey, Long.class, defaultSettingValue);
        if (settingValue != null) {
            valueCache.put(cacheKey, settingValue);
        }
        return settingValue;
    }

    /**
     * Double 값 가져오기
     */
    public Double getDouble(String settingKey) {
        return getDouble(settingKey, null);
    }

    /**
     * Double 값 가져오기 (기본값 지원)
     */
    public Double getDouble(String settingKey, Double defaultSettingValue) {
        String cacheKey = "double:" + settingKey;
        if (valueCache.containsKey(cacheKey)) {
            return (Double) valueCache.get(cacheKey);
        }

        Double settingValue = environment.getProperty(settingKey, Double.class, defaultSettingValue);
        if (settingValue != null) {
            valueCache.put(cacheKey, settingValue);
        }
        return settingValue;
    }

    /**
     * Boolean 값 가져오기
     */
    public Boolean getBoolean(String settingKey) {
        return getBoolean(settingKey, null);
    }

    /**
     * Boolean 값 가져오기 (기본값 지원)
     */
    public Boolean getBoolean(String settingKey, Boolean defaultSettingValue) {
        String cacheKey = "bool:" + settingKey;
        if (valueCache.containsKey(cacheKey)) {
            return (Boolean) valueCache.get(cacheKey);
        }

        Boolean settingValue = environment.getProperty(settingKey, Boolean.class, defaultSettingValue);
        if (settingValue != null) {
            valueCache.put(cacheKey, settingValue);
        }
        return settingValue;
    }

    /**
     * 문자열 목록 가져오기 (쉼표로 구분)
     */
    public List<String> getList(String settingKey) {
        return getList(settingKey, ",");
    }

    /**
     * 문자열 목록 가져오기 (구분자 지정)
     */
    public List<String> getList(String settingKey, String delimiter) {
        String cacheKey = "list:" + settingKey + ":" + delimiter;
        if (valueCache.containsKey(cacheKey)) {
            return (List<String>) valueCache.get(cacheKey);
        }

        String settingValue = environment.getProperty(settingKey);
        if (settingValue == null || settingValue.trim().isEmpty()) {
            return List.of();
        }

        List<String> result = Arrays.stream(settingValue.split(delimiter))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        valueCache.put(cacheKey, result);
        return result;
    }

    /**
     * 특정 접두사로 시작하는 모든 설정 조회
     */
    public Map<String, String> getPropertiesByPrefix(String prefix) {
        // 접두사로 시작하는 모든 속성 이름 조회
        Set<String> settingKeys = propertySource.getPropertyNamesByPrefix(prefix);

        // 속성 이름과 값을 맵으로 변환
        Map<String, String> result = new HashMap<>();
        for (String settingKey : settingKeys) {
            String settingValue = getString(settingKey);
            if (settingValue != null) {
                result.put(settingKey, settingValue);
            }
        }

        return result;
    }

    /**
     * 일치하는 패턴을 가진 모든 설정 조회
     */
    public Map<String, String> getPropertiesByPattern(String pattern) {
        // 패턴과 일치하는 모든 속성 이름 조회
        Set<String> settingKeys = propertySource.getPropertyNamesByPattern(pattern);

        // 속성 이름과 값을 맵으로 변환
        Map<String, String> result = new HashMap<>();
        for (String settingKey : settingKeys) {
            String settingValue = getString(settingKey);
            if (settingValue != null) {
                result.put(settingKey, settingValue);
            }
        }

        return result;
    }

    /**
     * Duration 값 가져오기
     */
    public Duration getDuration(String settingKey) {
        return getDuration(settingKey, null);
    }

    /**
     * Duration 값 가져오기 (기본값 지원)
     */
    public Duration getDuration(String settingKey, Duration defaultSettingValue) {
        String cacheKey = "duration:" + settingKey;
        if (valueCache.containsKey(cacheKey)) {
            return (Duration) valueCache.get(cacheKey);
        }

        try {
            String settingValue = environment.getProperty(settingKey);
            if (settingValue == null) {
                return defaultSettingValue;
            }

            Duration duration = parseDuration(settingValue);
            valueCache.put(cacheKey, duration);
            return duration;
        } catch (Exception e) {
            logger.warn("Duration 값 '{}' 파싱 중 오류 발생: {}", settingKey, e.getMessage());
            return defaultSettingValue;
        }
    }

    /**
     * Duration 문자열 파싱
     */
    private Duration parseDuration(String settingValue) {
        if (settingValue == null || settingValue.trim().isEmpty()) {
            return Duration.ZERO;
        }

        settingValue = settingValue.trim().toLowerCase();

        // 숫자만 있는 경우 밀리초로 간주
        if (settingValue.matches("\\d+")) {
            return Duration.ofMillis(Long.parseLong(settingValue));
        }

        // Java Duration 형식 (PT1H 등)
        if (settingValue.startsWith("pt")) {
            return Duration.parse(settingValue);
        }

        // 단위 지정 형식 지원
        if (settingValue.endsWith("ms")) {
            return Duration.ofMillis(Long.parseLong(settingValue.substring(0, settingValue.length() - 2)));
        } else if (settingValue.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(settingValue.substring(0, settingValue.length() - 1)));
        } else if (settingValue.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(settingValue.substring(0, settingValue.length() - 1)));
        } else if (settingValue.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(settingValue.substring(0, settingValue.length() - 1)));
        } else if (settingValue.endsWith("d")) {
            return Duration.ofDays(Long.parseLong(settingValue.substring(0, settingValue.length() - 1)));
        }

        // 기타 형식은 ISO-8601로 시도
        return Duration.parse(settingValue);
    }

    /**
     * 활성 프로파일 가져오기
     */
    public String[] getActiveProfiles() {
        return environment.getActiveProfiles();
    }

    /**
     * 특정 프로파일이 활성화되어 있는지 확인
     */
    public boolean isProfileActive(String profile) {
        if (profile == null || profile.isEmpty()) {
            return false;
        }
        return Arrays.asList(environment.getActiveProfiles()).contains(profile);
    }

    /**
     * 설정 값이 존재하는지 확인
     */
    public boolean containsProperty(String settingKey) {
        return environment.containsProperty(settingKey);
    }

    /**
     * 값 캐시 초기화
     */
    public void clearCache() {
        valueCache.clear();
        logger.debug("환경 헬퍼 캐시가 초기화되었습니다.");
    }
}