package com.uf.assistance.dto.room;

import com.uf.assistance.domain.room.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomReqDTO {
    private String name;
    private String description;

    public static Room toEntity(RoomReqDTO dto) {

        Room room = Room.builder()
                .name(dto.name)
                .description(dto.description)
                .build();

        return room;
    }

}
