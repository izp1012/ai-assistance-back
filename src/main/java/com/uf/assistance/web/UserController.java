package com.uf.assistance.web;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.user.*;
import com.uf.assistance.service.OAuth2UserService;
import com.uf.assistance.service.TokenService;
import com.uf.assistance.service.UserService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
@Log4j2
@Tag(name = "사용자 CRUD", description = "사용자 CRUD 관련 API")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserService userService;
    private final TokenService tokenService;
    private final OAuth2UserService oAuth2UserService;

    @PostMapping("/auth/join")
    public ResponseEntity<?> join(HttpServletRequest request, @RequestBody @Valid JoinReqDto joinReqDto, BindingResult bindingResult){
        JoinRespDto joinRespDto = userService.join(joinReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "회원가입 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), joinRespDto), HttpStatus.CREATED);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(HttpServletRequest request, HttpServletResponse response, @RequestBody @Valid LoginReqDto loginReqDto, BindingResult bindingResult){
        LoginRespDto loginRespDto = userService.login(loginReqDto, response);
        return new ResponseEntity<>(new ResponseDto<>(1, "로그인 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), loginRespDto), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login2(HttpServletRequest request, HttpServletResponse response, @RequestBody @Valid LoginReqDto loginReqDto, BindingResult bindingResult){
        TokenDTO tokenDTO = userService.login2(loginReqDto, response);

        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", tokenDTO.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(tokenDTO.getDuration())
                .path("/")
                .build();

        TokenRespDto tokenResponseDTO = TokenRespDto.builder()
                .isNewMember(false)
                .accessToken(tokenDTO.getAccessToken())
                .build();
        return new ResponseEntity<>(new ResponseDto<>(1, "로그인2 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), tokenResponseDTO), HttpStatus.OK);
    }

    @GetMapping("/get-current-member")
    public String getCurrentMember(Authentication authentication){
        logger.info("authentication.getName() : " + authentication.getName());
        User user = userService.findUserbyUsername(authentication.getName());
        return user.getUserId();
    }
}
