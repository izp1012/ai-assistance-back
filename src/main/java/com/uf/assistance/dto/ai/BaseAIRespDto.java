package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.BaseAI;
import lombok.*;


@Getter
@Builder
@AllArgsConstructor
public class BaseAIRespDto {

    private Long id;
    private String name;
    private String description;
    private String aiProvider;
    private String basePrompt;
    private Long createByUsrId;
    private String createByUsrName;
    private Long updateByUsrId;
    private String updateByUsrName;
    private boolean active;

    public static BaseAIRespDto from(BaseAI baseAI) {

        return BaseAIRespDto.builder()
                .id(baseAI.getId())
                .name(baseAI.getName())
                .description(baseAI.getDescription())
                .aiProvider(baseAI.getAiProvider())
                .basePrompt(baseAI.getBasePrompt())
                .createByUsrId(baseAI.getCreatedBy().getId())
                .createByUsrName(baseAI.getCreatedBy().getUsername())
                .updateByUsrId(baseAI.getUpdatedBy().getId())
                .updateByUsrName(baseAI.getUpdatedBy().getUsername())
                .active(baseAI.isActive())
                .build();
    }
}

