package com.uf.assistance.dto.message;

import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ChatRespDto {
    private Long id;
    private String senderName; // User의 이름
    private String content;
    private String status;
    private String customAiName;
    private MessageType type;
    private LocalDateTime timestamp;

    public ChatRespDto(Chat chatPersistence) {
        this.id = chatPersistence.getId();
        this.senderName = chatPersistence.getSender().getUsername();
        this.content = chatPersistence.getContent();
        this.type = chatPersistence.getType();
        this.timestamp = LocalDateTime.now();
    }


    public static ChatRespDto from(Chat chat) {
        ChatRespDto dto = new ChatRespDto();
        dto.setId(chat.getId());
        dto.setContent(chat.getContent());
        dto.setStatus(chat.getStatus().name());  // ChatStatus를 String으로 변환
        dto.setCustomAiName(chat.getAiSubscription().getCustomAI().getName());
        dto.setTimestamp(chat.getTimestamp());
        dto.setType(chat.getType());  // MessageType을 String으로 변환

        // User의 정보를 가져옵니다 (Lazy 로딩 문제를 피하기 위해 필요한 데이터만 로드)
        dto.setSenderName(chat.getSender().getUsername());

        return dto;
    }
}
