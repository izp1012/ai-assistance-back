package com.uf.assistance.dto.message;


import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.ChatStatus;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private String sender;
    private String text;
    private LocalDateTime timestamp;



    public ChatMessageDto(String sender, String text) {
        this.sender = sender;
        this.text = text;
    }

    public static Chat toEntity(User user, String text) {

        return Chat.builder()
                .sender(user)
                .text(text)
                .status(ChatStatus.SENT)
                .build();
    }
}
