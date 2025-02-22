package com.uf.assistance.web;

import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.user.UserReqDto;
import com.uf.assistance.dto.user.UserReqDto.LoginReqDto;
import com.uf.assistance.dto.user.UserReqDto.JoinReqDto;
import com.uf.assistance.dto.user.UserRespDto.LoginRespDto;
import com.uf.assistance.dto.user.UserRespDto.JoinRespDto;
import com.uf.assistance.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class UserController {
    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> join(HttpServletRequest request, @RequestBody @Valid JoinReqDto joinReqDto, BindingResult bindingResult){
        JoinRespDto joinRespDto = userService.join(joinReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "회원가입 성공", LocalDateTime.now(), joinRespDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest request, @RequestBody @Valid LoginReqDto loginReqDto, BindingResult bindingResult){
        LoginRespDto loginRespDto = userService.login(loginReqDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "로그인 성공", LocalDateTime.now(), loginRespDto), HttpStatus.OK);
    }
}
