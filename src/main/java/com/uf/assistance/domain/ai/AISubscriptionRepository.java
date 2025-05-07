package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AI 구독 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface AISubscriptionRepository extends JpaRepository<AISubscription, Long> {

    /**
     * 사용자가 구독한 AI 목록 조회
     * @param user 사용자
     * @return 구독한 AI 목록
     */
    @Query("SELECT s.customAI FROM AISubscription s WHERE s.user = :user AND s.customAI.active = true")
    List<CustomAI> findAIsByUser(@Param("user") User user);

    /**
     * 사용자의 특정 AI 구독 정보 조회
     * @param user 사용자
     * @param customAI AI
     * @return 구독 정보
     */
    Optional<AISubscription> findByUserAndCustomAI(User user, CustomAI customAI);

    /**
     * 사용자의 AI 구독목록 조회
     * @param user 사용자
     * @return 구독 정보
     */
    List<AISubscription> findByUser(User user);

    /**
     * 사용자의 특정 AI 구독 여부 확인
     * @param user 사용자
     * @param customAI AI
     * @return 구독 여부
     */
    boolean existsByUserAndCustomAI(User user, CustomAI customAI);
}