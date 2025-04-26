package com.uf.assistance.service.impl;

import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.ai.AISubscriptionRepository;
import com.uf.assistance.domain.ai.BaseAIRepository;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.ai.AISubScriptionRespDto;
import com.uf.assistance.dto.ai.CustomAIRespDto;
import com.uf.assistance.handler.exception.ResourceNotFoundException;
import com.uf.assistance.service.AIService;
import com.uf.assistance.service.AISubscriptionService;
import com.uf.assistance.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
 * AI 구독 서비스 OpenAI 구현체
 */
@Service
public class SpringAIOpenAISubscriptionService implements AISubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(SpringAIOpenAISubscriptionService.class);

    private final BaseAIRepository baseAiRepository;
    private final AISubscriptionRepository aiSubscriptionRepository;
    private final AIService aiService;
    private final UserService userService;

    @Autowired
    public SpringAIOpenAISubscriptionService(
            BaseAIRepository baseAiRepository,
            AISubscriptionRepository aiSubscriptionRepository,
            SpringAIOpenAIService aiService,
            UserService userService) {
        this.baseAiRepository = baseAiRepository;
        this.aiSubscriptionRepository = aiSubscriptionRepository;
        this.aiService = aiService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    @Override
    public AISubscription getAISubScriptionById(Long subscriptionId) {
        return aiSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("AISubScription", "id", subscriptionId));

    }

    @Override
    public List<CustomAIRespDto> getSubscribedAIs(String userId) {
        logger.debug("사용자 ID: {}의 구독 AI 목록 조회", userId);
        User user = userService.findUserEntityById(userId);

        List<CustomAI> customAIList = aiSubscriptionRepository.findAIsByUser(user);
        List<CustomAIRespDto> customAIRespDtoList = new ArrayList<>();

        for(CustomAI customAI: customAIList){
            customAIRespDtoList.add(CustomAIRespDto.from(customAI));
        }

        return customAIRespDtoList;
    }


    @Override
    public boolean hasUserSubscribedAI(String userId, Long aiId) {
        logger.debug("사용자 ID: {}의 AI ID: {} 구독 여부 확인", userId, aiId);

        User user = userService.findUserEntityById(userId);
        CustomAI customAI = aiService.getCustomAIById(aiId);

        return aiSubscriptionRepository.existsByUserAndCustomAI(user, customAI);
    }

    @Override
    @Transactional
    public AISubScriptionRespDto subscribe(String userId, Long aiId) {
        logger.debug("사용자 ID: {}의 AI ID: {} 구독 시작", userId, aiId);

        User user = userService.findUserEntityById(userId);
        CustomAI customAI = aiService.getCustomAIById(aiId);

        // 이미 구독 중인지 확인
        if (aiSubscriptionRepository.existsByUserAndCustomAI(user, customAI)) {
            logger.info("사용자 ID: {}가 이미 AI ID: {}를 구독 중입니다", userId, aiId);
            throw new RuntimeException("사용자 ID: "+userId+"가 이미 AI ID: "+aiId+"를 구독 중입니다");
        }

        // AI 활성화 여부 확인
        if (!customAI.isActive()) {
            logger.error("비활성화된 AI ID: {}는 구독할 수 없습니다", aiId);
            throw new IllegalStateException("비활성화된 AI는 구독할 수 없습니다: " + aiId);
        }
        // 새 구독 생성
        AISubscription subscription = AISubscription.builder()
                .user(user)
                .customAI(customAI)
                .subscribedAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .build();

        AISubscription aiSubScription = aiSubscriptionRepository.save(subscription);
        return AISubScriptionRespDto.from(aiSubScription);
    }

    @Override
    @Transactional
    public void unsubscribe(String userId, Long aiId) {
        logger.debug("사용자 ID: {}의 AI ID: {} 구독 취소", userId, aiId);

        User user = userService.findUserEntityById(userId);
        CustomAI customAI = aiService.getCustomAIById(aiId);

        Optional<AISubscription> subscription = aiSubscriptionRepository.findByUserAndCustomAI(user, customAI);
        if (subscription.isPresent()) {
            aiSubscriptionRepository.delete(subscription.get());
            logger.info("사용자 ID: {}의 AI ID: {} 구독이 취소되었습니다", userId, aiId);
        } else {
            logger.warn("사용자 ID: {}의 AI ID: {} 구독 정보가 없습니다", userId, aiId);
        }
    }

    @Override
    public String generateStandaloneAIResponse(CustomAI ai, Map<String, String> variables) {
        logger.debug("AI ID: {}를 사용한 독립형 응답 생성", ai.getId());

        if (!ai.isActive()) {
            logger.error("비활성화된 AI ID: {}로 응답을 생성할 수 없습니다", ai.getId());
            return "이 AI는 현재 비활성화되어 있습니다.";
        }

        // 변수가 null이면 빈 맵으로 초기화
        if (variables == null) {
            variables = new HashMap<>();
        }

        try {
            // BaseAI의 프롬프트 템플릿 가져오기
            String basePrompt = ai.getBaseAI().getBasePrompt();
            if (basePrompt == null) {
                logger.error("AI ID: {}의 BaseAI 가 없습니다", ai.getId());
                return "AI 프롬프트 설정이 올바르지 않습니다.";
            }

            // 기본 프롬프트에 변수 주입
            String promptText = basePrompt.format(variables.toString());

            // 사용자 정의 프롬프트와 결합
            String customPrompt = ai.getCustomPrompt();
            if (StringUtils.hasText(customPrompt)) {
                promptText = CustomAI.combine(promptText, customPrompt);
            }

            // AI 서비스를 통해 응답 생성
            String response = aiService.generateResponse(promptText, null);

            return response;

        } catch (NullPointerException e){
            logger.error("OpenAI API 호출 중 NPE 오류 발생: {}", e.getMessage(), e);
            return "AI 서비스 NPE 오류 발생: " + e.getMessage();
        } catch (Exception e) {
            logger.error("AI ID: {}에서 응답 생성 중 오류 발생: {}", ai.getId(), e.getMessage(), e);
            return "AI 응답 생성 중 오류가 발생했습니다: " + e.getMessage();
        }

    }

    @Override
    @Transactional
    public void updateLastUsed(String userId, Long aiId) {
        logger.debug("사용자 ID: {}의 AI ID: {} 마지막 사용 시간 업데이트", userId, aiId);

        User user = userService.findUserEntityById(userId);

        CustomAI customAI = aiService.getCustomAIById(aiId);

        aiSubscriptionRepository.findByUserAndCustomAI(user, customAI)
                .ifPresent(subscription -> {
                    // JPA는 @Builder로 생성된 객체에 setter가 없어 새 객체를 생성해 저장
                    AISubscription updatedSubscription = AISubscription.builder()
                            .id(subscription.getId())
                            .user(subscription.getUser())
                            .customAI(subscription.getCustomAI())
                            .subscribedAt(subscription.getSubscribedAt())
                            .lastUsedAt(LocalDateTime.now())
                            .build();

                    aiSubscriptionRepository.save(updatedSubscription);
                    logger.debug("사용자 ID: {}의 AI ID: {} 마지막 사용 시간이 업데이트되었습니다", userId, aiId);
                });
    }
}