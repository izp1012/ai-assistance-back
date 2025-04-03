package com.uf.assistance.service;

import com.fasterxml.jackson.databind.JsonNode;
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

    @Autowired
    public ChatKeywordService(
            ChatKeywordRepository chatKeywordRepository,
            VectorInterestService vectorInterestService,
            UserInterestService userInterestService,
            ObjectMapper objectMapper) {
        this.chatKeywordRepository = chatKeywordRepository;
        this.vectorInterestService = vectorInterestService;
        this.userInterestService = userInterestService;
        this.objectMapper = objectMapper;
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

            // 유사한 키워드 검색을 위한 초기화
            List<Interest> similarInterests;
            StringBuilder enhancedMessage = new StringBuilder(userMessage);

            // 유사도가 80% 미만인 경우 새 키워드 추출 및 저장
            if (similarity < 0.8) {
                // 유사도가 높은 키워드 검색 (80% 이상)
                similarInterests = vectorInterestService.findSimilarInterestsAboveThreshold(midpointVector, 0.8);

                // 유사한 키워드가 없으면 중간점 벡터로 새 키워드 생성
                if (similarInterests.isEmpty()) {
                    // 추출된 키워드 가져오기
                    Map extractedKeywords = (Map) midpointResult.get("extracted_keywords");
                    List<String> text1Keywords = (List<String>) extractedKeywords.get("text1");
                    List<String> text2Keywords = (List<String>) extractedKeywords.get("text2");

                    // 시스템 메시지와 사용자 메시지에서 추출된 키워드 병합
                    List<String> allKeywords = new ArrayList<>();
                    allKeywords.addAll(text1Keywords);
                    allKeywords.addAll(text2Keywords);

                    // 중간점 벡터로 새 Interest 생성
                    if (!allKeywords.isEmpty()) {
                        String newKeyword = allKeywords.get(0);  // 첫 번째 키워드 사용

                        // Interest 생성 및 벡터 저장
                        Interest interest = Interest.builder()
                                .keyword(newKeyword)
                                .build();
                        interest = vectorInterestService.findOrCreateInterest(newKeyword);

                        // 중간점 벡터 업데이트
                        interest.setVector(midpointVector);

                        // 사용자 메시지에 키워드 추가
                        enhancedMessage.append(" #").append(newKeyword);
                    }
                } else {
                    // 가장 유사한 키워드 선택 (첫 번째 결과)
                    Interest mostSimilarInterest = similarInterests.get(0);

                    // 사용자 메시지에 키워드 추가
                    enhancedMessage.append(" #").append(mostSimilarInterest.getKeyword());
                }
            } else {
                // 유사도가 80% 이상인 경우 기존 키워드 사용
                similarInterests = vectorInterestService.findSimilarInterests(midpointVector, 3);

                if (!similarInterests.isEmpty()) {
                    // 가장 유사한 키워드 선택 (첫 번째 결과)
                    Interest mostSimilarInterest = similarInterests.get(0);

                    // 사용자 메시지에 키워드 추가
                    enhancedMessage.append(" #").append(mostSimilarInterest.getKeyword());
                }
            }

            return enhancedMessage.toString();

        } catch (Exception e) {
            logger.error("키워드 중간값 계산 중 오류 발생: {}", e.getMessage(), e);
            // 오류 발생 시 원본 메시지 그대로 반환
            return userMessage;
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