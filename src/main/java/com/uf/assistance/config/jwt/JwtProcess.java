package com.uf.assistance.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class JwtProcess {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    //토큰 생성
    public static String create(LoginUser loginUser){

        if ("ERROR".equals(JwtVO.SECRET)) {
            throw new IllegalArgumentException("JWT 시크릿 키가 유효하지 않습니다.");
        }

        String jwtToken = JWT.create()
                .withSubject("uf")
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtVO.EXPIRATION_TIME))
                .withClaim("id", loginUser.getUser().getUserId())
                .withClaim("role", loginUser.getUser().getRoles())
                .sign(Algorithm.HMAC512(JwtVO.SECRET));

        return JwtVO.TOKEN_PREFIX + jwtToken;
    }

    // 토큰 검증
    // return 되는 LoginUser 객체를 강제로 시큐리티 세션에 직접 주입할 예쩡
    public static LoginUser verify(String token){

        if ("ERROR".equals(JwtVO.SECRET)) {
            throw new IllegalArgumentException("JWT 시크릿 키가 유효하지 않습니다.");
        }

        DecodedJWT decodeddjwt = JWT.require(Algorithm.HMAC512(JwtVO.SECRET)).build().verify(token);
        String userId = decodeddjwt.getClaim("id").toString();
        List role = Arrays.asList(decodeddjwt.getClaim("role"));
        User user = User.builder()
                .userId(userId)
                .roles(role)
                .build();
        return new LoginUser(user);
    }
//    // 토큰에서 회원 정보(이메일) 추출
//    public static String getUserEmail(String token) {
//        try {
//            Jws<Claims> claims = Jwts.parser()
//                    .build().parseSignedClaims(token, JwtVO.SECRET.getBytes());
//
//            return claims.getBody().getSubject();
//        } catch (Exception e) {
//            throw new RuntimeException("유효하지 않은 토큰입니다.", e);
//        }
//    }

}
