package com.uf.assistance.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * REST API 호출을 위한 웹 클라이언트 설정
 */
@Configuration
public class WebClientConfig {

    /**
     * RestTemplate 빈 등록
     * API 호출 시 사용할 HTTP 클라이언트
     *
     * @param builder RestTemplateBuilder
     * @return 구성된 RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * 더 복잡한 HTTP 클라이언트 설정이 필요한 경우 Apache HttpClient를 사용한 팩토리 메서드
     * 이 메서드는 현재 사용되지 않지만, 향후 필요시 활성화 가능
     */
    /*
    @Bean
    public HttpComponentsClientHttpRequestFactory httpRequestFactory() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectionRequestTimeout((int) Duration.ofSeconds(10).toMillis());
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        return requestFactory;
    }
    */
}