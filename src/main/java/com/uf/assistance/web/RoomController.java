package com.uf.assistance.web;

import com.uf.assistance.domain.room.Room;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.room.RoomRespDto;
import com.uf.assistance.service.RoomService;
import com.uf.assistance.util.CustomDateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        List<Room> listRooms = roomService.getAllRooms();
        return new ResponseEntity<>(new ResponseDto<>(1, "모든 채팅방 정보조회", new CustomDateUtil().toStringFormat(LocalDateTime.now()), listRooms), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        RoomRespDto roomRespDto = RoomRespDto.fromEntity(room);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 정보조회 ID : "+room, new CustomDateUtil().toStringFormat(LocalDateTime.now()), roomRespDto), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Room room) {
        RoomRespDto roomRespDto = roomService.createRoom(room);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 생성 ID : "+room.getId(), new CustomDateUtil().toStringFormat(LocalDateTime.now()), roomRespDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        Room room = roomService.getRoomById(id);
        roomService.deleteRoom(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 삭제 ID : "+id, new CustomDateUtil().toStringFormat(LocalDateTime.now()), room), HttpStatus.OK);
    }
}