package com.uf.assistance.config;

import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.domain.scheduler.ScheduledJobRepository;
import com.uf.assistance.domain.scheduler.Status;
import com.uf.assistance.service.DynamicSchedulerService;
import lombok.RequiredArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SchedulerInitializer implements ApplicationRunner {

    private final ScheduledJobRepository scheduledJobRepository;
    private final DynamicSchedulerService schedulerService;

    @Override
    public void run(ApplicationArguments args) {
        List<ScheduledJob> jobs = scheduledJobRepository.findByStatus(Status.ENABLED);
        for (ScheduledJob job : jobs) {
            try {
                schedulerService.scheduleJob(job); // Job 스케줄 등록
            } catch (SchedulerException e) {
                // 로깅
            }
        }
    }
}
