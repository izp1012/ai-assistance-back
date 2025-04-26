package com.uf.assistance.web;

import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.ai.AISubScriptionReqDto;
import com.uf.assistance.dto.ai.AISubScriptionRespDto;
import com.uf.assistance.dto.ai.CustomAIRespDto;
import com.uf.assistance.dto.user.UserRespDto;
import com.uf.assistance.service.AISubscriptionService;
import com.uf.assistance.service.UserService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/aisubsciption")
@Tag(name = "AI 구독", description = "AI 구독 CRUD 관련 API")
public class AISubscriptionController {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserService userService;
    private final AISubscriptionService aiSubscriptionService;

    @GetMapping("/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseDto<List<CustomAIRespDto>>> getSubscribedAIsbyUserId(@PathVariable String userId) {
        try {
            List<CustomAIRespDto> customAIRespDtos = aiSubscriptionService.getSubscribedAIs(userId);
            return new ResponseEntity<>(new ResponseDto<>(1, "사용자 구독리스트 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), customAIRespDtos), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("사용자 구독리스트 조회 실패: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, "사용자 구독리스트 조회 실패", CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND);
        }


    }

    @PostMapping("/subscribe")
    public ResponseEntity<ResponseDto<AISubScriptionRespDto>> subscribe(@RequestBody AISubScriptionReqDto aiSubScriptionReqDto) {

        try {
            UserRespDto userRespDto = userService.findUserById(aiSubScriptionReqDto.getUserId());
            AISubScriptionRespDto aiSubScriptionRespDto = aiSubscriptionService.subscribe(aiSubScriptionReqDto.getUserId(), aiSubScriptionReqDto.getCustomAiId());

            return new ResponseEntity<>(new ResponseDto<>(1, "AI 구독성공 -" + userRespDto.getUsername(), CustomDateUtil.toStringFormat(LocalDateTime.now()), aiSubScriptionRespDto), HttpStatus.OK);

        }catch (Exception e) {
            return new ResponseEntity<>(new ResponseDto<>(-1, "AI 구독실패 " + e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND);
        }
    }
}
