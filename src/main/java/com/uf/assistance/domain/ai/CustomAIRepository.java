package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.user.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CustomAI 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface CustomAIRepository extends JpaRepository<CustomAI, Long>, JpaSpecificationExecutor {

    @Override
    List<CustomAI> findAll();

    List<CustomAI> findAll(Specification spec);

    List<CustomAI> findByActive(boolean active);

    List<CustomAI> findByHidden(boolean hidden);

    /**
     * 활성화된 모든 AI 조회
     * @return 활성화된 AI 목록
     */
    List<CustomAI> findAllByActiveTrue();

    /**
     * @Param active
     * @Param hidden
     * @return active, hidden 에 따른, Custom AI 목록
     */
    List<CustomAI> findAllByActiveAndHidden(boolean active, boolean hidden);

    /**
     * 특정 사용자가 만든 AI 목록 조회
     * @param developer 개발자
     * @return 사용자가 만든 AI 목록
     */
    List<CustomAI> findByCreatedBy(User developer);
}