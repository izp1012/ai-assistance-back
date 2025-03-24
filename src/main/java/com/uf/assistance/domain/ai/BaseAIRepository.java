package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * BaseAI 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface BaseAIRepository extends JpaRepository<BaseAI, Long> {

    /**
     * 활성화된 모든 AI 조회
     * @return 활성화된 AI 목록
     */
    List<BaseAI> findAllByActiveTrue();

    /**
     * 특정 개발자가 만든 AI 목록 조회
     * @param developer 개발자
     * @return 개발자가 만든 AI 목록
     */
    List<BaseAI> findByCreatedBy(User developer);
}