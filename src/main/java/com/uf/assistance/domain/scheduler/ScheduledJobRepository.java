package com.uf.assistance.domain.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledJobRepository extends JpaRepository<ScheduledJob, Long> {
    List<ScheduledJob> findByStatus(ScheduledJob.Status status);
}
