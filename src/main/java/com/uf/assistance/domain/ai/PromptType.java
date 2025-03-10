package com.uf.assistance.domain.ai;

/**
 * 프롬프트 타입 열거형
 */
public enum PromptType {
    BASE("기본 프롬프트"),
    CUSTOM("커스텀 프롬프트");

    private final String description;

    PromptType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}