package com.uf.assistance.dto.room;

import com.uf.assistance.domain.room.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomRespDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;

    public static RoomRespDto fromEntity(Room room) {
        return RoomRespDto.builder()
                .id(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
