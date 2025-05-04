package com.uf.assistance.service;

import com.uf.assistance.batchjob.DynamicQuartzJob;
import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.domain.scheduler.ScheduledJobRepository;
import com.uf.assistance.domain.scheduler.Status;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicSchedulerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Scheduler scheduler;
    private final ScheduledJobRepository scheduledJobRepository;

    public void scheduleJob(ScheduledJob job) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobData", job.getJobData()); // ✨ jobData 세팅

        JobDetail jobDetail = JobBuilder.newJob(DynamicQuartzJob.class)
                .withIdentity(job.getJobName(), job.getJobGroup())
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName() + "_trigger", job.getJobGroup())
                .withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Quartz에 등록되지 않은 Job을 단건 실행한다.
     */
    public void triggerOneTimeJob(ScheduledJob job) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobData", job.getJobData()); // ✨ jobData 세팅

        JobDetail jobDetail = JobBuilder.newJob(DynamicQuartzJob.class)
                .withIdentity(job.getJobName()+"_onetime", job.getJobGroup())
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName() + "_OneTime_Trigger", job.getJobGroup())
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    @Transactional
    public boolean removeJob(String jobName, String groupName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, groupName);

        if (scheduler.checkExists(jobKey)) {
            boolean result = scheduler.deleteJob(jobKey);

            // DB에서 작업 삭제 또는 상태 업데이트
            ScheduledJob job = scheduledJobRepository.findByJobNameAndJobGroup(jobName, groupName)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found"));
            job.setStatus(Status.DISABLED);
            scheduledJobRepository.save(job);

            logger.info("Job deleted: {}", jobName);
            return result;
        }
        return false;
    }

    /**
     * 작업 일시 중지
     */
    @Transactional
    public void pauseJob(String jobName, String groupName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, groupName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);

            // DB 상태 업데이트
            ScheduledJob job = scheduledJobRepository.findByJobNameAndJobGroup(jobName, groupName)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found"));
            job.setStatus(Status.PAUSED);
            scheduledJobRepository.save(job);

            logger.info("Job paused: {}", jobName);
        } else {
            throw new JobExecutionException("Job not found: " + jobName);
        }
    }

    /**
     * 작업 재개
     */
    @Transactional
    public void resumeJob(String jobName, String groupName) throws SchedulerException {
        JobKey jobKey = new JobKey(jobName, groupName);
        if (scheduler.checkExists(jobKey)) {
            scheduler.resumeJob(jobKey);

            // DB 상태 업데이트
            ScheduledJob job = scheduledJobRepository.findByJobNameAndJobGroup(jobName, groupName)
                    .orElseThrow(() -> new IllegalArgumentException("Job not found"));
            job.setStatus(Status.ENABLED);
            scheduledJobRepository.save(job);

            logger.info("Job resumed: {}", jobName);
        } else {
            throw new JobExecutionException("Job not found: " + jobName);
        }
    }

    public void updateJobStatus(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = new JobKey(job.getJobName(), job.getJobGroup());

        if (!scheduler.checkExists(jobKey)) {
            throw new JobExecutionException("Job not found: " + job.getJobName());
        }

        switch (job.getStatus()) {
            case ENABLED:
                scheduler.resumeJob(jobKey);
                logger.info("Job resumed: {}", jobKey);
                break;

            case DISABLED:
                scheduler.deleteJob(jobKey);
                logger.info("Job deleted: {}", jobKey);
                break;

            case PAUSED:
                scheduler.pauseJob(jobKey);
                logger.info("Job paused: {}", jobKey);
                break;

            default:
                throw new IllegalArgumentException("Unsupported status: " + job.getStatus());
        }

        // DB 상태 저장
        scheduledJobRepository.save(job);
    }

    /**
     * 작업 상태 확인
     */
    public Trigger.TriggerState getJobState(String jobName, String groupName) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey(jobName, groupName);
        return scheduler.getTriggerState(triggerKey);
    }

    /**
     * 모든 작업 목록 조회
     */
    public List<String> getAllJobNames() throws SchedulerException {
        return scheduler.getJobKeys(GroupMatcher.anyGroup())
                .stream()
                .map(JobKey::getName)
                .toList();
    }

    /**
     * Job Detail 생성
     */
    private JobDetail buildJobDetail(ScheduledJob job) {
        Class<? extends Job> jobClass;
        try {
            // 문자열로 저장된 클래스 이름을 실제 클래스 객체로 변환
            jobClass = (Class<? extends Job>) Class.forName(job.getJobClass());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Invalid job class: " + job.getJobClass(), e);
        }

        return JobBuilder.newJob(jobClass)
                .withIdentity(job.getJobName(), job.getJobGroup())
                .withDescription(job.getDescription())
                .storeDurably()
                .build();
    }
    /**
     * Trigger 생성
     */
    private Trigger buildTrigger(ScheduledJob job) {
        TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName(), job.getJobGroup())
                .withDescription(job.getDescription())
                .startAt(new Date()); // 기본 시작 시간

        // Cron 표현식이 있으면 Cron 트리거 사용
        if (job.getCronExpression() != null && !job.getCronExpression().isEmpty()) {
            return builder.withSchedule(CronScheduleBuilder.cronSchedule(job.getCronExpression()))
                    .build();
        }

        // 간단한 반복 트리거 (기본값: 1시간마다)
        return builder.withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInHours(1)
                        .repeatForever())
                .build();
    }
}