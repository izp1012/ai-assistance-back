package com.uf.assistance.dto.user;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class UserRespDto {
    private String userId;
    private String username;
    private String email;
    private List<UserRole> roles;
    private boolean social;

    public static UserRespDto from(User user) {
        return new UserRespDto(user.getUserId(), user.getUsername(), user.getEmail(), user.getRoles(), user.isSocial());
    }
}
