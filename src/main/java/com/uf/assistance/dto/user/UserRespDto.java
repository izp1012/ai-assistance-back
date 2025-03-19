package com.uf.assistance.dto.user;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.util.CustomDateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@AllArgsConstructor
public class UserRespDto {

    private Long id;
    private String username;

    public static UserRespDto from(User user) {
        return new UserRespDto(user.getId(), user.getUsername());
    }

    @Getter
    @Setter
    public static class LoginRespDto {
        private Long id;
        private String username;
        private String loginAt;
        private String jwtToken;

        public LoginRespDto(User user, String jwtToken) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.loginAt = CustomDateUtil.toStringFormat(user.getCreatedAt());
            this.jwtToken = jwtToken;
        }
    }


    @Getter
    @Setter
    @ToString
    public static class JoinRespDto {
        private Long id;
        private String username;
        private String email;

        public JoinRespDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
    }
}
