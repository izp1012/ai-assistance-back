package com.uf.assistance.web;

import com.uf.assistance.domain.scheduler.ScheduledJob;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.scheduler.SchedulerReqDto;
import com.uf.assistance.dto.scheduler.SchedulerRespDto;
import com.uf.assistance.service.DynamicSchedulerService;
import com.uf.assistance.service.ScheduledJobService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/auth/schedule")
@RequiredArgsConstructor
@Tag(name ="QuartzScheduleJob Controller", description =  "쿼츠 스케줄링 관련")
public class ScheduleJobController {

    private final DynamicSchedulerService schedulerService;
    private final ScheduledJobService scheduledJobService;

    @PostMapping("/create")
    @Operation(summary = "쿼츠 스케줄러 등록", description = "크론표현식 예시 : 0 15 10 * * ?")
    public ResponseEntity<ResponseDto<SchedulerRespDto>> createJob(@RequestBody SchedulerReqDto schedulerReqDto) {
        SchedulerRespDto schedulerRespDto = scheduledJobService.createJob(schedulerReqDto);

        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 등록성공 -" + schedulerReqDto.getJobName(), CustomDateUtil.toStringFormat(LocalDateTime.now()), schedulerRespDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "스케줄 삭제 ")
    public ResponseEntity<ResponseDto<String>> deleteJob(@PathVariable Long id) {
        String resultMessage = scheduledJobService.deleteJob(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 삭제 성공 -" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "스케줄 일시 정지시키기 ")
    public ResponseEntity<ResponseDto<String>> pauseJob(@PathVariable Long id) {
        String resultMessage = scheduledJobService.pauseJob(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 일시 정지 성공 -" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }

    @PostMapping("/{id}/resume")
    @Operation(summary = "스케줄 재개시키기 ")
    public ResponseEntity<ResponseDto<String>> resumeJob(@PathVariable Long id) {
        String resultMessage = scheduledJobService.resumeJob(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 재개 성공-" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }

    @PostMapping("/{id}/status")
    @Operation(summary = "스케줄 상태 변경 ")
    public ResponseEntity<ResponseDto<String>> changeJobStatus(@PathVariable Long id, @RequestParam("status") ScheduledJob.Status status) {
        String resultMessage = scheduledJobService.changeJobStatus(id, status);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 상태 변경 성공" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }

    @GetMapping("/job")
    @Operation(summary = "사용자 ID + AISubscription 기준 스케줄러 조희")
    public ResponseEntity<ResponseDto<SchedulerRespDto>> getJobByUserAndSubscription(
            @RequestParam Long aiSubscriptionId) {

        SchedulerRespDto respDto = scheduledJobService.getJobByUserAndSubscription(aiSubscriptionId);

        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), respDto), HttpStatus.OK);
    }

    @GetMapping("/all")
    @Operation(summary = "전체 스케줄러 목록 조회")
    public ResponseEntity<ResponseDto<List<SchedulerRespDto>>> getAllJobs() {
        List<SchedulerRespDto> respDtos = scheduledJobService.getAllJobs();
        return new ResponseEntity<>(new ResponseDto<>(1, "전체 스케줄 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), respDtos), HttpStatus.OK);
    }
}