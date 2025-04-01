package com.uf.assistance.service;

import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.keyword.ChatKeyword;
import com.uf.assistance.domain.keyword.ChatKeywordRepository;
import com.uf.assistance.domain.keyword.Interest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatKeywordService {

    private final ChatKeywordRepository chatKeywordRepository;
    private final VectorInterestService vectorInterestService;

    @Autowired
    public ChatKeywordService(
            ChatKeywordRepository chatKeywordRepository,
            VectorInterestService vectorInterestService) {
        this.chatKeywordRepository = chatKeywordRepository;
        this.vectorInterestService = vectorInterestService;
    }

    /**
     * 채팅 메시지에서 키워드를 추출하고 ChatKeyword 관계를 생성
     */
    @Transactional
    public ChatKeyword processMessageAndCreateLink(Chat chat, String message) {
        // 텍스트를 분석하여 Interest 생성 또는 업데이트
        Interest interest = vectorInterestService.processTextAndSaveInterest(message);

        // ChatKeyword 생성 및 저장
        ChatKeyword chatKeyword = ChatKeyword.builder()
                .chat(chat)
                .interest(interest)
                .build();

        return chatKeywordRepository.save(chatKeyword);
    }

    /**
     * 채팅과 연결된 모든 키워드 조회
     */
    public List<ChatKeyword> findKeywordsByChat(Chat chat) {
        return chatKeywordRepository.findByChat(chat);
    }

    /**
     * 키워드와 연결된 모든 채팅 조회
     */
    public List<ChatKeyword> findChatsByInterestId(Long interestId) {
        return chatKeywordRepository.findByInterestId(interestId);
    }
}
