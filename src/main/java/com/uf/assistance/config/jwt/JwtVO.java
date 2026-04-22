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
        // 1순위: DB env_setting 테이블의 SECRET 키
        SECRET = environment.getProperty("SECRET");
        // 2순위: yml의 jwt.secret (로컬/개발 환경 폴백)
        if (SECRET == null) {
            SECRET = environment.getProperty("jwt.secret");
        }
        if (SECRET == null) {
            SECRET = "ERROR";
        }
    }
}