package com.uf.assistance.service;

import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.chat.ChatRepository;
import com.uf.assistance.domain.chat.MessageType;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.message.ChatReqDto;
import com.uf.assistance.dto.message.ChatRespDto;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ChatService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChatModel chatModel;
    private final ChatRepository chatRepository;
    private final UserService userService;
    private final AISubscriptionService aiSubscriptionService;
    private final OpenAiChatOptions openAiChatOptions;

    // 시스템 메시지 정의
    private final static String SYSTEM_INSTRUCTION = """
            당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
            사용자의 질문에 명확하고 정확하게 답변해 주세요.
            항상 예의 바르게 대응하고, 유용한 정보를 제공하세요.
            """;
    @Transactional
    public ChatRespDto sendMessageAI(ChatReqDto chatReqDto, Long aiSubscriptionId, MessageType messageType) {

        User user = userService.findUserbyUsername("GPT");

        AISubscription aiSubscription = aiSubscriptionService.getAISubScriptionById(aiSubscriptionId);

        // 메시지 준비
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_INSTRUCTION));
        messages.add(new UserMessage(chatReqDto.getContent()));

        Prompt aiPrompt = new Prompt(
                messages,
                openAiChatOptions
        );
        ChatResponse responseAI = chatModel.call(aiPrompt);

        String rtnResult = responseAI.getResult().getOutput().getText();

        Chat chat = ChatReqDto.toEntity(user, rtnResult, aiSubscription, messageType);

        Chat chatPersistence = chatRepository.save(chat);

        return new ChatRespDto(chatPersistence);
    }

    @Transactional
    public ChatRespDto sendMessage(ChatReqDto chatReqDto, Long aiSubscriptionId, MessageType messageType) {

        User user = userService.findUserbyUsername(chatReqDto.getSender());

        AISubscription aiSubscription = aiSubscriptionService.getAISubScriptionById(aiSubscriptionId);

        Chat chat = ChatReqDto.toEntity(user, chatReqDto.getContent(), aiSubscription, messageType);

        Chat chatPersistence = chatRepository.save(chat);

        return new ChatRespDto(chatPersistence);
    }

    public List<Chat> getMessagesByAiId(Long aiSubscriptionId) {
        return chatRepository.findByAiSubscriptionIdOrderByTimestamp(aiSubscriptionId);
    }

    public Page<Chat> getMessagesByAiIdWithPagination(Long aiSubscriptionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return chatRepository.findByAiSubscriptionId(aiSubscriptionId, pageable);
    }

}
