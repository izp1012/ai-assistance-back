package com.uf.assistance.service;

import com.uf.assistance.batchjob.DynamicQuartzJob;
import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.domain.scheduler.ScheduledJobRepository;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicSchedulerService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Scheduler scheduler;
    private final ScheduledJobRepository scheduledJobRepository;

    /**
     * Quartz JobKey 생성 (jobName_userId, jobGroup)
     * 등록/조회/일시정지/재개/삭제 모두 이 키를 사용해야 함
     */
    public static JobKey jobKeyOf(ScheduledJob job) {
        return new JobKey(job.getJobName() + "_" + job.getUser().getUserId(), job.getJobGroup());
    }

    public void scheduleJob(ScheduledJob job) throws SchedulerException {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("jobData", job.getJobData()); // ✨ jobData 세팅
        jobDataMap.put("jobType", job.getJobType()); // DynamicQuartzJob 실행 로직 분기 기준

        JobKey jobKey = jobKeyOf(job);
        JobDetail jobDetail = JobBuilder.newJob(DynamicQuartzJob.class)
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(jobKey.getName() + "_trigger", job.getJobGroup())
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
        jobDataMap.put("jobType", job.getJobType()); // DynamicQuartzJob 실행 로직 분기 기준

        JobDetail jobDetail = JobBuilder.newJob(DynamicQuartzJob.class)
                .withIdentity(job.getJobName() + "_" + job.getUser().getUserId() +"_onetime", job.getJobGroup())
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(job.getJobName() + "_" + job.getUser().getUserId() + "_OneTime_Trigger", job.getJobGroup())
                .startNow()
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * Quartz에서 작업 삭제 (DB 처리는 호출 측 담당)
     */
    public boolean removeJob(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = jobKeyOf(job);

        if (scheduler.checkExists(jobKey)) {
            boolean result = scheduler.deleteJob(jobKey);
            logger.info("Job deleted: {}", jobKey);
            return result;
        }
        logger.warn("Job not registered in Quartz, skip delete: {}", jobKey);
        return false;
    }

    /**
     * 작업 일시 중지 (DB 처리는 호출 측 담당)
     */
    public void pauseJob(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = jobKeyOf(job);
        if (scheduler.checkExists(jobKey)) {
            scheduler.pauseJob(jobKey);
            logger.info("Job paused: {}", jobKey);
        } else {
            throw new JobExecutionException("Job not found: " + jobKey);
        }
    }

    /**
     * 작업 재개 (DB 처리는 호출 측 담당)
     * 서버 재시작 시 SchedulerInitializer는 ENABLED 작업만 등록하므로, 미등록 상태면 새로 등록
     */
    public void resumeJob(ScheduledJob job) throws SchedulerException {
        JobKey jobKey = jobKeyOf(job);
        if (scheduler.checkExists(jobKey)) {
            scheduler.resumeJob(jobKey);
            logger.info("Job resumed: {}", jobKey);
        } else {
            scheduleJob(job);
            logger.info("Job re-scheduled on resume: {}", jobKey);
        }
    }

    public void updateJobStatus(ScheduledJob job) throws SchedulerException {
        switch (job.getStatus()) {
            case ENABLED:
                resumeJob(job);
                break;

            case DISABLED:
                removeJob(job);
                break;

            case PAUSED:
                pauseJob(job);
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