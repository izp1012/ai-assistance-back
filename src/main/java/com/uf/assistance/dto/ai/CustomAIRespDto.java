package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.CustomAI;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomAIRespDto {

    @Value("${file.upload-dir}")
    private static String IMAGE_BASE_URL;

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


        String fullImageUrl = customAI.getImageUrl() != null ?
                IMAGE_BASE_URL + customAI.getImageUrl() :
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

