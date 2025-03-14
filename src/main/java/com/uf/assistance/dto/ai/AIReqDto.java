package com.uf.assistance.dto.ai;

import com.uf.assistance.domain.ai.AI;
import com.uf.assistance.domain.ai.PromptTemplate;
import com.uf.assistance.domain.user.User;

public class AIReqDto {
    private String name;
    private String description;
    private String aiProvider;
    private User developer;
    private PromptTemplate basePrompt;
    private String customPrompt;

    public AIReqDto() {
        super();
    }
    public AIReqDto(String name, String description, String aiProvider, User developer) {
        this.name = name;
        this.description = description;
        this.aiProvider = aiProvider;
        this.developer = developer;
    }

    public AIReqDto(String name, String description, String aiProvider, User developer, PromptTemplate basePrompt, String customPrompt) {
        this.name = name;
        this.description = description;
        this.aiProvider = aiProvider;
        this.developer = developer;
        this.basePrompt = basePrompt;
        this.customPrompt = customPrompt;
    }

    public static AI toEntity(User user, String name, String description, PromptTemplate basePrompt, String customPrompt, boolean active, boolean isPublic) {

        return AI.builder()
                .name(name)
                .description(description)
                .developer(user)
                .basePrompt(basePrompt)
                .customPrompt(customPrompt)
                .isActive(active)
                .isPublic(isPublic)
                .build();
    }
}
