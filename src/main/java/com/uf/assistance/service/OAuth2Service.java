package com.uf.assistance.service;

import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.config.jwt.JwtProcess;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.domain.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class OAuth2Service {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

//    @Value("${app.auth.token-secret}")
//    private String tokenSecret;
//
//    @Value("${app.auth.token-expiration-msec}")
//    private long tokenExpirationMsec;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public String processGoogleToken(String authCode) {
        // Google에 토큰 요청
        String accessToken = getGoogleAccessToken(authCode);

        // 액세스 토큰으로 사용자 정보 요청
        Map<String, Object> userAttributes = getGoogleUserInfo(accessToken);

        // 사용자 정보로 회원가입 또는 로그인 처리
        User user = processUserRegistration("google", userAttributes);

        LoginUser loginUser = new LoginUser(user);
        // JWT 생성
        String token = JwtProcess.create(loginUser);

        return token;
    }

    private String getGoogleAccessToken(String authCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", authCode);
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
//        body.add("redirect_uri", "com.example.app:/oauth2callback");  // 모바일 앱의 리다이렉트 URI
        body.add("redirect_uri", "http://3.39.234.47:3000/");  // 모바일 앱의 리다이렉트 URI
        body.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://oauth2.googleapis.com/token",
                HttpMethod.POST,
                entity,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> getGoogleUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }

    private User processUserRegistration(String provider, Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String username = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        Optional<User> userOptional = userRepository.findByEmail(email);

        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.updateUser(username, picture);
        } else {
            user = User.builder()
                    .email(email)
                    .username(username)
                    .role(UserRole.USER)
                    .build();
        }

        return userRepository.save(user);
    }

//    private String createToken(User user) {
//        Date now = new Date();
//        Date expiryDate = new Date(now.getTime() + tokenExpirationMsec);
//
//        byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);
//        Key key = Keys.hmacShaKeyFor(keyBytes);
//
//        return Jwts.builder()
//                .setSubject(Long.toString(user.getId()))
//                .claim("role", user.getRoleKey())
//                .setIssuedAt(now)
//                .setExpiration(expiryDate)
//                .signWith(key, SignatureAlgorithm.HS512)
//                .compact();
//    }
//
//    public UserRespDto.LoginRespDto getUserInfoFromToken(String token) {
//        // 토큰에서 사용자 ID 추출
//        byte[] keyBytes = Decoders.BASE64.decode(tokenSecret);
//        Key key = Keys.hmacShaKeyFor(keyBytes);
//
//        String userId = Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody()
//                .getSubject();
//
//        // 사용자 정보 조회
//        User user = userRepository.findById(Long.parseLong(userId))
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        return new UserRespDto.LoginRespDto(user, token);
//    }
}