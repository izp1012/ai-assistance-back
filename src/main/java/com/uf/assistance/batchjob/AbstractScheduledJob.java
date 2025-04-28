package com.uf.assistance.batchjob;

import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.domain.scheduler.ScheduledJobRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 모든 스케줄 작업의 기본 구현을 제공하는 추상 클래스
 */
public abstract class AbstractScheduledJob implements Job {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected ScheduledJobRepository scheduledJobRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getJobDetail().getKey();
        String jobName = jobKey.getName();
        String jobGroup = jobKey.getGroup();

        log.info("Executing job: {}.{} at {}", jobGroup, jobName, LocalDateTime.now());

        try {
            // 작업 실행 전 처리
            beforeExecution(context);

            // 실제 작업 로직 실행
            executeJob(context);

            // 작업 실행 후 처리
            afterExecution(context);

            // DB에 마지막 실행 시간 업데이트
            updateJobExecutionInfo(jobName, jobGroup, context, null);

        } catch (Exception e) {
            log.error("Error executing job {}.{}: {}", jobGroup, jobName, e.getMessage(), e);

            // DB에 오류 정보 업데이트
            updateJobExecutionInfo(jobName, jobGroup, context, e);

            // Quartz에 예외 전파 여부 결정
            handleJobExecutionException(context, e);
        }
    }

    /**
     * 실제 작업 로직을 구현하는 메서드 (자식 클래스에서 구현 필요)
     */
    protected abstract void executeJob(JobExecutionContext context) throws Exception;

    /**
     * 작업 실행 전 처리할 로직
     */
    protected void beforeExecution(JobExecutionContext context) throws Exception {
        // 기본 구현 없음 - 필요시 자식 클래스에서 오버라이드
    }

    /**
     * 작업 실행 후 처리할 로직
     */
    protected void afterExecution(JobExecutionContext context) throws Exception {
        // 기본 구현 없음 - 필요시 자식 클래스에서 오버라이드
    }

    /**
     * DB에 작업 실행 정보 업데이트
     */
    protected void updateJobExecutionInfo(String jobName, String jobGroup, JobExecutionContext context, Exception exception) {
        try {
            Optional<ScheduledJob> jobOptional = scheduledJobRepository.findByJobNameAndJobGroup(jobName, jobGroup);

            if (jobOptional.isPresent()) {
                ScheduledJob job = jobOptional.get();
                job.updateLastExecution();

                // 예외가 발생한 경우 상태 업데이트
                if (exception != null) {
                    // 선택적: 오류 상태로 설정하거나 오류 메시지 저장
                    // job.setStatus(ScheduledJob.Status.ERROR);
                    // job.setJobData("{\"lastError\": \"" + exception.getMessage() + "\"}");
                }

                // 다음 실행 시간이 있는 경우 업데이트
                if (context.getNextFireTime() != null) {
                    job.updateNextExecution(
                            LocalDateTime.ofInstant(context.getNextFireTime().toInstant(),
                                    java.time.ZoneId.systemDefault()));
                }

                scheduledJobRepository.save(job);
            }
        } catch (Exception e) {
            log.error("Error updating job execution info: {}", e.getMessage(), e);
        }
    }

    /**
     * 작업 실행 중 발생한 예외 처리
     * @return true이면 Quartz에 예외를 전파하지 않음
     */
    protected void handleJobExecutionException(JobExecutionContext context, Exception exception) throws JobExecutionException {
        // 기본적으로 JobExecutionException으로 변환하여 전파
        throw new JobExecutionException("Job execution failed", exception, false);
    }
}