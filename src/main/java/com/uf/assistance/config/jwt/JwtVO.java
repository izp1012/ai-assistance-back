package com.uf.assistance.config.jwt;

public interface JwtVO {
    //HS256 대칭키  -- 이거는 절대 노출 X 환경 변수나 DB값을 쓸것
    //TO-DO 추후 변경필요
    public static final String SECRET = System.getenv("SECRET") != null
            ? System.getenv("SECRET")
            : "ERROR";

    public static final int EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 7;  //만료시간 1주일

    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String HEADER_STRING = "Authorization";
}
