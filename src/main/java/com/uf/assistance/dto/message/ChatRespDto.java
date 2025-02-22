package com.uf.assistance.dto.message;

import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.util.CustomDateUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

public class ChatRespDto {
    @Getter
    @Setter
    private Long id;
    private User sender;
    private String text;
    private LocalDateTime timestamp;

    public ChatRespDto(Chat chatPersistence) {
        this.id = chatPersistence.getId();
        this.sender = chatPersistence.getSender();
        this.text = chatPersistence.getText();
        this.timestamp = LocalDateTime.now();
    }
}
