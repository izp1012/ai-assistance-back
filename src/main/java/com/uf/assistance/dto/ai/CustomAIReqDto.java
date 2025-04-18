package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.BaseAI;
import com.uf.assistance.domain.ai.CustomAI;
import com.uf.assistance.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomAIReqDto {
    private Long id;
    private String name;
    private String description;
    private MultipartFile imageFile; // MultipartFile로 변경
    private String imageUrl; // 저장된 파일 경로를 저장할 필드
    private Long baseAiId;
    private String customPrompt;
    private Long userId;
    private boolean active;
    private boolean hidden;
    private boolean approved;

    public static CustomAI toEntity(CustomAIReqDto aiReqDto, BaseAI baseAI, User user, String imageUrl) {
        return CustomAI.builder()
                .name(aiReqDto.getName())
                .description(aiReqDto.getDescription())
                .customPrompt(aiReqDto.getCustomPrompt())
                .imageUrl(imageUrl)
                .createdBy(user)
                .baseAI(baseAI)
                .active(aiReqDto.isActive())
                .hidden(aiReqDto.isHidden())
                .approved(aiReqDto.isApproved())
                .build();
    }
}
