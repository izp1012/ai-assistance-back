package com.uf.assistance.service.impl;

import com.uf.assistance.domain.ai.BaseAIRepository;
import com.uf.assistance.domain.ai.BaseAI;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.domain.ai.CustomAIRepository;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.dto.ai.BaseAIReqDto;
import com.uf.assistance.dto.ai.BaseAIRespDto;
import com.uf.assistance.dto.ai.CustomAIReqDto;
import com.uf.assistance.dto.ai.CustomAIRespDto;
import com.uf.assistance.handler.exception.ResourceNotFoundException;
import com.uf.assistance.service.AIService;
import com.uf.assistance.service.FileStorageService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * OpenAI API를 사용한 AI 서비스 구현 (Spring AI 적용)
 */
@Service
public class SpringAIOpenAIService implements AIService {
    private static final Logger logger = LoggerFactory.getLogger(SpringAIOpenAIService.class);

    private final BaseAIRepository baseAiRepository;
    private final CustomAIRepository customAiRepository;
    private final ChatModel chatModel;
    private final OpenAiChatOptions openAiChatOptions;
    private final String apiKey;
    private final int maxTokens;
    private final UserService userService;
    private final FileStorageService fileStorageService;


    public SpringAIOpenAIService(
            BaseAIRepository baseAiRepository,
            CustomAIRepository customAiRepository,
            ChatModel chatModel,
            OpenAiChatOptions openAiChatOptions,
            @Value("${spring.ai.openai.api-key:#{null}}") String apiKey,
            @Value("${openai.max.tokens:4096}") int maxTokens, UserService userService,
            FileStorageService fileStorageService
    ) {
        this.baseAiRepository = baseAiRepository;
        this.customAiRepository = customAiRepository;
        this.chatModel = chatModel;
        this.openAiChatOptions = openAiChatOptions;
        this.apiKey = apiKey;
        this.maxTokens = maxTokens;
        this.userService = userService;
        this.fileStorageService = fileStorageService;
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

        } catch (NullPointerException e){
            logger.error("OpenAI API 호출 중 NPE 오류 발생: {}", e.getMessage(), e);
            return "AI 서비스 NPE 오류 발생: " + e.getMessage();
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
    public BaseAI getBaseAIById(Long baseAIId) {
        logger.debug("BaseAI ID: {} 조회", baseAIId);
        BaseAI baseAI = baseAiRepository.findById(baseAIId)
                .orElseThrow(() -> new ResourceNotFoundException("Base AI", "id", baseAIId));
        return baseAI;
    }

    @Override
    public CustomAI getCustomAIById(Long customAIId) {
        logger.debug("CustomAI ID: {} 조회", customAIId);
        CustomAI customAI = customAiRepository.findById(customAIId)
                .orElseThrow(() -> new ResourceNotFoundException("AI", "id", customAIId));
        return customAI;
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
    public List<BaseAI> getAvailableBaseAIs() {
        logger.debug("사용 가능한 모든 공개 BaseAI 조회");
        return baseAiRepository.findAllByActiveTrue();
    }

    @Override
    public List<CustomAI> getAvailableCustomAIs() {
        logger.debug("사용 가능한 모든 공개 CustomAI 조회");
        return customAiRepository.findAllByActiveTrueAndHiddenTrue();
    }

    @Override
    public BaseAIRespDto createBaseAI(BaseAIReqDto baseAIReqDto) {
        User user = userService.findUserEntityById(baseAIReqDto.getUserId());

        BaseAI baseAI = baseAiRepository.save(BaseAIReqDto.toEntity(baseAIReqDto, user));
        return BaseAIRespDto.from(baseAI);
    }

    @Override
    public CustomAIRespDto createCustomAI(CustomAIReqDto customAIReqDto, MultipartFile file) {
        BaseAI baseAI = this.getBaseAIById(customAIReqDto.getBaseAiId());

        User user = userService.findUserEntityById(customAIReqDto.getUserId());

        // 이미지 처리
        String imageUrl = null;
        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.storeFile(file);

            // 이미지 URL 생성
            imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/home/ubuntu/youf/file/")
                    .path(fileName)
                    .toUriString();
        } else {
            // 기본 이미지 URL 사용 (이미지가 없는 경우)
            imageUrl = customAIReqDto.getImageUrl();
        }

        CustomAI customAI = customAiRepository.save(customAIReqDto.toEntity(customAIReqDto, baseAI, user, imageUrl));
        return CustomAIRespDto.from(customAI);
    }
}