package com.uf.assistance.service;

import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.dto.ai.AISubScriptionRespDto;
import com.uf.assistance.dto.ai.CustomAIRespDto;

import java.util.List;
import java.util.Map;

/**
 * AI 구독 서비스 인터페이스
 * 사용자의 AI 구독 관리 및 AI 응답 생성 서비스에 대한 기능 정의
 */
public interface AISubscriptionService {

    AISubscription getAISubScriptionById(Long subscriptionId);
    /**
     * 특정 사용자가 구독한 AI 목록 조회
     * @param userId 사용자 ID
     * @return 구독한 AI 목록
     */
    List<CustomAIRespDto> getSubscribedAIs(String userId);

    /**
     * 사용자의 AI 구독 여부 확인
     * @param userId 사용자 ID
     * @param aiId AI ID
     * @return 구독 여부
     */
    boolean hasUserSubscribedAI(String userId, Long aiId);

    /**
     * AI 구독
     * @param userId 사용자 ID
     * @param aiId AI ID
     * @return 생성된 구독 정보
     */
    AISubScriptionRespDto subscribe(String userId, Long aiId);

    /**
     * AI 구독 취소
     * @param aisubscriptionId 구독 ID
     */
    Map<String, String> unsubscribe(Long aisubscriptionId);

    /**
     * 독립형 AI 응답 생성 (채팅방 없이)
     * @param ai AI 정보
     * @param variables 프롬프트 변수
     * @return AI 응답
     */
    String generateStandaloneAIResponse(CustomAI ai, Map<String, String> variables);

    /**
     * 구독 사용 기록 업데이트
     * @param userId 사용자 ID
     * @param aiId AI ID
     */
    void updateLastUsed(String userId, Long aiId);
}
