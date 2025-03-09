package com.uf.assistance.config.ai;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class SpringAIConfig {
    @Value("${openai.api.key:#{null}}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4}")
    private String openAiModel;

    @Value("${openai.max.tokens:4096}")
    private Integer openAiMaxTokens;

    /**
     * OpenAI API 인스턴스 생성
     */
    @Bean
    public OpenAiApi openAiApi() {
        if (!StringUtils.hasText(openAiApiKey)) {
            throw new IllegalStateException("OpenAI API 키가 설정되지 않았습니다. 환경 변수 OPENAI_API_KEY 또는 openai.api.key 속성을 설정하세요.");
        }

        return OpenAiApi.builder()
                .apiKey(openAiApiKey)
                .build();
    }

    /**
     * OpenAI 기본 옵션 생성
     */
    @Bean
    public OpenAiChatOptions openAiChatOptions() {
        return OpenAiChatOptions.builder()
                .model(openAiModel)
                .maxTokens(openAiMaxTokens)
                .temperature(0.7)
                .build();
    }

    /**
     * ChatModel 빈 생성
     */
    @Bean
    public ChatModel chatModel(OpenAiApi openAiApi, OpenAiChatOptions openAiChatOptions) {
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(openAiChatOptions)
                .build();
    }
}
