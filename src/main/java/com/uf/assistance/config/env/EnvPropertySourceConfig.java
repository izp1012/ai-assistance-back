package com.uf.assistance.config.env;

import com.uf.assistance.service.EnvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.HashMap;
import java.util.Map;

/**
 * 환경 설정 속성 소스 구성 및 관리
 */
@Configuration
@EnableScheduling
public class EnvPropertySourceConfig {

    private static final Logger logger = LoggerFactory.getLogger(EnvPropertySourceConfig.class);

    private final EnvService envService;
    private final ConfigurableEnvironment environment;

    private EnvPropertySource propertySource;

    @Value("${env.refresh.enabled:true}")
    private boolean refreshEnabled;

    @Value("${env.refresh.interval:3600000}")  // 기본값 1시간
    private long refreshInterval;

    @Autowired
    public EnvPropertySourceConfig(EnvService envService, ConfigurableEnvironment environment) {
        this.envService = envService;
        this.environment = environment;
    }

    /**
     * EnvPropertySource 빈 등록 및 환경에 추가
     */
    @Bean
    public EnvPropertySource envPropertySource() {
        propertySource = new EnvPropertySource(envService);

        // 환경에 추가
        MutablePropertySources propertySources = environment.getPropertySources();

        // 이미 존재하면 교체, 아니면 추가
        if (propertySources.contains(propertySource.getName())) {
            propertySources.replace(propertySource.getName(), propertySource);
        } else {
            // 시스템 속성 다음에 위치하도록 추가
            propertySources.addAfter("systemProperties", propertySource);
        }

        logger.info("환경 설정 PropertySource 등록됨 ({}개 속성)",
                propertySource.getPropertyNames().length);

        return propertySource;
    }

    /**
     * 정기적으로 설정 새로고침
     */
    @Scheduled(fixedDelayString = "${env.refresh.interval:3600000}")
    public void scheduledRefresh() {
        if (!refreshEnabled || propertySource == null) {
            return;
        }

        try {
            long startTime = System.currentTimeMillis();
            int count = propertySource.refreshProperties();
            long elapsed = System.currentTimeMillis() - startTime;

            logger.debug("스케줄링된 설정 새로고침 완료: {}개 속성 ({}ms 소요)", count, elapsed);
        } catch (Exception e) {
            logger.error("스케줄링된 설정 새로고침 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 설정 변경 이벤트 수신 시 해당 설정만 갱신
     */
    @Async
    @EventListener
    public void handleEnvChangeEvent(EnvChangeEvent event) {
        if (propertySource == null) {
            return;
        }

        try {
            String settingKey = event.getSettingKey();
            boolean success = propertySource.refreshProperty(settingKey);

            if (success) {
                logger.debug("이벤트에 의해 설정 '{}' 갱신됨", settingKey);
            }
        } catch (Exception e) {
            logger.error("설정 변경 이벤트 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 모든 설정 강제 새로고침
     * @return 새로고침 결과 정보
     */
    public Map<String, Object> forceRefresh() {
        Map<String, Object> result = new HashMap<>();

        if (propertySource == null) {
            result.put("success", false);
            result.put("message", "PropertySource가 초기화되지 않았습니다");
            return result;
        }

        try {
            long startTime = System.currentTimeMillis();
            int count = propertySource.refreshProperties();
            long elapsed = System.currentTimeMillis() - startTime;

            result.put("success", true);
            result.put("count", count);
            result.put("elapsed", elapsed);
            result.put("timestamp", System.currentTimeMillis());

            logger.info("강제 설정 새로고침 완료: {}개 속성 ({}ms 소요)", count, elapsed);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "새로고침 중 오류 발생: " + e.getMessage());
            logger.error("강제 설정 새로고침 중 오류 발생: {}", e.getMessage(), e);
        }

        return result;
    }
}