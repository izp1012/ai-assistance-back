package com.uf.assistance.web;

import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.scheduler.SchedulerReqDto;
import com.uf.assistance.dto.scheduler.SchedulerRespDto;
import com.uf.assistance.service.DynamicSchedulerService;
import com.uf.assistance.service.ScheduledJobService;
import com.uf.assistance.util.CustomDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth/schedule")
@RequiredArgsConstructor
public class ScheduleJobController {

    private final DynamicSchedulerService schedulerService;
    private final ScheduledJobService scheduledJobService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto<SchedulerRespDto>> createJob(@RequestBody SchedulerReqDto schedulerReqDto) {
        SchedulerRespDto schedulerRespDto = scheduledJobService.createJob(schedulerReqDto);

        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 등록성공 -" + schedulerReqDto.getJobName(), CustomDateUtil.toStringFormat(LocalDateTime.now()), schedulerRespDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto<String>> deleteJob(@PathVariable Long id) {
        String resultMessage = scheduledJobService.deleteJob(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 삭제 성공 -" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ResponseDto<String>> pauseJob(@PathVariable Long id) {
        String resultMessage = scheduledJobService.pauseJob(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 일시 정지 성공 -" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }

    @PostMapping("/{id}/resume")
    public ResponseEntity<ResponseDto<String>> resumeJob(@PathVariable Long id) {
        String resultMessage = scheduledJobService.resumeJob(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 재개 성공-" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<ResponseDto<String>> changeJobStatus(@PathVariable Long id, @RequestParam("status") ScheduledJob.Status status) {
        String resultMessage = scheduledJobService.changeJobStatus(id, status);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 상태 변경 성공" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }
}