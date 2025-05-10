package com.uf.assistance.web;

import com.uf.assistance.domain.scheduler.Status;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.scheduler.SchedulerReqDto;
import com.uf.assistance.dto.scheduler.SchedulerRespDto;
import com.uf.assistance.handler.exception.CustomApiException;
import com.uf.assistance.service.ScheduledJobService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/auth/schedule")
@RequiredArgsConstructor
@Tag(name ="QuartzScheduleJob Controller", description =  "쿼츠 스케줄링 관련")
public class ScheduleJobController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ScheduledJobService scheduledJobService;

    @PostMapping("/create")
    @Operation(summary = "쿼츠 스케줄러 등록",
            description = "크론표현식 예시 : 0 15 10 * * ?,\n" +
                    "jobData 포맷 : { \n" +
                    "  \"prompt\": \"너는 숙련된 미국 주식 애널리스트이며, 투자자에게 유용한 정보를 제공하는 것이 목적이다. 다음 항목들을 바탕으로 사용자가 물어보는 미국 주식 종목에 대해 요약 분석해줘: 기업 개요, 최근 실적, 주요 뉴스, 주가 차트 흐름, 경쟁사 비교, 향후 투자 리스크. 초보자도 이해할 수 있도록 쉽게 설명해줘, 엔비디아와 테슬라 에 대해서 자세히설명해줘\",\n" +
                    "  \"senderName\": \"GPT\",\n" +
                    "}\n, " +
                    "job_type : SendMessageAI 로 고정" +
                    "status : ENABLED, DISABLED, PAUSED 중 하나로 입력")
    public ResponseEntity<ResponseDto<SchedulerRespDto>> createJob(@RequestBody SchedulerReqDto schedulerReqDto) {
        try {
            SchedulerRespDto schedulerRespDto = scheduledJobService.createJob(schedulerReqDto);
            return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 등록성공 -" + schedulerReqDto.getJobName(), CustomDateUtil.toStringFormat(LocalDateTime.now()), schedulerRespDto), HttpStatus.CREATED);
        } catch (CustomApiException e) {
            logger.error("Error Creating ScheduleJob execution info: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.CONFLICT);
        } catch (Exception e) {
            logger.error("Error Creating ScheduleJob execution info: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //TODO 추후 changeJobStatus 로 통합예정
    @DeleteMapping("/{id}")
    @Operation(summary = "스케줄 삭제 - 추후 changeJobStatus 로 통합예정 ")
    public ResponseEntity<ResponseDto<String>> deleteJob(@PathVariable Long id) {

        try{
            String resultMessage = scheduledJobService.deleteJob(id);
            return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 삭제 성공 -" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
        } catch (CustomApiException e){
            logger.error("Error Deleting ScheduleJob execution info: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND);
        } catch (Exception e){
            logger.error("Error Deleting ScheduleJob execution info: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //TODO 추후 changeJobStatus 로 통합예정
    @PostMapping("/pause/{id}")
    @Operation(summary = "스케줄 일시 정지시키기 - 추후 changeJobStatus 로 통합예정")
    public ResponseEntity<ResponseDto<String>> pauseJob(@PathVariable Long id) {
        try{
            String resultMessage = scheduledJobService.pauseJob(id);
            return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 일시 정지 성공 -" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
        } catch (CustomApiException e){
            logger.error("Error Pausing ScheduleJob execution info: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND);
        } catch (Exception e){
            logger.error("Error Pausing ScheduleJob execution info: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //TODO 추후 changeJobStatus 로 통합예정
    @PostMapping("/resume/{id}")
    @Operation(summary = "스케줄 재개시키기 - 추후 changeJobStatus 로 통합예정")
    public ResponseEntity<ResponseDto<String>> resumeJob(@PathVariable Long id) {
        String resultMessage = scheduledJobService.resumeJob(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 재개 성공-" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
    }

    @PostMapping("/status/{id}")
    @Operation(summary = "스케줄 상태 변경 ")
    public ResponseEntity<ResponseDto<String>> changeJobStatus(@PathVariable Long id, @RequestParam("status") Status status) {
        try {
            String resultMessage = scheduledJobService.changeJobStatus(id, status);
            return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 상태 변경 성공" + id + ", status : " + status.getDescription(), CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
        }catch (CustomApiException e){
            logger.error("Error Changing ScheduleJob execution info: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND);
        }catch (Exception e){
            logger.error("Error Changing ScheduleJob execution info: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Async
    public CompletableFuture<String> executeJobAsync(Long id, Status status) {
        try {
            String resultMessage = scheduledJobService.triggerJobNow(id, status);
            return CompletableFuture.completedFuture(resultMessage);
        } catch (CustomApiException e){
            logger.error("Error Trigger ScheduleJob execution info: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            logger.error("Error executing ScheduleJob asynchronously (ID: {}): {}", id, e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    @PostMapping("/onetime/{id}")
    @Operation(summary = "스케줄 1회 실행 ")
    public ResponseEntity<ResponseDto<String>> triggerJobNowAsync(@PathVariable Long id) {
        logger.info("비동기 작업 실행 요청 (ID: {})", id);
        CompletableFuture<String> futureResult =executeJobAsync(id, Status.ONETIME);

        try {
            String resultMessage = futureResult.get(); // 비동기 작업 완료까지 현재 스레드 블로킹
            return new ResponseEntity<>(new ResponseDto<>(1, "작업 수동 실행 요청 완료- 작업은 백그라운드에서 실행됨" + id, CustomDateUtil.toStringFormat(LocalDateTime.now()), resultMessage), HttpStatus.OK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("비동기 작업 Interrupted (ID: {}): {}", id, e.getMessage(), e);
            return new ResponseEntity<>(
                    new ResponseDto<>(-1, "작업 실행 중 오류 발생 (Interrupted): " + e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        } catch (ExecutionException e) {
            logger.error("비동기 작업 Execution Exception (ID: {}): {}", id, e.getMessage(), e);
            return new ResponseEntity<>(
                    new ResponseDto<>(-1, "작업 실행 중 오류 발생: " + e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

    }

    @GetMapping("/job")
    @Operation(summary = "사용자 ID + AISubscription 기준 스케줄러 조희")
    public ResponseEntity<ResponseDto<List<SchedulerRespDto>>> getJobByUserAndSubscription(
            @RequestParam String userId,
            @RequestParam(value = "aiSubscriptionId", required = false) Long aiSubscriptionId) {

        List<SchedulerRespDto> respDtos = scheduledJobService.getJobByUserAndSubscription(userId, aiSubscriptionId);

        return new ResponseEntity<>(new ResponseDto<>(1, "스케줄 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), respDtos), HttpStatus.OK);
    }

    @GetMapping("/all")
    @Operation(summary = "전체 스케줄러 목록 조회")
    public ResponseEntity<ResponseDto<List<SchedulerRespDto>>> getAllJobs() {
        List<SchedulerRespDto> respDtos = scheduledJobService.getAllJobs();
        return new ResponseEntity<>(new ResponseDto<>(1, "전체 스케줄 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), respDtos), HttpStatus.OK);
    }
}