package com.uf.assistance.dto.message;

import com.uf.assistance.config.AppConfig;
import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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

        ChatRespDto dto = new ChatRespDto();
        dto.setId(chat.getId());
        dto.setContent(chat.getContent());
        dto.setStatus(chat.getStatus().name());  // ChatStatus를 String으로 변환
        dto.setCustomAiName(chat.getAiSubscription().getCustomAI().getName());
        dto.setImageUrl(fullImageUrl);
        dto.setTimestamp(chat.getTimestamp());
        dto.setType(chat.getType());  // MessageType을 String으로 변환

        // User의 정보를 가져옵니다 (Lazy 로딩 문제를 피하기 위해 필요한 데이터만 로드)
        dto.setSenderName(chat.getSender().getUsername());

        return dto;
    }
}
