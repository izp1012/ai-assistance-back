package com.uf.assistance.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.uf.assistance.dto.user.LoginRespDto;
import com.uf.assistance.dto.user.TokenDTO;
import com.uf.assistance.dto.user.TokenRespDto;
import com.uf.assistance.service.OAuth2Service;
import com.uf.assistance.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;
    private final TokenService tokenService;

    @PostMapping("/login/google")
    public ResponseEntity<?> googleLogin(@RequestParam("code") String code) {
        String jwtToken = oAuth2Service.processGoogleToken(code);

        return ResponseEntity.ok().body(jwtToken);
    }


    @Operation(summary = "구글 소셜 로그인")
    @GetMapping("/google")
    public ResponseEntity<TokenRespDto> oauth2Google(@RequestParam("id_token") String idToken) throws JsonProcessingException, ParseException {
        Map<String, Object> userMap =  oAuth2Service.findOrSaveMember(idToken, "google");
        TokenDTO tokenDTO = tokenService.createToken((LoginRespDto) userMap.get("dto"));

        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", tokenDTO.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(tokenDTO.getDuration())
                .path("/")
                .build();

        TokenRespDto tokenResponseDTO = TokenRespDto.builder()
                .isNewMember(false)
                .accessToken(tokenDTO.getAccessToken())
                .build();

        return ResponseEntity.status((Integer) userMap.get("status")).header("Set-Cookie", responseCookie.toString()).body(tokenResponseDTO);
    }
}