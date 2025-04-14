package com.uf.assistance.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.service.KeywordGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

// OpenAI를 사용한 구현체
@Service
public class OpenAiKeywordGenerationService implements KeywordGenerationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Autowired
    public OpenAiKeywordGenerationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> generateMiddleKeywords(List<String> keywords1, List<String> keywords2) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4");

            // 프롬프트 구성
            StringBuilder prompt = new StringBuilder();
            prompt.append("다음 두 그룹의 키워드 사이에 있는 중간 연결 키워드를 5개 제안해주세요.\n\n");
            prompt.append("첫 번째 그룹: ").append(String.join(", ", keywords1)).append("\n");
            prompt.append("두 번째 그룹: ").append(String.join(", ", keywords2)).append("\n\n");
            prompt.append("각 키워드는 쉼표로 구분된 하나의 단어나 짧은 구문으로 반환해주세요. 형식: keyword1, keyword2, keyword3, keyword4, keyword5");

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "당신은 텍스트 분석과 키워드 추출 전문가입니다."));
            messages.add(Map.of("role", "user", "content", prompt.toString()));

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.3);
            requestBody.put("max_tokens", 100);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl,
                    entity,
                    String.class
            );

            JsonNode responseNode = objectMapper.readTree(response.getBody());
            String content = responseNode.get("choices").get(0).get("message").get("content").asText();

            // 응답에서 키워드 추출
            List<String> extractedKeywords = Arrays.stream(content.split(","))
                    .map(String::trim)
                    .filter(keyword -> !keyword.isEmpty())
                    .collect(Collectors.toList());

            return extractedKeywords;
        } catch (Exception e) {
            logger.error("키워드 생성 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}