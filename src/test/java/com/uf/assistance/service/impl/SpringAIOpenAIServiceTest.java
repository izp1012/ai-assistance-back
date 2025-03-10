package com.uf.assistance.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
public class SpringAIOpenAIServiceTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private OpenAiChatOptions openAiChatOptions;

    @Mock
    private ChatResponse chatResponse;

    @Mock
    private Generation generation;

    @Mock
    private AssistantMessage assistantMessage;

    private SpringAIOpenAIService aiService;

    @BeforeEach
    void setUp() {
        // 명시적으로 서비스 객체 생성 (생성자 주입 방식을 사용)
        aiService = new SpringAIOpenAIService(chatModel, openAiChatOptions, "test-api-key", 4096);
    }

    @Test
    @DisplayName("프롬프트만 있을 때 응답 생성 테스트")
    void testGenerateResponseWithPromptOnly() {
        // Given
        String prompt = "Hello, AI!";
        String expectedResponse = "Hello, human!";

        // Mock ChatResponse
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn(expectedResponse);

        // When
        String response = aiService.generateResponse(prompt, null);

        // Then
        assertEquals(expectedResponse, response);

        // ArgumentCaptor 사용하여 Prompt 객체 검증
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        List<Message> messages = capturedPrompt.getInstructions();

        assertEquals(1, messages.size());
        assertTrue(messages.get(0) instanceof UserMessage);
        assertEquals(prompt, ((UserMessage) messages.get(0)).getText());
    }

    @Test
    @DisplayName("프롬프트와 컨텍스트가 모두 있을 때 응답 생성 테스트")
    void testGenerateResponseWithPromptAndContext() {
        // Given
        String prompt = "What can you tell me about this?";
        String context = "You are a helpful assistant specializing in Spring Boot.";
        String expectedResponse = "Spring Boot is a Java-based framework...";

        // Mock ChatResponse
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
        when(chatResponse.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(assistantMessage);
        when(assistantMessage.getText()).thenReturn(expectedResponse);

        // When
        String response = aiService.generateResponse(prompt, context);

        // Then
        assertEquals(expectedResponse, response);

        // 대안 1: ArgumentCaptor 사용
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt capturedPrompt = promptCaptor.getValue();
        List<Message> messages = capturedPrompt.getInstructions();

        assertEquals(2, messages.size());
        assertTrue(messages.get(0) instanceof SystemMessage);
        assertTrue(messages.get(1) instanceof UserMessage);
        assertEquals(context, ((SystemMessage) messages.get(0)).getText());
        assertEquals(prompt, ((UserMessage) messages.get(1)).getText());
    }

    @Test
    @DisplayName("API 호출 중 예외 발생 테스트")
    void testGenerateResponseException() {
        // Given
        String prompt = "Hello, AI!";
        when(chatModel.call(any(Prompt.class))).thenThrow(new RuntimeException("API Error"));

        // When
        String response = aiService.generateResponse(prompt, null);

        // Then
        assertTrue(response.contains("AI 서비스 오류"));
        assertTrue(response.contains("API Error"));
    }

    @Test
    @DisplayName("서비스 가용성 확인 테스트")
    void testIsAvailable() {
        // API 키가 있으므로 가용함
        assertTrue(aiService.isAvailable());

        // API 키가 없는 경우
        ReflectionTestUtils.setField(aiService, "apiKey", "");
        assertFalse(aiService.isAvailable());

        // API 키가 null인 경우
        ReflectionTestUtils.setField(aiService, "apiKey", null);
        assertFalse(aiService.isAvailable());

        // 원래 값으로 복원
        ReflectionTestUtils.setField(aiService, "apiKey", "test-api-key");
    }

    @Test
    @DisplayName("프로바이더 이름 확인 테스트")
    void testGetProviderName() {
        assertEquals("OpenAI (Spring AI)", aiService.getProviderName());
    }

    @Test
    @DisplayName("최대 토큰 수 확인 테스트")
    void testGetMaxTokens() {
        assertEquals(4096, aiService.getMaxTokens());
    }
}
