package com.uf.assistance.domain.scheduler;

import com.uf.assistance.domain.ai.AISubscription;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
/**
 * 스케줄 작업을 위한 데이터 액세스 인터페이스
 */
@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
    List<ScheduledJob> findAll(Specification spec);

    /**
     * 특정 상태의 모든 작업을 찾음
     * @param status 작업 상태
     * @return 해당 상태의 작업 목록
     */
    List<ScheduledJob> findByStatus(Status status);

    /**
     * 작업 이름과 그룹, AISubscription 으로 작업을 찾음
     * @param jobName 작업 이름
     * @param jobGroup 작업 그룹
     * @param aiSubscription 비서 구독
     * @return 해당 작업
     */
    Optional<ScheduledJob> findByJobNameAndJobGroupAndAiSubscription(String jobName, String jobGroup, AISubscription aiSubscription);


    /**
     * 작업 이름과 그룹으로 작업을 찾음
     * @param jobName 작업 이름
     * @param jobGroup 작업 그룹
     * @return 해당 작업
     */
    Optional<ScheduledJob> findByJobNameAndJobGroup(String jobName, String jobGroup);

    /**
     * 작업 클래스로 작업을 찾음
     * @param jobClass 작업 클래스명
     * @return 해당 작업 목록
     */
    List<ScheduledJob> findByJobClass(String jobClass);

    /**
     * 특정 그룹의 모든 작업을 찾음
     * @param jobGroup 작업 그룹
     * @return 해당 그룹의 작업 목록
     */
    List<ScheduledJob> findByJobGroup(String jobGroup);

    /**
     * 작업 이름을 포함하는 작업을 찾음
     * @param jobNamePattern 작업 이름 패턴
     * @return 해당 패턴의 작업 목록
     */
    List<ScheduledJob> findByJobNameContaining(String jobNamePattern);

    /**
     * 특정 상태와 그룹의 작업을 찾음
     * @param status 작업 상태
     * @param jobGroup 작업 그룹
     * @return 해당 상태와 그룹의 작업 목록
     */
    List<ScheduledJob> findByStatusAndJobGroup(Status status, String jobGroup);

    /**
     * 해당 작업 이름과 그룹의 작업이 존재하는지 확인
     * @param jobName 작업 이름
     * @param jobGroup 작업 그룹
     * @return 존재 여부
     */
    boolean existsByJobNameAndJobGroup(String jobName, String jobGroup);
}
