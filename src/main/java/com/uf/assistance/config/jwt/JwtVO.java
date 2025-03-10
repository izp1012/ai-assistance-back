package com.uf.assistance.config.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class JwtVO {
    // 원래 static final 상수들
    public static String SECRET;
    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;  // 만료시간 1주일
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";

    // 초기화를 위한 생성자
    @Autowired
    public JwtVO(Environment environment) {
        // SECRET 값을 Environment에서 가져옴
        SECRET = environment.getProperty("SECRET");
        if (SECRET == null) {
            SECRET = "ERROR";
        }
    }
}