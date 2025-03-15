package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.AISubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AISubScriptionRespDto {
    private Long id;
    private String username;
    private AIRespDto aiRespDto;
    private LocalDateTime createdAt;

    public static AISubScriptionRespDto from(AISubscription aiSubscription) {
        return AISubScriptionRespDto.builder()
                .id(aiSubscription.getId())
                .aiRespDto(AIRespDto.from(aiSubscription.getAi()))
                .username(aiSubscription.getUser().getUsername())
                .createdAt(aiSubscription.getSubscribedAt())
                .build();
    }
}
