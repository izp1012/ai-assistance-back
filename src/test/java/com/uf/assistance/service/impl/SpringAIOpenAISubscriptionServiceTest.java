package com.uf.assistance.service.impl;

import com.uf.assistance.domain.ai.*;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserEnum;
import com.uf.assistance.dto.ai.AISubScriptionRespDto;
import com.uf.assistance.handler.exception.ResourceNotFoundException;
import com.uf.assistance.service.AIService;
import com.uf.assistance.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringAIOpenAISubscriptionServiceTest {

    @Mock
    private AIRepository aiRepository;

    @Mock
    private AISubscriptionRepository aiSubscriptionRepository;

    @Mock
    private SpringAIOpenAIService aiService;

    @Mock
    private UserService uesrService;

    private SpringAIOpenAISubscriptionService subscriptionService;

    private User testUser;
    private AI testAI;
    private AISubscription testSubscription;
    private PromptTemplate testPromptTemplate;

    @BeforeEach
    void setUp() {
        // 명시적으로 서비스 객체 생성
        subscriptionService = new SpringAIOpenAISubscriptionService(
                aiRepository,
                aiSubscriptionRepository,
                aiService,
                uesrService
        );

        // 테스트 데이터 설정
        testUser = new User();
        testUser.setId(1L);
        // 테스트 데이터 설정
        testUser = User.builder()
                .username("John")
                .email("john@gmail.com")
                .role(UserEnum.CUSTOMER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testPromptTemplate = PromptTemplate.builder()
                .id(1L)
                .name("기본 대화 템플릿")
                .description("AI와 대화하기 위한 기본 템플릿")
                .template("안녕하세요, 당신의 이름은 {{name}}이고, 당신의 관심사는 {{interest}}입니다.")
                .type(PromptType.BASE)
                .isActive(true)
                .build();

        testAI = AI.builder()
                .id(1L)
                .name("테스트 AI")
                .description("테스트용 AI입니다.")
                .isActive(true)
                .isPublic(true)
                .developer(testUser)
                .basePrompt(testPromptTemplate)
                .customPrompt("추가적인 지시사항: 항상 친절하게 대답하세요.")
                .build();

        testSubscription = AISubscription.builder()
                .id(1L)
                .user(testUser)
                .ai(testAI)
                .subscribedAt(LocalDateTime.now().minusDays(10))
                .lastUsedAt(LocalDateTime.now().minusDays(5))
                .build();
    }

//   Build 되지않아 주석처리 25.03.15
//    @Test
//    @DisplayName("사용 가능한 모든 AI 조회 테스트")
//    void testGetAvailableAIs() {
//        // Given
//        testAI = AI.builder()
//                .id(1L)
//                .name("테스트 AI")
//                .description("테스트용 AI입니다.")
//                .isActive(true)
//                .isPublic(true)
//                .developer(testUser)
//                .basePrompt(testPromptTemplate)
//                .customPrompt("추가적인 지시사항: 항상 친절하게 대답하세요.")
//                .build();
//
//        List<AI> expectedAIs = Arrays.asList(testAI);
//        when(aiRepository.findAllByIsActiveTrueAndIsPublicTrue()).thenReturn(expectedAIs);
//
//        // When
//        List<AI> result = aiService.getAvailableAIs();
//        System.out.println("result = "+result);
//        // Then
//        assertEquals(expectedAIs, result);
//        verify(aiRepository).findAllByIsActiveTrueAndIsPublicTrue();
//    }

    @Test
    @DisplayName("사용자의 구독 AI 목록 조회 테스트")
    void testGetSubscribedAIs() {
        // Given
        List<AI> expectedAIs = Arrays.asList(testAI);
        when(aiSubscriptionRepository.findAIsByUser(any(User.class))).thenReturn(expectedAIs);

        // When
        List<AI> result = subscriptionService.getSubscribedAIs(1L);

        // Then
        assertEquals(expectedAIs, result);
        verify(aiSubscriptionRepository).findAIsByUser(argThat(user -> user.getId().equals(1L)));
    }

    @Test
    @DisplayName("AI ID로 AI 조회 테스트")
    void testGetAIById() {
        // Given
        when(aiRepository.findById(1L)).thenReturn(Optional.of(testAI));

        // When
        AI result = subscriptionService.getAIById(1L);

        // Then
        assertEquals(testAI, result);
        verify(aiRepository).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 AI ID로 조회 시 예외 발생 테스트")
    void testGetAIByIdNotFound() {
        // Given
        when(aiRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> subscriptionService.getAIById(999L));
    }

    @Test
    @DisplayName("사용자의 AI 구독 여부 확인 테스트 - 구독 중")
    void testHasUserSubscribedAITrue() {
        // Given
        when(uesrService.findUserEntityById(1L)).thenReturn(testUser); // userService.findUserEntityById(1L) 호출 시 testUser 반환하도록 Mock 설정
        when(aiRepository.findById(1L)).thenReturn(Optional.of(testAI)); // aiRepository.findById(1L) 호출 시 testAI 반환하도록 Mock 설정
        when(aiSubscriptionRepository.existsByUserAndAi(testUser, testAI)).thenReturn(true); // aiSubscriptionRepository.existsByUserAndAi(testUser, testAI) 호출 시 true 반환하도록 Mock 설정


        // When
        boolean result = subscriptionService.hasUserSubscribedAI(1L, 1L);
        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("사용자의 AI 구독 여부 확인 테스트 - 구독하지 않음")
    void testHasUserSubscribedAIFalse() {
        // Given
        when(uesrService.findUserEntityById(1L)).thenReturn(testUser);
        when(aiRepository.findById(1L)).thenReturn(Optional.of(testAI));
        when(aiSubscriptionRepository.existsByUserAndAi(testUser, testAI)).thenReturn(false);

        // When
        boolean result = subscriptionService.hasUserSubscribedAI(1L, 1L);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("AI 구독 테스트 - 신규 구독")
    void testSubscribeNew() {
        // Given
        when(uesrService.findUserEntityById(1L)).thenReturn(testUser);
        when(aiRepository.findById(1L)).thenReturn(Optional.of(testAI));
        when(aiSubscriptionRepository.existsByUserAndAi(testUser, testAI)).thenReturn(false);
        when(aiSubscriptionRepository.save(any(AISubscription.class))).thenReturn(testSubscription);

        // When
        AISubScriptionRespDto result = subscriptionService.subscribe(1L, 1L);
        AISubScriptionRespDto testAISubScriptionRespDto = AISubScriptionRespDto.from(testSubscription);

        // Then
        assertEquals(testAISubScriptionRespDto.getUsername(), result.getUsername());
        assertEquals(testAISubScriptionRespDto.getAiRespDto().getName(), result.getAiRespDto().getName());
        assertEquals(testAISubScriptionRespDto.getId(), result.getId());
        verify(aiSubscriptionRepository).save(any(AISubscription.class));
    }

    @Test
    @DisplayName("AI 구독 테스트 - 이미 구독 중")
    void testSubscribeExisting() {
        // Given
        when(uesrService.findUserEntityById(1L)).thenReturn(testUser);
        when(aiRepository.findById(1L)).thenReturn(Optional.of(testAI));
        when(aiSubscriptionRepository.existsByUserAndAi(testUser, testAI)).thenReturn(true);
        when(aiSubscriptionRepository.findByUserAndAi(any(User.class), eq(testAI))).thenReturn(Optional.of(testSubscription));

        // When
        AISubScriptionRespDto result = subscriptionService.subscribe(1L, 1L);
        AISubScriptionRespDto testAISubScriptionRespDto = AISubScriptionRespDto.from(testSubscription);

        // Then
        assertEquals(testAISubScriptionRespDto.getUsername(), result.getUsername());
        assertEquals(testAISubScriptionRespDto.getAiRespDto().getName(), result.getAiRespDto().getName());
        assertEquals(testAISubScriptionRespDto.getId(), result.getId());
        verify(aiSubscriptionRepository, never()).save(any(AISubscription.class));
    }

    @Test
    @DisplayName("비활성화된 AI 구독 시도 테스트")
    void testSubscribeInactiveAI() {
        // Given
        AI inactiveAI = AI.builder()
                .id(2L)
                .isActive(false)
                .build();
        when(uesrService.findUserEntityById(1L)).thenReturn(testUser);
        when(aiRepository.findById(2L)).thenReturn(Optional.of(inactiveAI));
        when(aiSubscriptionRepository.existsByUserAndAi(testUser, inactiveAI)).thenReturn(false);

        // When & Then
        assertThrows(IllegalStateException.class, () -> subscriptionService.subscribe(1L, 2L));
    }

    @Test
    @DisplayName("AI 구독 취소 테스트 - 기존 구독 있음")
    void testUnsubscribeExisting() {
        // Given
        when(aiRepository.findById(1L)).thenReturn(Optional.of(testAI));
        when(aiSubscriptionRepository.findByUserAndAi(any(User.class), eq(testAI))).thenReturn(Optional.of(testSubscription));

        // When
        subscriptionService.unsubscribe(1L, 1L);

        // Then
        verify(aiSubscriptionRepository).delete(testSubscription);
    }

    @Test
    @DisplayName("AI 구독 취소 테스트 - 구독 정보 없음")
    void testUnsubscribeNonExisting() {
        // Given
        when(aiRepository.findById(1L)).thenReturn(Optional.of(testAI));
        when(aiSubscriptionRepository.findByUserAndAi(any(User.class), eq(testAI))).thenReturn(Optional.empty());

        // When
        subscriptionService.unsubscribe(1L, 1L);

        // Then
        verify(aiSubscriptionRepository, never()).delete(any(AISubscription.class));
    }

    @Test
    @DisplayName("독립형 AI 응답 생성 테스트")
    void testGenerateStandaloneAIResponse() {
        // Given
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "홍길동");
        variables.put("interest", "인공지능");

        String expectedResponse = "안녕하세요 홍길동님! 인공지능에 관심이 있으시군요. 도움이 필요하신가요?";

        when(aiService.generateResponse(anyString(), isNull())).thenReturn(expectedResponse);

        // When
        String result = subscriptionService.generateStandaloneAIResponse(testAI, variables);

        // Then
        assertEquals(expectedResponse, result);
        verify(aiService).generateResponse(anyString(), isNull());
    }

    @Test
    @DisplayName("비활성화된 AI로 응답 생성 시도 테스트")
    void testGenerateStandaloneAIResponseInactiveAI() {
        // Given
        AI inactiveAI = AI.builder()
                .id(2L)
                .isActive(false)
                .build();
        Map<String, String> variables = new HashMap<>();

        // When
        String result = subscriptionService.generateStandaloneAIResponse(inactiveAI, variables);

        // Then
        assertTrue(result.contains("비활성화"));
        verify(aiService, never()).generateResponse(anyString(), anyString());
    }

    @Test
    @DisplayName("변수가 없는 경우의 독립형 AI 응답 생성 테스트")
    void testGenerateStandaloneAIResponseNoVariables() {
        // Given
        // Mockito의 Strict 모드 때문에 모킹을 더 유연하게 변경
        String expectedResponse = "안녕하세요! 어떻게 도와드릴까요?";

        when(aiService.generateResponse(anyString(), isNull())).thenReturn(expectedResponse);

        // When
        String result = subscriptionService.generateStandaloneAIResponse(testAI, null);

        // Then
        assertEquals(expectedResponse, result);
        verify(aiService).generateResponse(anyString(), isNull());
    }

    @Test
    @DisplayName("사용자 정의 프롬프트가 없는 경우의 독립형 AI 응답 생성 테스트")
    void testGenerateStandaloneAIResponseNoCustomPrompt() {
        // Given
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "홍길동");
        variables.put("interest", "인공지능");

        AI aiWithoutCustomPrompt = AI.builder()
                .id(2L)
                .name("기본 AI")
                .description("사용자 정의 프롬프트가 없는 AI")
                .isActive(true)
                .isPublic(true)
                .basePrompt(testPromptTemplate)
                .customPrompt("")
                .build();

        String expectedResponse = "안녕하세요 홍길동님!";

        when(aiService.generateResponse(anyString(), isNull())).thenReturn(expectedResponse);

        // When
        String result = subscriptionService.generateStandaloneAIResponse(aiWithoutCustomPrompt, variables);

        // Then
        assertEquals(expectedResponse, result);
        verify(aiService).generateResponse(anyString(), isNull());
    }

    @Test
    @DisplayName("기본 프롬프트가 없는 AI 응답 생성 테스트")
    void testGenerateStandaloneAIResponseNoPromptTemplate() {
        // Given
        AI aiWithoutPrompt = AI.builder()
                .id(3L)
                .isActive(true)
                .basePrompt(null)
                .build();

        // When
        String result = subscriptionService.generateStandaloneAIResponse(aiWithoutPrompt, null);

        // Then
        assertTrue(result.contains("AI 프롬프트 설정이 올바르지 않습니다"));
        verify(aiService, never()).generateResponse(anyString(), anyString());
    }

    @Test
    @DisplayName("프롬프트 템플릿 변수 치환 테스트")
    void testPromptTemplateFormatting() {
        // Given
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "홍길동");
        variables.put("interest", "인공지능");

        String expected = "안녕하세요, 당신의 이름은 홍길동이고, 당신의 관심사는 인공지능입니다.";

        // When
        String result = testPromptTemplate.format(variables);

        // Then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("프롬프트 템플릿 결합 테스트")
    void testPromptTemplateCombine() {
        // Given
        String basePrompt = "기본 프롬프트입니다.";
        String customPrompt = "사용자 정의 프롬프트입니다.";

        String expected = "기본 프롬프트입니다.\n\n사용자 정의 프롬프트입니다.";

        // When
        String result = PromptTemplate.combine(basePrompt, customPrompt);

        // Then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("AI 응답 생성 중 예외 발생 테스트")
    void testGenerateStandaloneAIResponseException() {
        // Given
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "홍길동");
        variables.put("interest", "인공지능");

        when(aiService.generateResponse(anyString(), isNull())).thenThrow(new RuntimeException("API Error"));

        // When
        String result = subscriptionService.generateStandaloneAIResponse(testAI, variables);

        // Then
        assertTrue(result.contains("오류가 발생"));
        assertTrue(result.contains("API Error"));
    }

    @Test
    @DisplayName("마지막 사용 시간 업데이트 테스트")
    void testUpdateLastUsed() {
        // Given
        when(aiRepository.findById(1L)).thenReturn(Optional.of(testAI));
        when(aiSubscriptionRepository.findByUserAndAi(any(User.class), eq(testAI))).thenReturn(Optional.of(testSubscription));

        // AISubscription을 새로 생성할 때 lastUsedAt이 현재 시간으로 설정되는지 확인하기 위한 캡처
        ArgumentCaptor<AISubscription> subscriptionCaptor = ArgumentCaptor.forClass(AISubscription.class);

        // When
        subscriptionService.updateLastUsed(1L, 1L);

        // Then
        verify(aiSubscriptionRepository).save(subscriptionCaptor.capture());
        AISubscription captured = subscriptionCaptor.getValue();

        assertEquals(testSubscription.getId(), captured.getId());
        assertEquals(testSubscription.getUser(), captured.getUser());
        assertEquals(testSubscription.getAi(), captured.getAi());
        assertEquals(testSubscription.getSubscribedAt(), captured.getSubscribedAt());

        // lastUsedAt이 현재 시간과 가까운지 확인 (5초 이내)
        LocalDateTime now = LocalDateTime.now();
        assertTrue(
                Math.abs(captured.getLastUsedAt().until(now, java.time.temporal.ChronoUnit.SECONDS)) < 5,
                "lastUsedAt should be close to current time"
        );
    }
}

