package com.uf.assistance.service;

import com.uf.assistance.domain.ai.BaseAI;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.dto.ai.BaseAIReqDto;
import com.uf.assistance.dto.ai.BaseAIRespDto;
import com.uf.assistance.dto.ai.CustomAIReqDto;
import com.uf.assistance.dto.ai.CustomAIRespDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
     * 특정 BaseAI 조회
     * @param baseAIId AI ID
     * @return AI 정보
     */
    BaseAI getBaseAIById(Long baseAIId);

    /**
     * 특정 CustomAI 조회
     * @param customAIId AI ID
     * @return AI 정보
     */
    CustomAI getCustomAIById(Long customAIId);

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

    /**
     * 사용 가능한 모든 BaseAI 조회
     * @return AI 목록
     */
    List<BaseAI> getAvailableBaseAIs();

    /**
     * @Param active
     * @Param hidden
     * @return active, hidden 에 따른, Custom AI 목록
     */
    List<CustomAI> getCustomAIs(Boolean isActive, Boolean isHidden);
    /**
     * AI 생성
     * @return 생성한 AI 정보
     */
    BaseAIRespDto createBaseAI(BaseAIReqDto aiReqDto);

    /**
     * CustomAI 생성
     * @return 생성한 AI 정보
     */
    CustomAIRespDto createCustomAI(CustomAIReqDto aiReqDto, MultipartFile file);
}