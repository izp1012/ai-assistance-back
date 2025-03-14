package com.uf.assistance.dto.message;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.ChatStatus;
import com.uf.assistance.domain.chat.MessageType;
import com.uf.assistance.domain.room.Room;
import com.uf.assistance.domain.room.RoomRepository;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.room.RoomReqDTO;
import com.uf.assistance.dto.room.RoomRespDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatReqDto {
    private String sender;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    private Room room;

    @JsonCreator
    public ChatReqDto(
            @JsonProperty("sender") String sender,
            @JsonProperty("content") String content,
            @JsonProperty("type") MessageType type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    public static Chat toEntity(User user, String content, Room room, MessageType Type) {

        return Chat.builder()
                .sender(user)
                .content(content)
                .Status(ChatStatus.SENT)
                .type(Type)
                .room(room)
                .build();
    }

}
