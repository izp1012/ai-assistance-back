package com.uf.assistance.web;

import com.uf.assistance.domain.ai.*;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.ai.AIRespDto;
import com.uf.assistance.dto.ai.AISubScriptionRespDto;
import com.uf.assistance.dto.user.UserRespDto;
import com.uf.assistance.service.AISubscriptionService;
import com.uf.assistance.service.UserService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth/aisubsciption")
@Tag(name = "AI", description = "AI CRUD 관련 API")
public class AISubscriptionController {

    private final UserService userService;
    private final AISubscriptionService aiSubscriptionService;

    @GetMapping("/{userId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ResponseDto<List<AIRespDto>>> getSubscribedAIsbyUserId(@PathVariable Long userId) {
        List<AI> list = aiSubscriptionService.getSubscribedAIs(userId);
        List<AIRespDto> aiRespList = new ArrayList<>();

        for(AI ai : list) {
            AIRespDto aiRespDto = AIRespDto.from(ai);
            aiRespList.add(aiRespDto);
        }

        return new ResponseEntity<>(new ResponseDto<>(1, "사용자 구독리스트 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), aiRespList), HttpStatus.OK);
    }

    @PostMapping("/subscribe/{aiId}/{userId}")
    public ResponseEntity<ResponseDto<AISubScriptionRespDto>> subscribe(@PathVariable Long aiId, @PathVariable Long userId) {

        try {
            UserRespDto userRespDto = userService.findUserById(userId);
            AISubScriptionRespDto aiSubScriptionRespDto = aiSubscriptionService.subscribe(userId, aiId);

            return new ResponseEntity<>(new ResponseDto<>(1, "AI 구독성공 -" + userRespDto.getUsername(), CustomDateUtil.toStringFormat(LocalDateTime.now()), aiSubScriptionRespDto), HttpStatus.OK);

        }catch (Exception e) {
            return new ResponseEntity<>(new ResponseDto<>(-1, "AI 구독실패 " + e.getMessage(), CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND);
        }
    }
}
