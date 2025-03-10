package com.uf.assistance.service.impl;
import com.uf.assistance.domain.ai.*;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.handler.exception.ResourceNotFoundException;
import com.uf.assistance.service.AIService;
import com.uf.assistance.service.AISubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AI 구독 서비스 OpenAI 구현체
 */
@Service
public class SpringAIOpenAISubscriptionService implements AISubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(SpringAIOpenAISubscriptionService.class);

    private final AIRepository aiRepository;
    private final AISubscriptionRepository aiSubscriptionRepository;
    private final AIService aiService;

    @Autowired
    public SpringAIOpenAISubscriptionService(
            AIRepository aiRepository,
            AISubscriptionRepository aiSubscriptionRepository,
            SpringAIOpenAIService aiService) {
        this.aiRepository = aiRepository;
        this.aiSubscriptionRepository = aiSubscriptionRepository;
        this.aiService = aiService;
    }

    @Override
    public List<AI> getAvailableAIs() {
        logger.debug("사용 가능한 모든 공개 AI 조회");
        return aiRepository.findAllByIsActiveTrueAndIsPublicTrue();
    }

    @Override
    public List<AI> getSubscribedAIs(Long userId) {
        logger.debug("사용자 ID: {}의 구독 AI 목록 조회", userId);
        User user = new User(); // 실제로는 UserRepository에서 조회해야 함
        user.setId(userId);
        return aiSubscriptionRepository.findAIsByUser(user);
    }

    @Override
    public AI getAIById(Long aiId) {
        logger.debug("AI ID: {} 조회", aiId);
        return aiRepository.findById(aiId)
                .orElseThrow(() -> new ResourceNotFoundException("AI", "id", aiId));
    }

    @Override
    public boolean hasUserSubscribedAI(Long userId, Long aiId) {
        logger.debug("사용자 ID: {}의 AI ID: {} 구독 여부 확인", userId, aiId);

        User user = new User(); // 실제로는 UserRepository에서 조회해야 함
        user.setId(userId);

        AI ai = getAIById(aiId);

        return aiSubscriptionRepository.existsByUserAndAi(user, ai);
    }

    @Override
    @Transactional
    public AISubscription subscribe(Long userId, Long aiId) {
        logger.debug("사용자 ID: {}의 AI ID: {} 구독 시작", userId, aiId);

        User user = new User(); // 실제로는 UserRepository에서 조회해야 함
        user.setId(userId);

        AI ai = getAIById(aiId);

        // 이미 구독 중인지 확인
        if (aiSubscriptionRepository.existsByUserAndAi(user, ai)) {
            logger.info("사용자 ID: {}가 이미 AI ID: {}를 구독 중입니다", userId, aiId);
            return aiSubscriptionRepository.findByUserAndAi(user, ai).orElse(null);
        }

        // AI 활성화 여부 확인
        if (!ai.isActive()) {
            logger.error("비활성화된 AI ID: {}는 구독할 수 없습니다", aiId);
            throw new IllegalStateException("비활성화된 AI는 구독할 수 없습니다: " + aiId);
        }

        // 새 구독 생성
        AISubscription subscription = AISubscription.builder()
                .user(user)
                .ai(ai)
                .subscribedAt(LocalDateTime.now())
                .lastUsedAt(LocalDateTime.now())
                .build();

        return aiSubscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(Long userId, Long aiId) {
        logger.debug("사용자 ID: {}의 AI ID: {} 구독 취소", userId, aiId);

        User user = new User(); // 실제로는 UserRepository에서 조회해야 함
        user.setId(userId);

        AI ai = getAIById(aiId);

        Optional<AISubscription> subscription = aiSubscriptionRepository.findByUserAndAi(user, ai);
        if (subscription.isPresent()) {
            aiSubscriptionRepository.delete(subscription.get());
            logger.info("사용자 ID: {}의 AI ID: {} 구독이 취소되었습니다", userId, aiId);
        } else {
            logger.warn("사용자 ID: {}의 AI ID: {} 구독 정보가 없습니다", userId, aiId);
        }
    }

    @Override
    public String generateStandaloneAIResponse(AI ai, Map<String, String> variables) {
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
            // AI의 프롬프트 템플릿 가져오기
            PromptTemplate basePrompt = ai.getBasePrompt();
            if (basePrompt == null) {
                logger.error("AI ID: {}의 기본 프롬프트 템플릿이 없습니다", ai.getId());
                return "AI 프롬프트 설정이 올바르지 않습니다.";
            }

            // 기본 프롬프트에 변수 주입
            String promptText = basePrompt.format(variables);

            // 사용자 정의 프롬프트와 결합
            String customPrompt = ai.getCustomPrompt();
            if (StringUtils.hasText(customPrompt)) {
                promptText = PromptTemplate.combine(promptText, customPrompt);
            }

            // AI 서비스를 통해 응답 생성
            String response = aiService.generateResponse(promptText, null);
            logger.debug("AI ID: {}에서 응답 생성 완료", ai.getId());
            return response;
        } catch (Exception e) {
            logger.error("AI ID: {}에서 응답 생성 중 오류 발생: {}", ai.getId(), e.getMessage(), e);
            return "AI 응답 생성 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    @Override
    @Transactional
    public void updateLastUsed(Long userId, Long aiId) {
        logger.debug("사용자 ID: {}의 AI ID: {} 마지막 사용 시간 업데이트", userId, aiId);

        User user = new User(); // 실제로는 UserRepository에서 조회해야 함
        user.setId(userId);

        AI ai = getAIById(aiId);

        aiSubscriptionRepository.findByUserAndAi(user, ai)
                .ifPresent(subscription -> {
                    // JPA는 @Builder로 생성된 객체에 setter가 없어 새 객체를 생성해 저장
                    AISubscription updatedSubscription = AISubscription.builder()
                            .id(subscription.getId())
                            .user(subscription.getUser())
                            .ai(subscription.getAi())
                            .subscribedAt(subscription.getSubscribedAt())
                            .lastUsedAt(LocalDateTime.now())
                            .build();

                    aiSubscriptionRepository.save(updatedSubscription);
                    logger.debug("사용자 ID: {}의 AI ID: {} 마지막 사용 시간이 업데이트되었습니다", userId, aiId);
                });
    }
}