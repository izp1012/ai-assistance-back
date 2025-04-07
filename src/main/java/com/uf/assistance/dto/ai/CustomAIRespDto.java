package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.CustomAI;
import lombok.*;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    public static CustomAIRespDto from(CustomAI customAI) {
        return CustomAIRespDto.builder()
                .id(customAI.getId())
                .name(customAI.getName())
                .description(customAI.getDescription())
                .imageUrl(customAI.getImageUrl()) // 이미지 파일명
                .basePrompt(customAI.getBaseAI().getBasePrompt())
                .createBy(customAI.getCreatedBy().getUsername())
                .active(customAI.isActive())
                .hidden(customAI.isHidden())
                .build();
    }
}

