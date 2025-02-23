package com.uf.assistance.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserEnum;

import java.util.Date;

public class JwtProcess {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    //토큰 생성
    public static String create(LoginUser loginUser){
        String jwtToken = JWT.create()
                .withSubject("uf")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtVO.EXPIRATION_TIME))
                .withClaim("id", loginUser.getUser().getId())
                .withClaim("role", loginUser.getUser().getRole()+"")
                .sign(Algorithm.HMAC512(JwtVO.SECRET));

        return JwtVO.TOKEN_PREFIX + jwtToken;
    }

    // 토큰 검증
    // return 되는 LoginUser 객체를 강제로 시큐리티 세션에 직접 주입할 예쩡
    public static LoginUser verify(String token){
        DecodedJWT decodeddjwt = JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);
        Long id = decodeddjwt.getClaim("id").asLong();
        String role = decodeddjwt.getClaim("role").asString();
        User user = User.builder()
                .id(id)
                .role(UserEnum.valueOf(role))
                .build();
        LoginUser loginUser = new LoginUser(user);
        return loginUser;
    }
}
