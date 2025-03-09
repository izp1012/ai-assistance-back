package com.uf.assistance.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * OpenAI API 요청 DTO
 */
public class OpenAIRequest {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatCompletion {
        private String model;
        private List<Message> messages;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        private Double temperature;

        @JsonProperty("top_p")
        private Double topP;

        @JsonProperty("frequency_penalty")
        private Double frequencyPenalty;

        @JsonProperty("presence_penalty")
        private Double presencePenalty;

        private List<String> stop;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role; // system, user, assistant
        private String content;
    }
}
