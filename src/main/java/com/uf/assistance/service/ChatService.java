package com.uf.assistance.service;

import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.ChatRepository;
import com.uf.assistance.domain.room.Room;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.dto.message.ChatReqDto;
import com.uf.assistance.dto.message.ChatRespDto;
import com.uf.assistance.dto.room.RoomRespDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final RoomService roomService;

    @Transactional
    public ChatRespDto sendMessage(ChatReqDto chatReqDto, Long roomId, String type) {

        User user = userService.findUserbyUsername(chatReqDto.getSender());

        Room room = roomService.getRoomById(roomId);
        Chat chat = ChatReqDto.toEntity(user, chatReqDto.getContent(), room, type);

        Chat chatPersistence = chatRepository.save(chat);

        return new ChatRespDto(chatPersistence);
    }

    public List<Chat> getMessagesByRoomId(Long roomId) {
        return chatRepository.findByRoomIdOrderByTimestamp(roomId);
    }

    public Page<Chat> getMessagesByRoomIdWithPagination(Long roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").ascending());
        return chatRepository.findByRoomId(roomId, pageable);
    }
}
