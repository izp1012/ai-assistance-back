package com.uf.assistance.service.impl;

import com.uf.assistance.domain.ai.AI;
import com.uf.assistance.domain.ai.AIRepository;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.ai.AIReqDto;
import com.uf.assistance.dto.ai.AIRespDto;
import com.uf.assistance.service.AIService;
import com.uf.assistance.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI API를 사용한 AI 서비스 구현 (Spring AI 적용)
 */
@Service
public class SpringAIOpenAIService implements AIService {
    private static final Logger logger = LoggerFactory.getLogger(SpringAIOpenAIService.class);

    private final AIRepository aiRepository;
    private final ChatModel chatModel;
    private final OpenAiChatOptions openAiChatOptions;
    private final String apiKey;
    private final int maxTokens;

    public SpringAIOpenAIService(
            AIRepository aiRepository,
            ChatModel chatModel,
            OpenAiChatOptions openAiChatOptions,
            @Value("${spring.ai.openai.api-key:#{null}}") String apiKey,
            @Value("${openai.max.tokens:4096}") int maxTokens) {
        this.aiRepository = aiRepository;
        this.chatModel = chatModel;
        this.openAiChatOptions = openAiChatOptions;
        this.apiKey = apiKey;
        this.maxTokens = maxTokens;
    }

    @Override
    public String generateResponse(String prompt, String context) {
        try {
            // Spring AI 프롬프트 구성
            List<Message> messages = new ArrayList<>();

            // 시스템 메시지 (컨텍스트)
            if (StringUtils.hasText(context)) {
                messages.add(new SystemMessage(context));
            }

            // 사용자 메시지 (프롬프트)
            messages.add(new UserMessage(prompt));

            // Prompt 생성 및 모델 옵션 설정
            Prompt aiPrompt = new Prompt(
                    messages,
                    openAiChatOptions
            );

            // Chat API 호출
            ChatResponse response = chatModel.call(aiPrompt);

            // 응답 처리
            return response.getResult().getOutput().getText();

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
        return "OpenAI (Spring AI)";
    }

    @Override
    public int getMaxTokens() {
        return maxTokens;
    }

    @Override
    public List<AI> getAvailableAIs() {
        logger.debug("사용 가능한 모든 공개 AI 조회");
        return aiRepository.findAllByIsActiveTrueAndIsPublicTrue();
    }

    @Override
    public AIRespDto createAI(AIReqDto aiReqDto) {
        AI ai = aiRepository.save(AIReqDto.toEntity(aiReqDto));
        return AIRespDto.from(ai);
    }
}