package com.uf.assistance.service.impl;

import com.uf.assistance.dto.ai.OpenAIRequest;
import com.uf.assistance.dto.ai.OpenAIResponse;
import com.uf.assistance.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


import java.util.*;

/**
 * OpenAI API를 사용한 AI 서비스 구현
 */
@Service
public class OpenAIService implements AIService {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.model:gpt-4}")
    private String model;

    @Value("${openai.max.tokens:4096}")
    private int maxTokens;

    public OpenAIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String generateResponse(String prompt, String context) {
        try {
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 메시지 목록 생성
            List<OpenAIRequest.Message> messages = new ArrayList<>();

            // 시스템 메시지 (컨텍스트)
            if (StringUtils.hasText(context)) {
                messages.add(new OpenAIRequest.Message("system", context));
            }

            // 사용자 메시지 (프롬프트)
            messages.add(new OpenAIRequest.Message("user", prompt));

            // 요청 객체 생성
            OpenAIRequest.ChatCompletion requestBody = OpenAIRequest.ChatCompletion.builder()
                    .model(model)
                    .messages(messages)
                    .maxTokens(maxTokens)
                    .temperature(0.7)
                    .build();

            // API 요청 생성
            HttpEntity<OpenAIRequest.ChatCompletion> requestEntity =
                    new HttpEntity<>(requestBody, headers);

            // API 호출
            OpenAIResponse.ChatCompletion response = restTemplate.postForObject(
                    apiUrl,
                    requestEntity,
                    OpenAIResponse.ChatCompletion.class
            );

            // 응답 처리
            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }

            return "응답을 생성할 수 없습니다.";
        } catch (Exception e) {
            logger.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            return "AI 서비스 오류: " + e.getMessage();
        }
    }

    @Override
    public boolean isAvailable() {
        return StringUtils.hasText(apiKey);
    }

    @Override
    public String getProviderName() {
        return "OpenAI";
    }

    @Override
    public int getMaxTokens() {
        return maxTokens;
    }
}