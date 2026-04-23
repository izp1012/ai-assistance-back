package com.uf.assistance.service;

import com.uf.assistance.domain.room.ChatRoom;
import com.uf.assistance.domain.room.ChatRoomRepository;

import com.uf.assistance.dto.chatroom.ChatRoomRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ChatRoom getRoomById(Long id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ChatRoom not found with id: " + id));
    }

    @Transactional
    public ChatRoomRespDto createRoom(ChatRoom chatRoom) {
        return ChatRoomRespDto.fromEntity(chatRoomRepository.save(chatRoom));
    }

    @Transactional
    public void deleteRoom(Long id) {
        chatRoomRepository.deleteById(id);
    }

}