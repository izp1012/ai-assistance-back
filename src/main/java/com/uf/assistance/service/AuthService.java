package com.uf.assistance.service;

import com.uf.assistance.dto.user.LoginReqDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public void authenticateLogin(LoginReqDto loginReqDto) {
        System.out.println("loginReqDto.getUserID = "+loginReqDto.getUserId());
        System.out.println("loginReqDto.getPassword = "+loginReqDto.getPassword());

        UsernamePasswordAuthenticationToken authenticationToken = loginReqDto.toAuthentication();
        authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }
}
