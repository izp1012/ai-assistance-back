package com.uf.assistance.config;

import com.uf.assistance.config.jwt.JwtAuthenticationFilter;
import com.uf.assistance.config.jwt.JwtAuthorizationFilter;
import com.uf.assistance.config.jwt.JwtRequestFilter;
import com.uf.assistance.config.jwt.JwtTokenProvider;
import com.uf.assistance.handler.JwtAccessDeniedHandler;
import com.uf.assistance.handler.JwtAuthenticationEntryPoint;
import com.uf.assistance.handler.OAuth2SuccessHandler;
import com.uf.assistance.service.OAuth2UserService;
import com.uf.assistance.util.CustomResponseUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2UserService oAuth2UserService;

//    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration) {
//        this.authenticationConfiguration = authenticationConfiguration;
//    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("디버그: BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    //JWT 필터 등록이 필요함
    public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            builder.addFilter(new JwtAuthenticationFilter(authenticationManager));
            builder.addFilter(new JwtAuthorizationFilter(authenticationManager));
            super.configure(builder);
        }

        public HttpSecurity build(){
            return getBuilder();
        }
    }

    // JWT 서버 생성 예정. Session 미사용
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        
        log.debug("디버그: filterChain 빈 등록됨");

        // iframe 미사용
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        // CSRF 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // CORS 설정
        http.cors(cors -> cors.configurationSource(configurationSource()));

        http
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                );

        // JSessionId를 서버에서 관리하지 않음
        // 추후 Token을 가지고 왔을 때 Authentication 세션을 강제로 만들어야함
        http.sessionManagement(sessionManagement ->
                sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        //필터 적용
        http.with(new CustomSecurityFilterManager(), CustomSecurityFilterManager::build);

        http      //jwt필터를 usernamepassword인증 전에 실행
                .addFilterBefore(new JwtRequestFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        // 인증 실패 가로채기
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    CustomResponseUtil.fail(response, "login", "로그인을 진행해 주세요", HttpStatus.UNAUTHORIZED);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    CustomResponseUtil.fail(response, "login", "권한이 없습니다.", HttpStatus.FORBIDDEN);
                })
        );

        // 폼 로그인 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);
        // HTTP Basic 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable);




        // OAuth2 설정
        http.oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(oAuth2UserService)
                )
        );

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/v3/api-docs/**", // OpenAPI JSON
                        "/api-docs/**",
                        "/swagger-ui/**",    // Swagger UI
                        "/swagger-ui.html",
                        "/api/login/**",
                        "/api/join/**",
                        "/api/image/**").permitAll()
//                .requestMatchers("/api/auth/**").permitAll()
//                .requestMatchers("/api/admin/**").hasRole(UserRole.ADMIN.name())
//                .requestMatchers("/api/admin/**").permitAll() //임시로 모든 요청 허용
                .requestMatchers("/chat/**").permitAll() //WebSocket 엔드포인트 허용
                .requestMatchers("/api/auth/**").authenticated());

        return http.build();
    }

    public CorsConfigurationSource configurationSource(){
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); //GET POST PUT DELETE (Javascript 요청 허용)
        configuration.addAllowedOriginPattern("*"); //모든 IP 주소 허용 (프론트 엔드 IP만 허용 react)
        configuration.setAllowCredentials(true); //클라이언트에서 쿠키 요청 허용
        configuration.addExposedHeader("Authorization"); //과거에는 디폴트. 브라우저에 Authorization 값을 노출 가능하게 함

        //모든 주소에 위 설정을 넣어준다
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

}
