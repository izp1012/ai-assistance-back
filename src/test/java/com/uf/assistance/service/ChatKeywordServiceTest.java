package com.uf.assistance.service;

import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.keyword.ChatKeyword;
import com.uf.assistance.domain.keyword.ChatKeywordRepository;
import com.uf.assistance.domain.keyword.Interest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatKeywordServiceTest {

    @Mock
    private ChatKeywordRepository chatKeywordRepository;

    @Mock
    private VectorInterestService vectorInterestService;

    private ChatKeywordService chatKeywordService;

    @BeforeEach
    void setUp() {
        chatKeywordService = new ChatKeywordService(chatKeywordRepository, vectorInterestService);
    }

    @Test
    void processMessageAndCreateLink_ShouldCreateChatKeywordLink() {
        // Given
        Chat chat = new Chat();
        chat.setId(1L);

        String message = "테스트 메시지입니다.";

        Interest interest = Interest.builder()
                .id(2L)
                .keyword("테스트")
                .count(1)
                .vector(new float[]{0.1f, 0.2f, 0.3f})
                .build();

        when(vectorInterestService.processTextAndSaveInterest(message)).thenReturn(interest);

        ChatKeyword savedChatKeyword = ChatKeyword.builder()
                .id(3L)
                .chat(chat)
                .interest(interest)
                .build();

        when(chatKeywordRepository.save(any(ChatKeyword.class))).thenReturn(savedChatKeyword);

        // When
        ChatKeyword result = chatKeywordService.processMessageAndCreateLink(chat, message);

        // Then
        assertNotNull(result);
        assertEquals(chat, result.getChat());
        assertEquals(interest, result.getInterest());

        // VectorInterestService 호출 검증
        verify(vectorInterestService).processTextAndSaveInterest(message);

        // ChatKeyword 저장 검증
        ArgumentCaptor<ChatKeyword> chatKeywordCaptor = ArgumentCaptor.forClass(ChatKeyword.class);
        verify(chatKeywordRepository).save(chatKeywordCaptor.capture());
        assertEquals(chat, chatKeywordCaptor.getValue().getChat());
        assertEquals(interest, chatKeywordCaptor.getValue().getInterest());
    }

    @Test
    void findKeywordsByChat_ShouldReturnChatKeywords() {
        // Given
        Chat chat = new Chat();
        chat.setId(1L);

        Interest interest1 = Interest.builder()
                .id(2L)
                .keyword("테스트1")
                .build();

        Interest interest2 = Interest.builder()
                .id(3L)
                .keyword("테스트2")
                .build();

        List<ChatKeyword> expectedChatKeywords = List.of(
                ChatKeyword.builder()
                        .id(4L)
                        .chat(chat)
                        .interest(interest1)
                        .build(),
                ChatKeyword.builder()
                        .id(5L)
                        .chat(chat)
                        .interest(interest2)
                        .build()
        );

        when(chatKeywordRepository.findByChat(chat)).thenReturn(expectedChatKeywords);

        // When
        List<ChatKeyword> result = chatKeywordService.findKeywordsByChat(chat);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(interest1, result.get(0).getInterest());
        assertEquals(interest2, result.get(1).getInterest());
        verify(chatKeywordRepository).findByChat(chat);
    }

    @Test
    void findChatsByInterestId_ShouldReturnChatKeywords() {
        // Given
        Long interestId = 2L;

        Chat chat1 = new Chat();
        chat1.setId(1L);

        Chat chat2 = new Chat();
        chat2.setId(3L);

        Interest interest = Interest.builder()
                .id(interestId)
                .keyword("테스트")
                .build();

        List<ChatKeyword> expectedChatKeywords = List.of(
                ChatKeyword.builder()
                        .id(4L)
                        .chat(chat1)
                        .interest(interest)
                        .build(),
                ChatKeyword.builder()
                        .id(5L)
                        .chat(chat2)
                        .interest(interest)
                        .build()
        );

        when(chatKeywordRepository.findByInterestId(interestId)).thenReturn(expectedChatKeywords);

        // When
        List<ChatKeyword> result = chatKeywordService.findChatsByInterestId(interestId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(chat1, result.get(0).getChat());
        assertEquals(chat2, result.get(1).getChat());
        verify(chatKeywordRepository).findByInterestId(interestId);
    }
}