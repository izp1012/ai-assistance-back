package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.BaseAI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;


@Getter
@Builder
@AllArgsConstructor
public class BaseAIRespDto {

    private Long id;
    private String name;
    private String description;
    private String aiProvider;
    private String basePrompt;
    private String createByUsrId;
    private String createByUsrName;
    private LocalDateTime createdTime;
    private String updateByUsrId;
    private String updateByUsrName;
    private LocalDateTime updatedTime;
    private boolean active;

    public static BaseAIRespDto from(BaseAI baseAI) {

        return BaseAIRespDto.builder()
                .id(baseAI.getId())
                .name(baseAI.getName())
                .description(baseAI.getDescription())
                .aiProvider(baseAI.getAiProvider())
                .basePrompt(baseAI.getBasePrompt())
                .createByUsrId(baseAI.getCreatedBy().getUserId())
                .createByUsrName(baseAI.getCreatedBy().getUsername())
                .createdTime(baseAI.getCreatedAt())
                .updateByUsrId(baseAI.getUpdatedBy().getUserId())
                .updateByUsrName(baseAI.getUpdatedBy().getUsername())
                .updatedTime(baseAI.getCreatedAt())
                .active(baseAI.isActive())
                .build();
    }
}

