package com.uf.assistance.service;

import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.ChatRepository;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserRepository;
import com.uf.assistance.dto.message.ChatMessageDto;
import com.uf.assistance.dto.message.ChatRespDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRespDto sendMessage(ChatMessageDto chatMessageDto) {

        User user = userRepository.findByUsername(chatMessageDto.getSender())
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + chatMessageDto.getText()));

        Chat chat = ChatMessageDto.toEntity(user, chatMessageDto.getText());

        Chat chatPersistence = chatRepository.save(chat);

        return new ChatRespDto(chatPersistence);
    }
}
