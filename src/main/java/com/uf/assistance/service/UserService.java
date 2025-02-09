package com.uf.assistance.service;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.dto.user.UserReqDto.JoinReqDto;
import com.uf.assistance.dto.user.UserRespDto.JoinRespDto;
import com.uf.assistance.handler.exception.CustomApiException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    //서비스는 DTO로 요청받고 DTO로 응답한다.
    @Transactional //트랜잭션이 메서드 시작할때, 시작되고, 종료될 때 함께 종료
    public JoinRespDto join(JoinReqDto joinReqDto) {
        // 1. 동일 유저네임 존재 검사
        Optional<User> userOptional = userRepository.findByUsername(joinReqDto.getUsername());
        if (userOptional.isPresent()) {
            //Username 중복
            throw new CustomApiException("동일한 Username이 존재합니다.");
        }

        // 2. 패스워드 인코딩 - 회원가입
        User userPersistence = userRepository.save(joinReqDto.toEntity(passwordEncoder));

        // 3. dto 응답
        return new JoinRespDto(userPersistence);
    }
}
