package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.BaseAI;
import com.uf.assistance.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseAIReqDto {
    private String name;
    private String description;
    private String aiProvider;
    private String basePrompt;
    private String userId;
    private boolean active;

    public static BaseAI toEntity(BaseAIReqDto aiReqDto, User user) {

        return BaseAI.builder()
                .name(aiReqDto.getName())
                .description(aiReqDto.getDescription())
                .aiProvider(aiReqDto.getAiProvider())
                .createdBy(user)
                .updatedBy(user)
                .basePrompt(aiReqDto.getBasePrompt())
                .active(aiReqDto.isActive())
                .build();
    }
}
