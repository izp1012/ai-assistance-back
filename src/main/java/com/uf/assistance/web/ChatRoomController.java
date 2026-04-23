package com.uf.assistance.web;

import com.uf.assistance.domain.room.ChatRoom;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.chatroom.ChatRoomRespDto;
import com.uf.assistance.service.ChatRoomService;
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
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping
    public ResponseEntity<?> getAllRooms() {
        List<ChatRoom> listRooms = chatRoomService.getAllRooms();
        return new ResponseEntity<>(new ResponseDto<>(1, "모든 채팅방 정보조회", new CustomDateUtil().toStringFormat(LocalDateTime.now()), listRooms), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        ChatRoom chatRoom = chatRoomService.getRoomById(id);
        ChatRoomRespDto chatRoomRespDto = ChatRoomRespDto.fromEntity(chatRoom);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 정보조회 ID : "+chatRoom, new CustomDateUtil().toStringFormat(LocalDateTime.now()), chatRoomRespDto), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody ChatRoom room) {
        ChatRoomRespDto chatRoomRespDto = chatRoomService.createRoom(room);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 생성 ID : "+room.getId(), new CustomDateUtil().toStringFormat(LocalDateTime.now()), chatRoomRespDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        ChatRoom chatRoom = chatRoomService.getRoomById(id);
        chatRoomService.deleteRoom(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 삭제 ID : "+id, new CustomDateUtil().toStringFormat(LocalDateTime.now()), chatRoom), HttpStatus.OK);
    }
}