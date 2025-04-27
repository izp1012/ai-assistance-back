package com.uf.assistance.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRole;
import com.uf.assistance.dto.user.LoginRespDto;
import com.uf.assistance.dto.user.TokenDTO;
import com.uf.assistance.dto.user.UserRespDto;
import com.uf.assistance.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.uf.assistance.dto.user.LoginReqDto;
import com.uf.assistance.util.CustomResponseUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtProvider;

    @Autowired
    private UserService userService;
    @Autowired
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtProvider) {
        super(authenticationManager);
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
        setFilterProcessesUrl("/api/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        logger.debug("디버그 : attemptAuthentication 호출됨");
        try{
            ObjectMapper om = new ObjectMapper();
            LoginReqDto loginReqDto = om.readValue(request.getInputStream(), LoginReqDto.class);

            //강제 로그인
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginReqDto.getUserId(), loginReqDto.getPassword());

            logger.debug("인증 시도 중: {}", authenticationToken);
            // UserDetailService 의 loadUserByUsername 호출
            // JWT 쓴다 하더라도, 컨트롤러 진입하면 시큐리티 권한체크, 인증체크의 도움을 받을 수 있게 세션을 만든다.
            // 이 세션의 유효기간의 request 하고, response 하면 끝
            try {
                return getAuthenticationManager().authenticate(authenticationToken);
            } catch (BadCredentialsException e) {
                logger.error("인증 실패: 잘못된 자격 증명", e);
                throw new BadCredentialsException("사용자 이름 또는 비밀번호가 잘못되었습니다");
            }

        }catch (IOException e) {
            logger.error("JSON 처리 중 오류", e);
            throw new InternalAuthenticationServiceException("요청 처리 중 오류가 발생했습니다", e);
        } catch (Exception e) {
            logger.error("인증 중 예상치 못한 오류", e);
            throw new InternalAuthenticationServiceException("인증 처리 중 오류가 발생했습니다", e);
        }
    }

    //return authtication 잘 작동하면 successfulAuthentication 메서드 호출된다.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        logger.debug("디버그 : successfulAuthentication 호출됨");

        LoginUser loginUser = (LoginUser) authResult.getPrincipal();
        String jwToken = JwtProcess.create(loginUser);
        response.addHeader(JwtVO.HEADER_STRING, jwToken);

        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser(), jwToken);
        CustomResponseUtil.success(response, "Login", "jwt-Login 성공", loginRespDto);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        logger.debug("디버그: unsuccessfulAuthentication 호출");
        logger.error(failed.getMessage());
        CustomResponseUtil.fail(response, "Login", "jwt-Login 실패", null);
    }
}
