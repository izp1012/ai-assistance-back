package com.uf.assistance.dto.ai;

import com.uf.assistance.config.AppConfig;
import com.uf.assistance.domain.ai.CustomAI;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    private String createByUsrId;
    private String createByUsrName;
    private LocalDateTime createdTime;
    private String updateByUsrId;
    private String updateByUsrName;
    private LocalDateTime updatedTime;
    private boolean active;
    private boolean hidden;
    private boolean approved;

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
                .createByUsrId(customAI.getCreatedBy().getUserId())
                .createByUsrName(customAI.getCreatedBy().getUsername())
                .createdTime(customAI.getCreatedAt())
                .updateByUsrId(customAI.getUpdatedBy().getUserId())
                .updateByUsrName(customAI.getUpdatedBy().getUsername())
                .updatedTime(customAI.getCreatedAt())
                .active(customAI.isActive())
                .hidden(customAI.isHidden())
                .approved(customAI.isApproved())
                .build();
    }
}

