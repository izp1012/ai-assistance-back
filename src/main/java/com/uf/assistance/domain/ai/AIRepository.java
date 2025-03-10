package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.ai.AI;
import com.uf.assistance.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AI 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface AIRepository extends JpaRepository<AI, Long> {

    /**
     * 활성화된 모든 AI 조회
     * @return 활성화된 AI 목록
     */
    List<AI> findAllByIsActiveTrue();

    /**
     * 공개된 활성화 AI 목록 조회
     * @return 공개된 활성화 AI 목록
     */
    List<AI> findAllByIsActiveTrueAndIsPublicTrue();

    /**
     * 특정 개발자가 만든 AI 목록 조회
     * @param developer 개발자
     * @return 개발자가 만든 AI 목록
     */
    List<AI> findByDeveloper(User developer);
}