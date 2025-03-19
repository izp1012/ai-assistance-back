package com.uf.assistance.web;

import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.MessageType;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.message.ChatReqDto;
import com.uf.assistance.dto.message.ChatRespDto;
import com.uf.assistance.service.ChatService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@Tag(name = "채팅", description = "채팅 관련 API")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.sendMessageAI/{roomId}")
    @SendTo("/topic/public/ai/{roomId}")
    public ResponseEntity<?> sendMessageAI(@Payload @Valid ChatReqDto chatReqDto, @DestinationVariable Long roomId) {

        if (roomId == null) {
            throw new IllegalArgumentException("roomId is missing in the WebSocket request");
        }

        ChatRespDto chatRespDtoUser = chatService.sendMessage(chatReqDto, roomId, MessageType.ASSISTANT);
        ChatRespDto chatRespDtoAI = chatService.sendMessageAI(chatReqDto, roomId, MessageType.USER);
        System.out.println("📨AI - 받은 메시지: " + chatRespDtoAI.getContent() + " / From : " + chatReqDto.getSender());

        messagingTemplate.convertAndSend("/topic/public/ai/" + roomId, chatRespDtoUser);
        messagingTemplate.convertAndSend("/topic/public/ai/" + roomId, chatRespDtoAI);

        return new ResponseEntity<>(new ResponseDto<>(1, "AI 채팅 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRespDtoAI), HttpStatus.OK);
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/public/ai/{roomId}")
    public ResponseEntity<?> sendMessage(@Payload @Valid ChatReqDto chatReqDto, @DestinationVariable Long roomId) {

        System.out.println("📨 받은 메시지: " + chatReqDto.getContent() + " / From : " + chatReqDto.getSender());
        ChatRespDto chatRespDto = chatService.sendMessage(chatReqDto, roomId, MessageType.USER);

        messagingTemplate.convertAndSend("/topic/public/" + roomId, chatRespDto);

        return new ResponseEntity<>(new ResponseDto<>(1, "채팅 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRespDto), HttpStatus.OK);
    }

    @MessageMapping("/chat.addUser/{roomId}")
    @SendTo("/topic/public/{roomId}")
    public ResponseEntity<?> addUser(@Payload ChatReqDto chatReqDto, @DestinationVariable Long roomId
                , SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatReqDto.getSender());
        headerAccessor.getSessionAttributes().put("roomId", roomId.toString());

        ChatRespDto chatRespDto = chatService.sendMessage(chatReqDto, roomId, MessageType.JOIN);
        return new ResponseEntity<>(new ResponseDto<>(1, "사용자 추가", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRespDto), HttpStatus.OK);
    }

    @GetMapping("/api/messages/{roomId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseDto<List<ChatRespDto>>> getMessages(@PathVariable Long roomId) {
        List<Chat> messages = chatService.getMessagesByRoomId(roomId);

        // Chat 객체를 DTO로 변환
        List<ChatRespDto> chatDtos = messages.stream()
                .map(ChatRespDto::from)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new ResponseDto<>(1, "채팅 목록 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatDtos), HttpStatus.OK);
    }

    @GetMapping("/api/messages/{roomId}/page")
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseDto<Page<Chat>>> getMessagesWithPagination(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Chat> messages = chatService.getMessagesByRoomIdWithPagination(roomId, page, size);
        return new ResponseEntity<>(new ResponseDto<>(1, "채팅 목록 페이징 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), messages), HttpStatus.OK);
    }
}
