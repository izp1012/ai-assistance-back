package com.uf.assistance.dto.chatroom;

import com.uf.assistance.domain.room.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomReqDTO {
    private String name;
    private String description;

    public static ChatRoom toEntity(ChatRoomReqDTO dto) {

        ChatRoom chatRoom = ChatRoom.builder()
                .name(dto.name)
                .description(dto.description)
                .build();

        return chatRoom;
    }

}
