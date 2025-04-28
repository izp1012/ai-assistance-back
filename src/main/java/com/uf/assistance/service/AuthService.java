package com.uf.assistance.service;

import com.uf.assistance.dto.user.LoginReqDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public void authenticateLogin(LoginReqDto loginReqDto) {
        UsernamePasswordAuthenticationToken authenticationToken = loginReqDto.toAuthentication();
        authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }
}
