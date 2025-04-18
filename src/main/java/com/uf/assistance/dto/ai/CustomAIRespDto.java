package com.uf.assistance.dto.ai;

import com.uf.assistance.config.AppConfig;
import com.uf.assistance.domain.ai.CustomAI;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Component
public class CustomAIRespDto {

    private Long id;
    private String name;
    private String description;
    private String basePrompt;
    private String customPrompt;
    private String imageUrl;
    private Long createByUsrId;
    private String createByUsrName;
    private LocalDateTime createdTime;
    private Long updateByUsrId;
    private String updateByUsrName;
    private LocalDateTime updatedTime;
    private boolean active;
    private boolean hidden;

    private static AppConfig appConfig;

    @Autowired
    public CustomAIRespDto(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public static CustomAIRespDto from(CustomAI customAI) {

        String fullImageUrl = customAI.getImageUrl() != null ?
                appConfig.getImageBaseUrl() + customAI.getImageUrl() :
                null;

        return CustomAIRespDto.builder()
                .id(customAI.getId())
                .name(customAI.getName())
                .description(customAI.getDescription())
                .imageUrl(fullImageUrl) // 이미지 파일명
                .basePrompt(customAI.getBaseAI().getBasePrompt())
                .customPrompt(customAI.getCustomPrompt())
                .createByUsrId(customAI.getCreatedBy().getId())
                .createByUsrName(customAI.getCreatedBy().getUsername())
                .createdTime(customAI.getCreatedAt())
                .updateByUsrId(customAI.getUpdatedBy().getId())
                .updateByUsrName(customAI.getUpdatedBy().getUsername())
                .updatedTime(customAI.getCreatedAt())
                .active(customAI.isActive())
                .hidden(customAI.isHidden())
                .build();
    }
}

