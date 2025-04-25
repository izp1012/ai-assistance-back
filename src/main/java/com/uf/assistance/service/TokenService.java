package com.uf.assistance.service;

import com.uf.assistance.config.jwt.JwtTokenProvider;
import com.uf.assistance.domain.token.RefreshToken;
import com.uf.assistance.domain.token.RefreshTokenRepository;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.dto.user.LoginRespDto;
import com.uf.assistance.dto.user.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public TokenDTO createToken(LoginRespDto loginRespDto) {
        TokenDTO tokenDTO = tokenProvider.createTokenReqDto(loginRespDto.getUserId(), loginRespDto.getRoles());
        User user = userRepository.findByUserId(loginRespDto.getUserId()).orElseThrow(() -> new RuntimeException("Wrong Access (user does not exist)"));

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenDTO.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenDTO;
    }

    public TokenDTO createToken(User user) {
        TokenDTO tokenDTO = tokenProvider.createTokenReqDto(user.getUserId(), user.getRoles());
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenDTO.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        return tokenDTO;
    }

    public TokenDTO refresh(TokenDTO tokenDTO) {
        if(!tokenProvider.validateToken(tokenDTO.getRefreshToken())) {
            throw new RuntimeException("Refresh Token이 유효하지 않습니다.");
        }

        Authentication authentication = tokenProvider.getAuthentication(tokenDTO.getAccessToken());

        RefreshToken refreshToken = refreshTokenRepository.findByUser(userRepository.findByUserId(authentication.getName()).get())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        if (!refreshToken.getToken().equals(tokenDTO.getRefreshToken())) {
            throw new RuntimeException("Refresh Token이 일치하지 않습니다.");
        }

        User user = userRepository.findByUserId(refreshToken.getUser().getUserId()).orElseThrow(() -> new RuntimeException("존재하지 않는 계정입니다."));
        TokenDTO tokenDto = tokenProvider.createTokenReqDto(user.getUserId(), user.getRoles());

        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        return tokenDto;
    }
}
