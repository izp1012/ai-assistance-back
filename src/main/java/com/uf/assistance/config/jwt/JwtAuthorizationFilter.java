package com.uf.assistance.config.jwt;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.uf.assistance.config.auth.LoginUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtTokenProvider jwtProvider;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtTokenProvider jwtProvider) {
        super(authenticationManager);
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String token = request.getHeader("Authorization");
        // JWT가 존재하지 않으면 필터 체인 계속 진행
        if (token == null || !token.startsWith(JwtVO.TOKEN_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // "Bearer " 제거 후 검증
            String jwtToken = token.replace(JwtVO.TOKEN_PREFIX, "").trim();
            LoginUser loginUser = JwtProcess.verify(jwtToken);

            // 인증 정보 저장 (세션처럼 활용)
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        } catch (JWTVerificationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid JWT Token");
            return;
        }

        // 필터 체인 계속 진행
        chain.doFilter(request, response);
    }
}