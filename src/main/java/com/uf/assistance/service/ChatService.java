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
    private final ChatKeywordService chatKeywordService;

    // 시스템 메시지 정의
    private final static String SYSTEM_INSTRUCTION = """
            당신은 친절하고 도움이 되는 AI 어시스턴트입니다.
            사용자의 질문에 명확하고 정확하게 답변해 주세요.
            항상 예의 바르게 대응하고, 유용한 정보를 제공하세요.
            """;

    @Transactional
    public ChatRespDto sendMessageAI(ChatReqDto chatReqDto, Long aiSubscriptionId, MessageType messageType) {
        logger.debug("AI 메시지 전송 시작 (aiSubscriptionId: {})", aiSubscriptionId);

        User user = userService.findUserbyUsername("GPT");
        AISubscription aiSubscription = aiSubscriptionService.getAISubScriptionById(aiSubscriptionId);

        // 시스템 메시지와 사용자 메시지 간의 키워드 중간값 처리
        String enhancedUserMessage = chatKeywordService.enhanceUserMessageWithKeywords(
                SYSTEM_INSTRUCTION, chatReqDto.getContent());

        logger.debug("원본 메시지: {}", chatReqDto.getContent());
        logger.debug("향상된 메시지: {}", enhancedUserMessage);

        // 메시지 준비
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_INSTRUCTION));
        messages.add(new UserMessage(enhancedUserMessage));  // 향상된 메시지 사용

        Prompt aiPrompt = new Prompt(
                messages,
                openAiChatOptions
        );
        ChatResponse responseAI = chatModel.call(aiPrompt);

        String rtnResult = responseAI.getResult().getOutput().getText();

        // 원본 메시지로 채팅 저장 (키워드는 내부적으로만 사용)
        Chat chat = ChatReqDto.toEntity(user, rtnResult, aiSubscription, messageType);
        Chat chatPersistence = chatRepository.save(chat);

        // 저장된 Chat과 메시지로 ChatKeyword 생성 및 처리
        chatKeywordService.processMessageAndCreateLink(chatPersistence, chatReqDto.getContent());

        return new ChatRespDto(chatPersistence);
    }

    @Transactional
    public ChatRespDto sendMessage(ChatReqDto chatReqDto, Long aiSubscriptionId, MessageType messageType) {
        logger.debug("사용자 메시지 전송 시작 (aiSubscriptionId: {})", aiSubscriptionId);

        User user = userService.findUserbyUsername(chatReqDto.getSender());
        AISubscription aiSubscription = aiSubscriptionService.getAISubScriptionById(aiSubscriptionId);

        Chat chat = ChatReqDto.toEntity(user, chatReqDto.getContent(), aiSubscription, messageType);
        Chat chatPersistence = chatRepository.save(chat);

        // 사용자 메시지에서도 키워드 추출 및 처리
        chatKeywordService.processMessageAndCreateLink(chatPersistence, chatReqDto.getContent());

        return new ChatRespDto(chatPersistence);
    }

    public List<Chat> getMessagesByAiId(Long aiSubscriptionId) {
        return chatRepository.findByAiSubscriptionIdOrderByTimestamp(aiSubscriptionId);
    }

    public List<Chat> getMessagesByuserId(Long userId) {
        return chatRepository.findBySenderIdOrderByTimestamp(userId);
    }

    public Page<Chat> getMessagesByAiIdWithPagination(Long aiSubscriptionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").ascending());
        return chatRepository.findByAiSubscriptionId(aiSubscriptionId, pageable);
    }

    public List<Chat> getLastMessagesForUser(Long userId) {
        return chatRepository.findLatestMessageByCustomAiIdAndSender(userId);
    }
}