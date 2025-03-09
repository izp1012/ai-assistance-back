package com.uf.assistance.service;

public interface AIService {
    /**
     * AI 응답 생성
     * @param prompt 사용자 프롬프트
     * @param context 대화 컨텍스트
     * @return AI 생성 응답
     */
    String generateResponse(String prompt, String context);

    /**
     * 서비스 가용성 확인
     * @return 서비스 사용 가능 여부
     */
    boolean isAvailable();

    /**
     * 공급자 이름 반환
     * @return AI 서비스 공급자 이름
     */
    String getProviderName();

    /**
     * 최대 토큰 수 반환
     * @return 최대 처리 가능한 토큰 수
     */
    int getMaxTokens();
}