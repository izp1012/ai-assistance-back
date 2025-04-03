package com.uf.assistance.service;

import com.uf.assistance.domain.keyword.Interest;
import com.uf.assistance.domain.keyword.UserInterest;
import com.uf.assistance.domain.keyword.UserInterestRepository;
import com.uf.assistance.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserInterestService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserInterestRepository userInterestRepository;

    @Autowired
    public UserInterestService(UserInterestRepository userInterestRepository) {
        this.userInterestRepository = userInterestRepository;
    }

    /**
     * 사용자의 관심사 키워드 카운트 증가
     * 키워드가 없으면 새로 생성
     */
    @Transactional
    public UserInterest incrementUserInterestCount(User user, Interest interest) {
        logger.debug("사용자 ID: {}의 관심사 '{}' 카운트 증가", user.getId(), interest.getKeyword());

        Optional<UserInterest> existingUserInterest = userInterestRepository.findByUserAndInterest(user, interest);

        if (existingUserInterest.isPresent()) {
            // 이미 있는 경우 카운트 증가
            UserInterest userInterest = existingUserInterest.get();
            userInterest.incrementCount();
            return userInterestRepository.save(userInterest);
        } else {
            // 새로운 관심사인 경우 생성
            UserInterest userInterest = UserInterest.builder()
                    .user(user)
                    .interest(interest)
                    .count(1)
                    .build();
            return userInterestRepository.save(userInterest);
        }
    }

    /**
     * 사용자의 모든 관심사 조회 (카운트 내림차순)
     */
    public List<UserInterest> getUserInterests(User user) {
        logger.debug("사용자 ID: {}의 모든 관심사 조회", user.getId());
        return userInterestRepository.findByUserOrderByCountDesc(user);
    }

    /**
     * 특정 관심사를 가진 모든 사용자 조회
     */
    public List<UserInterest> getUsersByInterest(Interest interest) {
        logger.debug("관심사 ID: {}를 가진 모든 사용자 조회", interest.getId());
        return userInterestRepository.findByInterestOrderByCountDesc(interest);
    }
}