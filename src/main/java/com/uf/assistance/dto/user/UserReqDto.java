package com.uf.assistance.dto.user;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserReqDto {

    @Getter
    @Builder
    public static class LoginReqDto {
        private String username;
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinReqDto {

        //영문 숫자 길이 2~20
        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "영문/숫자 2~20자 이내로 작성해주세요.")
        @NotEmpty // null or 공백일 수 없다
        private String username;

        //길이 4~20
        @Size(min = 4, max = 20)
        @NotEmpty
        private String password;

        //이메일형식
        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}@[a-zA-Z0-9]{2,20}\\.[a-zA-Z]{2,3}", message = "이메일 형식으로 작성해주세요.")
        @NotEmpty
        private String email;

        //영어 한글 2~20
        @Pattern(regexp = "^[a-zA-Z가-힣0-9]{2,20}$", message = "한글/영문/숫자 2~20자로 작성해주세요.")
        @NotEmpty
        private String fullname;

        public User toEntity(BCryptPasswordEncoder passwordEncoder) {
            return User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .fullname(fullname)
                    .role(UserEnum.CUSTOMER)
                    .build();
        }
    }


}
