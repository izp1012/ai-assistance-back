package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.AI;
import com.uf.assistance.domain.ai.PromptTemplate;
import com.uf.assistance.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIReqDto {
    private String name;
    private String description;
    private String aiProvider;
    private User developer;
    private PromptTemplate basePrompt;
    private String customPrompt;
    private boolean isActive;
    private boolean isPublic;

    public static AI toEntity(AIReqDto aiReqDto) {

        return AI.builder()
                .name(aiReqDto.getName())
                .description(aiReqDto.getDescription())
                .aiProvider(aiReqDto.getAiProvider())
                .developer(aiReqDto.getDeveloper())
                .basePrompt(aiReqDto.getBasePrompt())
                .customPrompt(aiReqDto.getCustomPrompt())
                .isActive(aiReqDto.isActive)
                .isPublic(aiReqDto.isPublic)
                .build();
    }
}
