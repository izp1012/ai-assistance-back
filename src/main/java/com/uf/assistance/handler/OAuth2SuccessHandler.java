package com.uf.assistance.handler;

import com.uf.assistance.domain.user.*;
import com.uf.assistance.dto.user.LoginRespDto;
import com.uf.assistance.dto.user.TokenDTO;
import com.uf.assistance.service.TokenService;
import com.uf.assistance.util.CookieUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

import static com.uf.assistance.domain.user.HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME;

@Component
@RequiredArgsConstructor
@Log4j2
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String targetUrl = determineTargetUrl(request, response, authentication);

        LoginRespDto userDTO = userRepository.findByEmail(oAuth2User.getAttribute("email").toString())
                .map(user -> new LoginRespDto(user, request.getHeader("Authorization")))
                .orElse(saveNewUser(oAuth2User));    //orElse에 계정저장

        //소셜이 아닌 회원이 이메일로 저장했을 때
        if (!userDTO.isSocial()) {
            response.sendError(404, "해당 이메일을 가진 회원이 존재합니다.");
            clearAuthenticationAttributes(request, response);
        } else {
            TokenDTO tokenDTO = tokenService.createToken(userDTO);
            ResponseCookie refreshTokenCookie = ResponseCookie
                    .from("refresh_token", tokenDTO.getRefreshToken())
                    .httpOnly(true)
                    .secure(true)
                    .sameSite("None")
                    .maxAge(tokenDTO.getDuration())
                    .path("/")
                    .build();

            response.addHeader("Set-Cookie", refreshTokenCookie.toString());
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl).queryParam("accessToken", tokenDTO.getAccessToken()).build().toUriString();
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }


    protected LoginRespDto saveNewUser(OAuth2User oAuth2User) {

        //userId를 나중에 변경해야함
        String userId = oAuth2User.getAttribute("userId").toString().concat(oAuth2User.getAttribute("provider").toString());

        User user = User.builder()
                .provider(Provider.of(oAuth2User.getAttribute("provider").toString()))
                .social(true)
                .email(oAuth2User.getAttribute("email"))
                .username(oAuth2User.getAttribute("username"))
                .userId(userId)
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        return new LoginRespDto(user);

    }

    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUrl = CookieUtil.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        String targetUrl = redirectUrl.orElse(getDefaultTargetUrl());

        return UriComponentsBuilder.fromUriString(targetUrl).toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}