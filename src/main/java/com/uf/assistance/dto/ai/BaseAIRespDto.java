package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.BaseAI;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class BaseAIRespDto {

    private String name;
    private String description;
    private String aiProvider;
    private String basePrompt;
    private String developerName;
    private boolean active;


    public BaseAIRespDto(BaseAI baseAI) {
        this.name = baseAI.getName();
        this.description = baseAI.getDescription();
        this.aiProvider = baseAI.getAiProvider();
        this.basePrompt = baseAI.getBasePrompt();
        this.developerName = baseAI.getCreatedBy().getUsername();
        this.active = baseAI.isActive();
    }


    public static BaseAIRespDto from(BaseAI baseAI) {
        BaseAIRespDto dto = new BaseAIRespDto();
        dto.setName(baseAI.getName());
        dto.setDescription(baseAI.getDescription());
        dto.setAiProvider(baseAI.getAiProvider());
        dto.setBasePrompt(baseAI.getBasePrompt());
        dto.setDeveloperName(baseAI.getCreatedBy().getUsername());
        dto.setActive(baseAI.isActive());

        return dto;
    }
}

