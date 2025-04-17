package com.uf.assistance.dto.message;

import com.uf.assistance.config.AppConfig;
import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Component
public class ChatRespDto {
    private Long id;
    private String senderName; // User의 이름
    private String content;
    private String status;
    private String customAiName;
    private String imageUrl;
    private MessageType type;
    private LocalDateTime timestamp;

    private static AppConfig appConfig;

    @Autowired
    public ChatRespDto(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public static ChatRespDto from(Chat chat) {

        String fullImageUrl = chat.getAiSubscription().getCustomAI().getImageUrl() != null ?
                appConfig.getImageBaseUrl() + chat.getAiSubscription().getCustomAI().getImageUrl() :
                null;

        return ChatRespDto.builder()
                .id(chat.getId())
                .content(chat.getContent())
                .status(chat.getStatus().name())
                .customAiName(chat.getAiSubscription().getCustomAI().getName())
                .imageUrl(fullImageUrl)
                .timestamp(chat.getTimestamp())
                .type(chat.getType())
                .senderName(chat.getSender().getUsername())
                .build();
    }
}
