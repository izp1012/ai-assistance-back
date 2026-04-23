package com.uf.assistance.dto.chatroom;

import com.uf.assistance.domain.room.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRespDto {
    private Long id;
    private String name;
    private String chatRoomType;
    private String description;
    private LocalDateTime createdAt;

    public static ChatRoomRespDto fromEntity(ChatRoom chatRoom) {
        return ChatRoomRespDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .chatRoomType(String.valueOf(chatRoom.getType()))
                .description(chatRoom.getDescription())
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
