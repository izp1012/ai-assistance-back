package com.uf.assistance.dto.user;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRole;
import com.uf.assistance.util.CustomDateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LoginRespDto {
    private String userId;
    private String username;
    private String email;
    private String loginAt;
    private String jwtToken;
    private boolean social;
    private List<UserRole> roles;

    public LoginRespDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.loginAt = CustomDateUtil.toStringFormat(user.getCreatedAt());
//        this.jwtToken = jwtToken;
        this.social = user.isSocial();
        this.roles = user.getRoles();
    }

    public LoginRespDto(User user, String jwtToken) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.loginAt = CustomDateUtil.toStringFormat(user.getCreatedAt());
        this.jwtToken = jwtToken;
        this.social = user.isSocial();
        this.roles = user.getRoles();
    }

//    public LoginRespDto from(User user) {
//        return new LoginRespDto(user.getUserId(), user.getEmail(), user.getUsername(), user.isSocial());
//    }
}