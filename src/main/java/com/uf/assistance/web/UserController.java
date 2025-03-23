package com.uf.assistance.web;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.user.UserReqDto;
import com.uf.assistance.dto.user.UserReqDto.LoginReqDto;
import com.uf.assistance.dto.user.UserReqDto.JoinReqDto;
import com.uf.assistance.dto.user.UserRespDto.LoginRespDto;
import com.uf.assistance.dto.user.UserRespDto.JoinRespDto;
import com.uf.assistance.service.UserService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
@Tag(name = "사용자 CRUD", description = "사용자 CRUD 관련 API")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final UserService userService;

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

    @GetMapping("/get-current-member")
    public Long getCurrentMember(Authentication authentication){
        logger.info("authentication.getName() : " + authentication.getName());
        User user = userService.findUserbyUsername(authentication.getName());
        return user.getId();
    }

//    @GetMapping("/loginInfo")
//    public String getJson(Authentication authentication) {
//        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
//
//        Map<String, Object> attributes = oAuth2User.getAttributes();
//
//        return attributes.toString();
//    }
}
