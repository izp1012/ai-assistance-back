package com.uf.assistance.web;

import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.user.*;
import com.uf.assistance.service.UserService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
@Slf4j
@Tag(name = "사용자 CRUD", description = "사용자 CRUD 관련 API")
public class UserController {
    private final UserService userService;

    @PostMapping("/auth/join")
    public ResponseEntity<?> join(@RequestBody @Valid JoinReqDto joinReqDto){
        JoinRespDto joinRespDto = userService.join(joinReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "회원가입 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), joinRespDto), HttpStatus.CREATED);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(HttpServletResponse response, @RequestBody @Valid LoginReqDto loginReqDto){
        LoginRespDto loginRespDto = userService.login(loginReqDto, response);
        return new ResponseEntity<>(new ResponseDto<>(1, "로그인 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), loginRespDto), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login2(HttpServletResponse response, @RequestBody @Valid LoginReqDto loginReqDto){
        TokenDTO tokenDTO = userService.login2(loginReqDto, response);

        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", tokenDTO.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(tokenDTO.getDuration())
                .path("/")
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString()); // Set-Cookie 헤더에 쿠키 추가

        TokenRespDto tokenResponseDTO = TokenRespDto.builder()
                .isNewMember(false)
                .accessToken(tokenDTO.getAccessToken())
                .build();
        return new ResponseEntity<>(new ResponseDto<>(1, "로그인2 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), tokenResponseDTO), HttpStatus.OK);
    }

    @GetMapping("/auth/get-current-member")
    public UserRespDto getCurrentMember(Authentication authentication){
        String userId = ((LoginUser) authentication.getPrincipal()).getUser().getUserId();
        log.debug("authentication.getName() : " + userId);
        return userService.findUserById(userId);
    }
}
