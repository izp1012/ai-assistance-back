package com.uf.assistance.service;

import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.scheduler.ScheduleJobSpecification;
import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.domain.scheduler.ScheduledJobRepository;
import com.uf.assistance.domain.scheduler.Status;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.scheduler.SchedulerReqDto;
import com.uf.assistance.dto.scheduler.SchedulerRespDto;
import com.uf.assistance.handler.exception.CustomApiException;
import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduledJobService {

    private final ScheduledJobRepository scheduledJobRepository;
    private final DynamicSchedulerService schedulerService;
    private final AISubscriptionService aiSubscriptionService;
    private final UserService userService;
    private final Scheduler scheduler;

    @Transactional
    public SchedulerRespDto createJob(SchedulerReqDto schedulerReqDto) {
        // Quartz cron은 초 단위 포함 6~7자리 (예: 0 15 10 * * ?)
        String cronExpression = schedulerReqDto.getCronExpression();
        if (cronExpression == null || !CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("잘못된 cron 표현식입니다. (초 분 시 일 월 요일 형식, 예: 0 15 10 * * ?) - " + cronExpression);
        }

        AISubscription aiSubscription = aiSubscriptionService.getAISubScriptionById(schedulerReqDto.getAisubscriptionId());
        User user = userService.findUserEntityById(schedulerReqDto.getUserId());

        String jobName = schedulerReqDto.getJobName();
        String userId = schedulerReqDto.getUserId();
        String jobGroup = schedulerReqDto.getJobGroup();

        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findByJobNameAndJobGroupAndAiSubscription(jobName, jobGroup, aiSubscription);
        if (jobOpt.isEmpty()) {
            ScheduledJob job = SchedulerReqDto.toEntity(schedulerReqDto, aiSubscription, user);
            scheduledJobRepository.save(job);
            JobKey jobKey = DynamicSchedulerService.jobKeyOf(job);

            try {
                if (!scheduler.checkExists(jobKey)) {
                    schedulerService.scheduleJob(job);
                    return SchedulerRespDto.from(job);
                } else {
                    throw new SchedulerException("Job " + job.getJobName() + " already exists");
                }
            } catch (SchedulerException e) {
                throw new RuntimeException("스케줄 등록 실패 - "+e.getMessage(), e);
            }
        }else{
            throw new CustomApiException("이미 등록된 스케줄입니다. - "+jobName + "_" + userId);
        }
    }

    @Transactional
    public String deleteJob(Long jobId) {
        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new CustomApiException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        try {
            schedulerService.removeJob(job);
            scheduledJobRepository.delete(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 삭제 실패", e);
        }

        return "삭제 완료";
    }

    @Transactional
    public String pauseJob(Long jobId) {
        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new CustomApiException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(Status.PAUSED);
        scheduledJobRepository.save(job);

        try {
            schedulerService.pauseJob(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 일시 정지 실패", e);
        }

        return "일시 정지 완료";
    }

    @Transactional
    public String resumeJob(Long jobId) {
        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new CustomApiException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(Status.ENABLED);
        scheduledJobRepository.save(job);

        try {
            schedulerService.resumeJob(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 재개 실패", e);
        }

        return "재개 완료";
    }

    @Transactional
    public String changeJobStatus(Long jobId, Status status) {
        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new CustomApiException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(status);

        try {
            schedulerService.updateJobStatus(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 상태 변경 실패", e);
        }

        return "상태 변경 완료";
    }

    @Transactional
    public String triggerJobNow(Long jobId, Status status) {
        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new CustomApiException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();

        try {
            schedulerService.triggerOneTimeJob(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("작업 수동 실행 실패", e);
        }

        return "작업 수동 실행 완료";
    }

    public List<SchedulerRespDto> getJobByUserAndSubscription(String userId, Long aiSubscriptionId) {
        Specification<ScheduledJob> spec = Specification.where(null);

        if (userId != null) {
            spec = spec.and(ScheduleJobSpecification.hasUserId(userId));
        }

        if (aiSubscriptionId != null) {
            spec = spec.and(ScheduleJobSpecification.hasAiSubscriptionId(aiSubscriptionId));
        }

        List<ScheduledJob> list = scheduledJobRepository.findAll(spec);

        return list.stream()
                .map(SchedulerRespDto::from)
                .collect(Collectors.toList());
    }

    public List<SchedulerRespDto> getAllJobs() {
        List<ScheduledJob> jobs = scheduledJobRepository.findAll();
        return jobs.stream()
                .map(SchedulerRespDto::from)
                .toList();
    }
}