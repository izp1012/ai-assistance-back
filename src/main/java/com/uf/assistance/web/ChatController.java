package com.uf.assistance.web;

import com.uf.assistance.domain.ai.AISubscriptionRepository;
import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.ChatRepository;
import com.uf.assistance.domain.chat.MessageType;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.message.ChatReqDto;
import com.uf.assistance.dto.message.ChatRespDto;
import com.uf.assistance.service.ChatService;
import com.uf.assistance.util.CustomDateUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/auth/")
@Tag(name = "채팅", description = "채팅 관련 API")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final AISubscriptionRepository aiSubscriptionRepository;
    private final ChatRepository chatRepository;

    @MessageMapping("/chat.sendMessageAI/{subscriptionId}")
    @SendTo("/topic/public/ai/{subscriptionId}")
    public ResponseEntity<?> sendMessageAI(@Payload @Valid ChatReqDto chatReqDto, @DestinationVariable Long subscriptionId) {

        if (subscriptionId == null) {
            throw new IllegalArgumentException("roomId is missing in the WebSocket request");
        }

        ChatRespDto chatRespDtoUser = chatService.sendMessage(chatReqDto, subscriptionId, MessageType.USER);
        ChatRespDto chatRespDtoAI = chatService.sendMessageAI(chatReqDto, subscriptionId, MessageType.ASSISTANT);
        System.out.println("📨AI - 받은 메시지: " + chatRespDtoAI.getContent() + " / From : " + chatReqDto.getSender());

        messagingTemplate.convertAndSend("/topic/public/ai/" + subscriptionId, chatRespDtoUser);
        messagingTemplate.convertAndSend("/topic/public/ai/" + subscriptionId, chatRespDtoAI);

        return new ResponseEntity<>(new ResponseDto<>(1, "AI 채팅 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRespDtoAI), HttpStatus.OK);
    }

    @MessageMapping("/chat.sendMessage/{subscriptionId}")
    @SendTo("/topic/public/ai/{subscriptionId}")
    public ResponseEntity<?> sendMessage(@Payload @Valid ChatReqDto chatReqDto, @DestinationVariable Long subscriptionId) {

        System.out.println("📨 받은 메시지: " + chatReqDto.getContent() + " / From : " + chatReqDto.getSender());
        ChatRespDto chatRespDto = chatService.sendMessage(chatReqDto, subscriptionId, MessageType.USER);

        messagingTemplate.convertAndSend("/topic/public/ai/" + subscriptionId, chatRespDto);

        return new ResponseEntity<>(new ResponseDto<>(1, "채팅 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRespDto), HttpStatus.OK);
    }

    @MessageMapping("/chat.addUser/{subscriptionId}")
    @SendTo("/topic/public/{subscriptionId}")
    public ResponseEntity<?> addUser(@Payload ChatReqDto chatReqDto, @DestinationVariable Long subscriptionId
            , SimpMessageHeaderAccessor headerAccessor) {
        // Add username in web socket session
        headerAccessor.getSessionAttributes().put("username", chatReqDto.getSender());
        headerAccessor.getSessionAttributes().put("subscriptionIdsubscriptionId", subscriptionId.toString());

        ChatRespDto chatRespDto = chatService.sendMessage(chatReqDto, subscriptionId, MessageType.JOIN);
        return new ResponseEntity<>(new ResponseDto<>(1, "사용자 추가", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRespDto), HttpStatus.OK);
    }

    @GetMapping("/messages/subscription/{subscriptionId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseDto<List<ChatRespDto>>> getMessages(@PathVariable Long subscriptionId) {
        List<Chat> messages = chatService.getMessagesByAiId(subscriptionId);

        // Chat 객체를 DTO로 변환
        List<ChatRespDto> chatDtos = messages.stream()
                .map(ChatRespDto::from)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new ResponseDto<>(1, "채팅 목록 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatDtos), HttpStatus.OK);
    }

    @GetMapping("/messages/latest/user/{userId}")
    @ResponseBody
    @Transactional
    @Tag(name ="사용자의 구독별 마지막 채팅 가져오기")
    public ResponseEntity<ResponseDto<List<ChatRespDto>>> getLastMessagesbyUserID(@PathVariable String userId) {

        List<Chat> messages = chatService.getLastMessagesForUser(userId);
        List<ChatRespDto> chatRespDtoList = new ArrayList<>();
        for(Chat chat : messages){
            chatRespDtoList.add(ChatRespDto.from(chat));
        }

        return new ResponseEntity<>(new ResponseDto<>(1, "사용자의 구독별 마지막 채팅 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRespDtoList), HttpStatus.OK);
    }

    @GetMapping("/messages/latest/AI/{userId}")
    @ResponseBody
    @Transactional
    @Tag(name ="사용자의 구독별 AI 의 마지막 채팅 가져오기")
    public ResponseEntity<ResponseDto<List<ChatRespDto>>> getLastAIMessagesbyUserID(@PathVariable String userId) {

        List<Chat> messages = chatService.getLastAIMessagesForUser(userId);
        List<ChatRespDto> chatRespDtoList = new ArrayList<>();
        for(Chat chat : messages){
            chatRespDtoList.add(ChatRespDto.from(chat));
        }

        return new ResponseEntity<>(new ResponseDto<>(1, "사용자의 구독별 AI 의 마지막 채팅 조회", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatRespDtoList), HttpStatus.OK);
    }

    @GetMapping("/messages/{subscriptionId}/page")
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseDto<Page<ChatRespDto>>> getMessagesWithPaginationAsc(
            @PathVariable Long subscriptionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Chat> messages = chatService.getMessagesByAiIdWithPaginationAscending(subscriptionId, page, size);

        // Chat 엔티티를 ChatRespDto로 변환
        Page<ChatRespDto> chatDtos = messages.map(ChatRespDto::from);


        return new ResponseEntity<>(new ResponseDto<>(1, "채팅 목록 페이징 조회 - 오름차순", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatDtos), HttpStatus.OK);
    }

    @GetMapping("/messages/{subscriptionId}/page/descending")
    @ResponseBody
    @Transactional
    public ResponseEntity<ResponseDto<Page<ChatRespDto>>> getMessagesWithPaginationDesc(
            @PathVariable Long subscriptionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Chat> messages = chatService.getMessagesByAiIdWithPaginationDescending(subscriptionId, page, size);

        // Chat 엔티티를 ChatRespDto로 변환
        Page<ChatRespDto> chatDtos = messages.map(ChatRespDto::from);


        return new ResponseEntity<>(new ResponseDto<>(1, "채팅 목록 페이징 조회 - 내림차순", CustomDateUtil.toStringFormat(LocalDateTime.now()), chatDtos), HttpStatus.OK);
    }
}
