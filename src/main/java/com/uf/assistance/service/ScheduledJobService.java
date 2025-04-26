package com.uf.assistance.service;

import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.domain.scheduler.ScheduledJobRepository;
import com.uf.assistance.dto.scheduler.SchedulerReqDto;
import com.uf.assistance.dto.scheduler.SchedulerRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.quartz.SchedulerException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScheduledJobService {

    private final ScheduledJobRepository jobRepository;
    private final DynamicSchedulerService schedulerService;

    @Transactional
    public SchedulerRespDto createJob(SchedulerReqDto schedulerReqDto) {
        ScheduledJob job = SchedulerReqDto.toEntity(schedulerReqDto);
        jobRepository.save(job);

        try {
            schedulerService.scheduleJob(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 등록 실패", e);
        }

        return SchedulerRespDto.from(job);
    }

    @Transactional
    public String deleteJob(Long jobId) {
        Optional<ScheduledJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        try {
            schedulerService.removeJob(job);
            jobRepository.delete(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 삭제 실패", e);
        }

        return "삭제 완료";
    }

    @Transactional
    public String pauseJob(Long jobId) {
        Optional<ScheduledJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(ScheduledJob.Status.PAUSED);
        jobRepository.save(job);

        try {
            schedulerService.pauseJob(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 일시 정지 실패", e);
        }

        return "일시 정지 완료";
    }

    @Transactional
    public String resumeJob(Long jobId) {
        Optional<ScheduledJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(ScheduledJob.Status.ENABLED);
        jobRepository.save(job);

        try {
            schedulerService.resumeJob(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 재개 실패", e);
        }

        return "재개 완료";
    }

    @Transactional
    public String changeJobStatus(Long jobId, ScheduledJob.Status status) {
        Optional<ScheduledJob> jobOpt = jobRepository.findById(jobId);
        if (jobOpt.isEmpty()) {
            throw new RuntimeException("존재하지 않는 스케줄입니다.");
        }

        ScheduledJob job = jobOpt.get();
        job.setStatus(status);
        jobRepository.save(job);

        try {
            schedulerService.updateJobStatus(job);
        } catch (SchedulerException e) {
            throw new RuntimeException("스케줄 상태 변경 실패", e);
        }

        return "상태 변경 완료";
    }
}