package com.uf.assistance.dto.ai;

import com.uf.assistance.config.AppConfig;
import com.uf.assistance.domain.ai.CustomAI;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private boolean active;
    private boolean hidden;
    private String imageUrl;
    private String developerName;
    private String createBy;

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
                .createBy(customAI.getCreatedBy().getUsername())
                .active(customAI.isActive())
                .hidden(customAI.isHidden())
                .build();
    }
}

