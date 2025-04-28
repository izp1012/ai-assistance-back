package com.uf.assistance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.keyword.ChatKeyword;
import com.uf.assistance.domain.keyword.ChatKeywordRepository;
import com.uf.assistance.domain.keyword.Interest;
import com.uf.assistance.domain.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatKeywordService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ChatKeywordRepository chatKeywordRepository;
    private final VectorInterestService vectorInterestService;
    private final UserInterestService userInterestService;
    private final ObjectMapper objectMapper;
    private final KeywordGenerationService keywordGenerationService;

    @Autowired
    public ChatKeywordService(
            ChatKeywordRepository chatKeywordRepository,
            VectorInterestService vectorInterestService,
            UserInterestService userInterestService,
            ObjectMapper objectMapper,
            KeywordGenerationService keywordGenerationService) {
        this.chatKeywordRepository = chatKeywordRepository;
        this.vectorInterestService = vectorInterestService;
        this.userInterestService = userInterestService;
        this.objectMapper = objectMapper;
        this.keywordGenerationService = keywordGenerationService;
    }

    /**
     * 채팅 메시지에서 키워드를 추출하고 ChatKeyword 관계를 생성
     * User-Interest 카운트도 함께 증가
     */
    @Transactional
    public ChatKeyword processMessageAndCreateLink(Chat chat, String message) {
        logger.debug("채팅 메시지에서 키워드 추출 및 링크 생성");

        // 텍스트를 분석하여 Interest 생성 또는 업데이트 (벡터 포함)
        Interest interest = vectorInterestService.processTextAndSaveInterest(message);

        // ChatKeyword 생성 및 저장
        ChatKeyword chatKeyword = ChatKeyword.builder()
                .chat(chat)
                .interest(interest)
                .build();

        // 사용자의 관심사 카운트 증가
        User user = chat.getSender();
        userInterestService.incrementUserInterestCount(user, interest);

        return chatKeywordRepository.save(chatKeyword);
    }

    /**
     * System Message와 User Message 간의 중간값 벡터 계산 및 유사 키워드 추출
     * 유사도 임계값 이상인 경우 기존 키워드 사용, 미만인 경우 새 키워드 추출 및 저장
     * OpenAI를 사용하여 키워드 간의 중간 키워드를 생성하는 기능 추가
     */
    @Transactional
    public String enhanceUserMessageWithKeywords(String systemMessage, String userMessage) {
        logger.debug("시스템 메시지와 사용자 메시지 간의 중간값 키워드 추출");

        try {
            // 두 텍스트 간의 키워드 벡터 중간점 계산
            Map<String, Object> midpointResult = vectorInterestService.calculateKeywordVectorMidpoint(
                    systemMessage, userMessage, 0.5f);

            // 유사도 확인
            double similarity = (Double) midpointResult.get("cosine_similarity");
            logger.debug("시스템 메시지와 사용자 메시지 간의 코사인 유사도: {}", similarity);

            // 중간점 벡터 추출
            List<Number> midpointNumbers = (List<Number>) midpointResult.get("midpoint");
            float[] midpointVector = new float[midpointNumbers.size()];
            for (int i = 0; i < midpointNumbers.size(); i++) {
                midpointVector[i] = midpointNumbers.get(i).floatValue();
            }

            List<String> resultKeywords = new ArrayList<>();
            // 유사한 키워드 검색을 위한 초기화
            List<Interest> similarInterests;

            // systemMessage와 userMessage를 모두 포함하는 메시지 생성
//            StringBuilder enhancedMessage = new StringBuilder();
//            enhancedMessage.append(systemMessage).append("\n\n").append(userMessage);

            // 유사도가 80% 이상인 경우 기존 키워드 사용 (Limit 증가)
            if (similarity >= 0.8) {
                // 상위 10개까지 유사 키워드 검색
                similarInterests = vectorInterestService.findSimilarInterests(midpointVector, 10);
            }
//                if (!similarInterests.isEmpty()) {
//                    // 모든 유사 키워드 사용
//                    for (Interest interest : similarInterests) {
//                        resultKeywords.add(interest.getKeyword());
//                    }
//                }
//            }
            // 유사도가 80% 미만인 경우
            else {
                // 유사도가 높은 키워드 검색 (80% 이상)
                similarInterests = vectorInterestService.findSimilarInterestsAboveThreshold(midpointVector, 0.8);

                // 유사한 키워드가 없으면 KeywordGenerationService 호출하여 중간 키워드 생성
                if (similarInterests.isEmpty()) {
                    // 추출된 키워드 가져오기
                    Map extratedText1 = (Map) midpointResult.get("text1");
                    Map extratedText2 = (Map) midpointResult.get("text2");

                    List<String> text1Keywords = (List<String>) extratedText1.get("keywords");
                    List<String> text2Keywords = (List<String>) extratedText2.get("keywords");

                    // 키워드 생성 서비스 호출
                    List<String> middleKeywords = keywordGenerationService.generateMiddleKeywords(text1Keywords, text2Keywords);

                    if (!middleKeywords.isEmpty()) {
                        for (String newKeyword : middleKeywords) {
//                            // 각 중간 키워드에 대한 임베딩 가져오기
//                            Interest interest = vectorInterestService.findOrCreateInterest(newKeyword);
//                            // 결과 키워드 목록에 추가
//                            resultKeywords.add(newKeyword);
                            // ✅ 여기 수정: List<Interest>로 받아서 모두 resultKeywords에 추가
                            List<Interest> newInterests = vectorInterestService.findOrCreateInterest(newKeyword);

                            for (Interest interest : newInterests) {
                                resultKeywords.add(interest.getKeyword());
                            }

                        }
                    }
                }
                // 유사도 높은 키워드 모두 사용
                if (!similarInterests.isEmpty()) {
                    for (Interest interest : similarInterests) {
                        resultKeywords.add(interest.getKeyword());
                    }
                }
            }

            // ✅ 메시지 최종 조립
            StringBuilder enhancedMessage = new StringBuilder();
            enhancedMessage.append(systemMessage).append("\n\n").append(userMessage);

            if (!resultKeywords.isEmpty()) {
                enhancedMessage.append("\n\n");
                for (String keyword : resultKeywords) {
                    enhancedMessage.append(" #").append(keyword);
                }
            }

            return enhancedMessage.toString();

        } catch (Exception e) {
            logger.error("키워드 중간값 계산 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 원본 메시지(시스템 메시지 + 사용자 메시지) 반환
            return systemMessage + "\n\n" + userMessage;
        }
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