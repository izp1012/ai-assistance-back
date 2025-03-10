package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.ai.PromptTemplate;
import com.uf.assistance.domain.ai.PromptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 프롬프트 템플릿 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface PromptTemplateRepository extends JpaRepository<PromptTemplate, Long> {

    /**
     * 활성화된 프롬프트 템플릿 조회
     * @return 활성화된 프롬프트 템플릿 목록
     */
    List<PromptTemplate> findAllByIsActiveTrue();

    /**
     * 프롬프트 타입별 활성화된 템플릿 조회
     * @param type 프롬프트 타입
     * @return 해당 타입의 활성화된 템플릿 목록
     */
    List<PromptTemplate> findAllByTypeAndIsActiveTrue(PromptType type);
}