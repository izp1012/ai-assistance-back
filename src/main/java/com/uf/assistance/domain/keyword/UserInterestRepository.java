package com.uf.assistance.domain.keyword;

import com.uf.assistance.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInterestRepository extends JpaRepository<UserInterest, Long> {

    /**
     * 사용자 ID와 관심사 ID로 UserInterest 조회
     */
    Optional<UserInterest> findByUserAndInterest(User user, Interest interest);

    /**
     * 사용자의 모든 관심사 조회 (카운트 내림차순)
     */
    List<UserInterest> findByUserOrderByCountDesc(User user);

    /**
     * 특정 관심사를 가진 모든 사용자 조회
     */
    List<UserInterest> findByInterestOrderByCountDesc(Interest interest);
}