package com.uf.assistance.dto.user;

import com.uf.assistance.domain.user.User;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class JoinRespDto {
    private String userId;
    private String username;
    private String email;

    public JoinRespDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}