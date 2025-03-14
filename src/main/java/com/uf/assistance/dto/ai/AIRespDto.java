package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.AI;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
public class AIRespDto {

    private String name;
    private String description;
    private String aiProvider;
    private String developerName;
    private String basePrompt;
    private String customPrompt;

    public AIRespDto(AI ai) {
        this.name = ai.getName();
        this.description = ai.getDescription();
        this.aiProvider = ai.getAiProvider();
        this.basePrompt = ai.getBasePrompt().getTemplate();
        this.customPrompt = ai.getCustomPrompt();
        this.developerName = ai.getDeveloper().getUsername();
    }


    public static AIRespDto from(AI ai) {
        AIRespDto dto = new AIRespDto();
        dto.setName(ai.getName());
        dto.setDescription(ai.getDescription());
        dto.setAiProvider(ai.getAiProvider());
        dto.setBasePrompt(ai.getBasePrompt().getTemplate());
        dto.setCustomPrompt(ai.getCustomPrompt());
        dto.setDeveloperName(ai.getDeveloper().getUsername());

        return dto;
    }
}

