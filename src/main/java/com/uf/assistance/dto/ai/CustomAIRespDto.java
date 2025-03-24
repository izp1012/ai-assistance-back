package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.CustomAI;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomAIRespDto {

    private String name;
    private String description;
    private String basePrompt;
    private String customPrompt;
    private boolean active;
    private boolean hidden;
    private String imageUrl;
    private String developerName;


    public static CustomAIRespDto from(CustomAI customAI) {
        CustomAIRespDto dto = new CustomAIRespDto();
        dto.setName(customAI.getName());
        dto.setDescription(customAI.getDescription());
        dto.setBasePrompt(customAI.getBaseAI().getBasePrompt());
        dto.setDeveloperName(customAI.getCreatedBy().getUsername());
        dto.setActive(customAI.isActive());
        dto.setHidden(customAI.isHidden());
        dto.setImageUrl(customAI.getImageUrl());
        return dto;
    }
}

