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

    @Transactional
    public SchedulerRespDto createJob(SchedulerReqDto schedulerReqDto) {
        AISubscription aiSubscription = aiSubscriptionService.getAISubScriptionById(schedulerReqDto.getAisubscriptionId());
        User user = userService.findUserEntityById(schedulerReqDto.getUserId());

        String jobName = schedulerReqDto.getJobName();
        String jobGroup = schedulerReqDto.getJobGroup();

        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findByJobNameAndJobGroupAndAiSubscription(jobName, jobGroup, aiSubscription);
        if (jobOpt.isEmpty()) {
            ScheduledJob job = SchedulerReqDto.toEntity(schedulerReqDto, aiSubscription, user);
            scheduledJobRepository.save(job);

            try {
                schedulerService.scheduleJob(job);
                return SchedulerRespDto.from(job);
            } catch (SchedulerException e) {
                throw new RuntimeException("스케줄 등록 실패", e);
            }
        }else{
            throw new CustomApiException("이미 등록된 스케줄입니다.");
        }
    }

    @Transactional
    public String deleteJob(Long jobId) {
        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        try {
            schedulerService.removeJob(job.getJobName(), job.getJobGroup());
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
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(Status.PAUSED);
        scheduledJobRepository.save(job);

        try {
            schedulerService.pauseJob(job.getJobName(), job.getJobGroup());
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 일시 정지 실패", e);
        }

        return "일시 정지 완료";
    }

    @Transactional
    public String resumeJob(Long jobId) {
        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(Status.ENABLED);
        scheduledJobRepository.save(job);

        try {
            schedulerService.resumeJob(job.getJobName(), job.getJobGroup());
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 재개 실패", e);
        }

        return "재개 완료";
    }

    @Transactional
    public String changeJobStatus(Long jobId, Status status) {
        Optional<ScheduledJob> jobOpt = scheduledJobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(status);
        scheduledJobRepository.save(job);

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
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
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

        List<SchedulerRespDto> customAIRespDtoList = list.stream()
                .map(SchedulerRespDto::from)
                .collect(Collectors.toList());

        return customAIRespDtoList;
    }

    public List<SchedulerRespDto> getAllJobs() {
        List<ScheduledJob> jobs = scheduledJobRepository.findAll();
        return jobs.stream()
                .map(SchedulerRespDto::from)
                .toList();
    }
}