package com.uf.assistance.web;

import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.message.ChatMessageDto;
import com.uf.assistance.dto.message.ChatRespDto;
import com.uf.assistance.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ResponseEntity<?> sendMessage(@RequestBody @Valid ChatMessageDto chatMessageDto) {
        System.out.println("📨 받은 메시지: " + chatMessageDto.getText() + " / From : " + chatMessageDto.getSender());
        ChatRespDto chatRespDto = chatService.sendMessage(chatMessageDto);
        return new ResponseEntity<>(new ResponseDto<>(1, "로그인 성공", LocalDateTime.now(), chatMessageDto), HttpStatus.OK);
    }
}
