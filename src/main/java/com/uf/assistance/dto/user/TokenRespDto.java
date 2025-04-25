package com.uf.assistance.dto.user;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenRespDto {

    private String accessToken;
    private boolean isNewMember;
    private UserRespDto user;
}