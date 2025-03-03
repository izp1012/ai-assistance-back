package com.uf.assistance.service;

import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.config.jwt.JwtProcess;
import com.uf.assistance.config.jwt.JwtVO;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.dto.user.UserReqDto;
import com.uf.assistance.dto.user.UserReqDto.JoinReqDto;
import com.uf.assistance.dto.user.UserRespDto.LoginRespDto;
import com.uf.assistance.dto.user.UserRespDto.JoinRespDto;
import com.uf.assistance.handler.exception.CustomApiException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    //서비스는 DTO로 요청받고 DTO로 응답한다.
    @Transactional //트랜잭션이 메서드 시작할때, 시작되고, 종료될 때 함께 종료
    public JoinRespDto join(JoinReqDto joinReqDto) {

        // 1. 동일 사용자 이메일 존재 검사
        Optional<User> userEmailOptional = userRepository.findByEmail(joinReqDto.getEmail());
        if (userEmailOptional.isPresent()) {
            //Username 중복
            throw new CustomApiException("동일한 E-Mail 이 존재합니다.");
        }

        // 2. 패스워드 인코딩 - 회원가입
        User userPersistence = userRepository.save(joinReqDto.toEntity(passwordEncoder));

        // 3. dto 응답
        return new JoinRespDto(userPersistence);
    }

    public LoginRespDto login(UserReqDto.LoginReqDto loginReqDto, HttpServletResponse response) {
        Optional<User> userOptional = userRepository.findByEmail(loginReqDto.getEmail());

        if(userOptional.isEmpty()) {
            throw new CustomApiException("사용자를 찾을 수 없습니다");
        }

        User user = userOptional.get();

        if(!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
            throw new CustomApiException("비밀번호가 일치하지 않습니다");
        }

        // JWT 생성
        LoginUser loginUser = new LoginUser(user);
        String jwtToken = JwtProcess.create(loginUser);

        // HTTP 응답 헤더에 JWT 추가
        response.addHeader(JwtVO.HEADER_STRING, JwtVO.TOKEN_PREFIX + jwtToken);

        // SecurityContextHolder 에 저장
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        return new LoginRespDto(user, jwtToken);
    }

    public User findUserbyUsername(String username) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username)));;

        User user = userOptional.get();

        return user;
    }

}
