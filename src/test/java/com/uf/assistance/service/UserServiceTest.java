package com.uf.assistance.service;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserEnum;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.dto.user.UserRespDto.JoinRespDto;
import com.uf.assistance.dto.user.UserReqDto.JoinReqDto;
import com.uf.assistance.dto.user.UserRespDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void join_test() throws Exception{
        // given
        JoinReqDto joinReqDto = JoinReqDto.builder()
                .username("inhyo")
                .password("1234")
                .email("inhyo@gmail.com")
                .fullname("존")
                .build();

        //stub1
        when(userRepository.findByUsername(any())).thenReturn(Optional.empty());
        //when(userRepository.findByUsername(any())).thenReturn(Optional.of(new User()));

        //stub2
        User john = User.builder()
                .id(1L)
                .username("John")
                .password("1234")
                .email("john@gmail.com")
                .fullname("존")
                .role(UserEnum.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any())).thenReturn(john);

        // when
        JoinRespDto joinRespDto = userService.join(joinReqDto);


        //then
        assertThat(joinRespDto.getId()).isEqualTo(1L);
        assertThat(joinRespDto.getUsername()).isEqualTo("John");

    }
}