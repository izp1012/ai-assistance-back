package com.uf.assistance.dto.user;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserRespDto {
    private String userId;
    private String username;
    private UserRole role;
    private boolean social;

    public static UserRespDto from(User user) {
        return new UserRespDto(user.getUserId(), user.getUsername(), user.getRole(), user.isSocial());
    }
}
