package com.uf.assistance.service;

import com.uf.assistance.config.auth.LoginUser;
import com.uf.assistance.config.jwt.JwtProcess;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.domain.user.UserRole;
import com.uf.assistance.dto.user.LoginReqDto;
import com.uf.assistance.dto.user.LoginRespDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

// Spring 관련 Bean 들이 하나도 없는 환경!!
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {


    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Spy
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        testUser = User.builder()
                .userId("1")
                .username("John")
                .password(passwordEncoder.encode("1234"))  // 비밀번호 암호화 적용
                .email("john@gmail.com")
                .role(UserRole.USER)
                .createdAt(LocalDateTime.now())
                .build();
//        userRepository.save(testUser);
    }

    @Test
    void testFindByUsername() {

        //Given
        when(userRepository.findByUsername("John")).thenReturn(Optional.of(testUser));

        List<User> users = userRepository.findAll();
        users.forEach(user -> System.out.println("DB에 저장된 유저: " + user.getUsername()));
        // When
        User foundUser = userRepository.findByUsername("John")
                .orElseThrow(() -> new RuntimeException("User not found"));  // 예외 처리 추가

        System.out.println("조회된 유저: " + foundUser.getUsername());

        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("John");
    }

    @Test
    void testFindByUsername_NotFound() {
        // When & Then: 존재하지 않는 사용자를 조회할 경우 예외가 발생해야 함
        assertThrows(RuntimeException.class, () -> {
            userRepository.findByUsername("NotExist")
                    .orElseThrow(() -> new RuntimeException("User not found"));
        });
    }

//    @Test
//    public void join_test() throws Exception{
//        // given
//        JoinReqDto joinReqDto = JoinReqDto.builder()
//                .username("inhyo")
//                .password("1234")
//                .email("inhyo@gmail.com")
//                .build();
//
//        //stub1
//        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
//
//        when(userRepository.save(any())).thenReturn(testUser);
//
//        // when
//        JoinRespDto joinRespDto = userService.join(joinReqDto);
//
//
//        //then
//        assertThat(joinRespDto.getUsername()).isEqualTo("John");
//
//    }

    @Test
    public void login_test() throws Exception {
        // given
        LoginReqDto loginReqDto = LoginReqDto.builder()
                .userId("1")
                .password("1234")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();

        try (MockedStatic<JwtProcess> jwtProcessMock = Mockito.mockStatic(JwtProcess.class)) {
            jwtProcessMock.when(() -> JwtProcess.create(any(LoginUser.class)))
                    .thenReturn("token-value");

            // 응답 헤더 설정
            response.addHeader("Authorization", "Bearer token-value");

            // 응답 본문 설정
            response.getWriter().write("Hello, Test!");

            //stub1
            when(userRepository.findByUserId("1")).thenReturn(Optional.of(testUser));
            when(bCryptPasswordEncoder.matches("1234", testUser.getPassword())).thenReturn(true);

            // When
            LoginRespDto loginRespDto = userService.login(loginReqDto, response);
            // Then
            assertNotNull(loginRespDto);
            assertEquals("John", loginRespDto.getUsername());
            assertThat(response.getHeader("Authorization")).isEqualTo("Bearer token-value");
            assertThat(response.getContentAsString()).isEqualTo("Hello, Test!");
        }


    }

//    @Test
//    public void findbyUserName_test() throws Exception {
//        JoinReqDto joinReqDto = JoinReqDto.builder()
//                .username("inhyo")
//                .password("1234")
//                .email("inhyo@gmail.com")
//                .build();
//
//        //stub1
//        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
//
//        when(userRepository.save(any())).thenReturn(testUser);
//
//        // when
//        JoinRespDto joinRespDto = userService.join(joinReqDto);
//
//
//        //then
//        assertThat(joinRespDto.getUsername()).isEqualTo("John");
//    }
}