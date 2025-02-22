package com.uf.assistance.dto.user;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.util.CustomDateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

public class UserRespDto {

    @Getter
    @Setter
    public static class LoginRespDto {
        private Long id;
        private String username;
        private String loginAt;

        public LoginRespDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.loginAt = CustomDateUtil.toStringFormat(user.getCreatedAt());
        }
    }


    @Getter
    @Setter
    @ToString
    public static class JoinRespDto {
        private Long id;
        private String username;
        private String fullname;

        public JoinRespDto(User user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.fullname = user.getFullname();
        }
    }
}
