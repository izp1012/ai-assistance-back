package com.uf.assistance.service;

import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.config.jwt.JwtProcess;
import com.uf.assistance.config.jwt.JwtVO;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.domain.user.UserRole;
import com.uf.assistance.dto.user.*;
import com.uf.assistance.handler.exception.CustomApiException;
import com.uf.assistance.handler.exception.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuthService authService;

    //서비스는 DTO로 요청받고 DTO로 응답한다.
    @Transactional //트랜잭션이 메서드 시작할때, 시작되고, 종료될 때 함께 종료
    public JoinRespDto join(JoinReqDto joinReqDto) {
        // 1. 동일 유저네임 존재 검사
        Optional<User> userOptional = userRepository.findByUserId(joinReqDto.getUserId());
        if (userOptional.isPresent()) {
            //Username 중복
            throw new CustomApiException("동일한 UserID 가 존재합니다.");
        }

        // 2. 패스워드 인코딩 - 회원가입
        User user = userRepository.save(joinReqDto.toEntity(passwordEncoder));
        user.updateRole(UserRole.USER);

        // 3. dto 응답
        return new JoinRespDto(user);
    }

    public LoginRespDto login(LoginReqDto loginReqDto, HttpServletResponse response) {
        Optional<User> userOptional = userRepository.findByUsername(loginReqDto.getUsername());

        if(userOptional.isEmpty()) {
            throw new CustomApiException("사용자를 찾을 수 없습니다");
        }

        User user = userOptional.get();

        if(!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
            throw new CustomApiException("비밀번호가 일치하지 않습니다");
        }

        // JWT 생성
        LoginUser loginUser = new LoginUser(user);
        String jwtToken = JwtProcess.create(loginUser);

        // HTTP 응답 헤더에 JWT 추가
        response.addHeader(JwtVO.HEADER_STRING, JwtVO.TOKEN_PREFIX + jwtToken);

        // SecurityContextHolder 에 저장
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        return new LoginRespDto(user, jwtToken);
    }

    public TokenDTO login2(LoginReqDto loginReqDto, HttpServletResponse response) {
        Optional<User> userOptional = userRepository.findByUsername(loginReqDto.getUsername());

        if(userOptional.isEmpty()) {
            throw new CustomApiException("사용자를 찾을 수 없습니다");
        }

        User user = userOptional.get();

        if(!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
            throw new CustomApiException("비밀번호가 일치하지 않습니다");
        }

        // JWT 생성
        LoginUser loginUser = new LoginUser(user);
        String jwtToken = JwtProcess.create(loginUser);

        // HTTP 응답 헤더에 JWT 추가
        response.addHeader(JwtVO.HEADER_STRING, JwtVO.TOKEN_PREFIX + jwtToken);

        // SecurityContextHolder 에 저장
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        ;
        return tokenService.createToken(userOptional);
    }


    public User findUserbyUsername(String username) {
        Optional<User> userOptional = Optional.ofNullable(userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username)));

        return userOptional.get();
    }

    /**
     * ID로 사용자 조회
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     */
    public UserRespDto findUserById(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return UserRespDto.from(user);
    }

    public User findUserEntityById(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public User getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return findUserbyUsername(username);
        }

        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findByUserId(userId)
                .map(this::createUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("userId: " + userId + "를 데이터베이스에서 찾을 수 없습니다."));
    }

    private UserDetails createUserDetails(User user) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRoles().stream().map(UserRole::getType).collect(Collectors.joining(",")));

        return new org.springframework.security.core.userdetails.User(
                user.getUserId(),
                user.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}
