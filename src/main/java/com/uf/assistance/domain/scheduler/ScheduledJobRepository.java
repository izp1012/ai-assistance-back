package com.uf.assistance.domain.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
    List<ScheduledJob> findByStatus(ScheduledJob.Status status);

//    Optional<ScheduledJob> findByUserIdAndAiSubscriptionId(String userId, Long aiSubscriptionId);
    Optional<ScheduledJob> findByAiSubscriptionId(Long aiSubscriptionId);
}
