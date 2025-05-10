package com.uf.assistance.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.dto.user.LoginReqDto;
import com.uf.assistance.dto.user.LoginRespDto;
import com.uf.assistance.dto.user.TokenDTO;
import com.uf.assistance.service.TokenService;
import com.uf.assistance.util.CustomResponseUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private AuthenticationManager authenticationManager;
    private TokenService tokenService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, TokenService tokenService) {
        super(authenticationManager);
        this.tokenService = tokenService;
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

            return getAuthenticationManager().authenticate(authenticationToken);

        } catch (BadCredentialsException e) {
            logger.error("인증 실패: 잘못된 자격 증명", e);
            throw new BadCredentialsException("사용자 이름 또는 비밀번호가 잘못되었습니다");
        } catch (IOException e) {
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
        LoginRespDto loginRespDto = new LoginRespDto(loginUser.getUser());
        TokenDTO tokenDTO = tokenService.createToken(loginRespDto);

        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", tokenDTO.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(tokenDTO.getDuration())
                .path("/")
                .build();

        response.addHeader("Set-Cookie", responseCookie.toString()); // Set-Cookie 헤더에 쿠키 추가

        LoginRespDto loginRespDto2 = new LoginRespDto(loginUser.getUser(), tokenDTO.getAccessToken());

        CustomResponseUtil.success(response, "Login", "Login 성공", loginRespDto2);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        logger.debug("디버그: unsuccessfulAuthentication 호출");
        logger.error(failed.getMessage());
        CustomResponseUtil.fail(response, "Login", "Login 실패 - " + failed.getMessage(), HttpStatus.valueOf(response.getStatus()));
    }
}
