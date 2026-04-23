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
    public ResponseEntity<?> getAllChatRooms() {
        List<ChatRoom> listRooms = chatRoomService.getAllChatRooms();
        return new ResponseEntity<>(new ResponseDto<>(1, "모든 채팅방 정보조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), listRooms), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getChatRoomById(@PathVariable Long id) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(id);
        ChatRoomRespDto chatRoomRespDto = ChatRoomRespDto.fromEntity(chatRoom);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 정보조회 ID : "+chatRoom, CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRoomRespDto), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> createChatRoom(@RequestBody ChatRoom chatRoom) {
        ChatRoomRespDto chatRoomRespDto = chatRoomService.createChatRoom(chatRoom);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 생성 ID : "+chatRoom.getId(), CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRoomRespDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChatRoom(@PathVariable Long id) {
        ChatRoom chatRoom = chatRoomService.getChatRoomById(id);
        chatRoomService.deleteChatRoom(id);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅방 삭제 ID : "+id, CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRoom), HttpStatus.OK);
    }
}