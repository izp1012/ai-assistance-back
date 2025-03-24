package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.BaseAI;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomAIReqDto {
    private String name;
    private String description;
    private String imageUrl;
    private Long baseAiId;
    private String customPrompt;
    private Long userId;
    private boolean active;
    private boolean hidden;

    public static CustomAI toEntity(CustomAIReqDto aiReqDto, BaseAI baseAI, User user) {

        return CustomAI.builder()
                .name(aiReqDto.getName())
                .description(aiReqDto.getDescription())
                .customPrompt(aiReqDto.getCustomPrompt())
                .imageUrl(aiReqDto.getImageUrl())
                .createdBy(user)
                .baseAI(baseAI)
                .active(aiReqDto.isActive())
                .hidden(aiReqDto.isHidden())
                .build();
    }
}
