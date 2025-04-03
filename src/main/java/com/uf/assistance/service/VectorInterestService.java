package com.uf.assistance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.domain.keyword.Interest;
import com.uf.assistance.domain.keyword.InterestRepository;
import com.uf.assistance.dto.keyword.KeywordReq;
import com.uf.assistance.dto.keyword.KeywordResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VectorInterestService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final JdbcTemplate jdbcTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String flaskBaseUrl;
    private final InterestRepository interestRepository;

    public VectorInterestService(
            JdbcTemplate jdbcTemplate,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            InterestRepository interestRepository,
            @Value("${flask.api.base-url}") String flaskBaseUrl) {
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.flaskBaseUrl = flaskBaseUrl;
        this.interestRepository = interestRepository;
    }

    /**
     * RowMapper for Interest
     */
    public static class InterestRowMapper implements RowMapper<Interest> {
        @Override
        public Interest mapRow(ResultSet rs, int rowNum) throws SQLException {
            // Builder 패턴을 사용하여 객체 생성
            Interest.InterestBuilder builder = Interest.builder()
                    .id(rs.getLong("id"))
                    .keyword(rs.getString("keyword"));

            // vector 컬럼 처리
            Array vectorArray = rs.getArray("vector");
            if (vectorArray != null) {
                // PostgreSQL의 vector 타입은 float 배열로 변환 가능
                Float[] vectorObj = (Float[]) vectorArray.getArray();
                float[] vector = new float[vectorObj.length];
                for (int i = 0; i < vectorObj.length; i++) {
                    vector[i] = vectorObj[i] != null ? vectorObj[i] : 0f;
                }
                builder.vector(vector);
            }

            return builder.build();
        }
    }

    /**
     * Flask API를 호출하여 키워드 추출
     */
    public KeywordResp extractKeywords(KeywordReq request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<KeywordReq> entity = new HttpEntity<>(request, headers);

        ResponseEntity<KeywordResp> response = restTemplate.exchange(
                flaskBaseUrl + "/extract_keywords",
                HttpMethod.POST,
                entity,
                KeywordResp.class
        );

        return response.getBody();
    }

    /**
     * 텍스트에서 키워드와 임베딩을 추출하여 Interest 테이블에 저장
     * 키워드가 이미 존재하면 벡터만 업데이트
     */
    @Transactional
    public Interest processTextAndSaveInterest(String text) {
        try {
            // API 호출 준비
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", text);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // API 호출 - 키워드와 임베딩 가져오기
            ResponseEntity<String> response = restTemplate.postForEntity(
                    flaskBaseUrl + "/get_keyword_embeddings",
                    request,
                    String.class);

            // 응답 처리
            JsonNode rootNode = objectMapper.readTree(response.getBody());

            // 첫 번째 키워드와 임베딩 가져오기 (가장 중요한 키워드)
            String keyword = rootNode.get("keywords").get(0).asText();

            // 임베딩 배열로 변환
            JsonNode embeddingNode = rootNode.get("embeddings").get(0);
            float[] vector = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                vector[i] = embeddingNode.get(i).floatValue();
            }

            // JPA로 키워드 조회
            Interest interest = interestRepository.findByKeyword(keyword);

            if (interest != null) {
                // 기존 키워드가 있다면 벡터만 업데이트 (JPA)
                interest = interestRepository.save(interest);

                // 벡터 업데이트 (JDBC)
                jdbcTemplate.update(
                        "UPDATE interest SET vector = ?::vector WHERE id = ?",
                        formatVectorForPgVector(vector),
                        interest.getId()
                );

                // 벡터 설정
                interest.setVector(vector);
            } else {
                // 새 키워드라면 JPA로 먼저 저장 (벡터 필드 제외)
                interest = Interest.builder()
                        .keyword(keyword)
                        .build();
                interest = interestRepository.save(interest);

                // 벡터는 JDBC로 업데이트
                jdbcTemplate.update(
                        "UPDATE interest SET vector = ?::vector WHERE id = ?",
                        formatVectorForPgVector(vector),
                        interest.getId()
                );

                // 벡터 설정
                interest.setVector(vector);
            }

            return interest;

        } catch (Exception e) {
            logger.error("텍스트 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("텍스트 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 키워드로 Interest 조회 또는 생성
     */
    @Transactional
    public Interest findOrCreateInterest(String keyword) {
        logger.debug("키워드 '{}' 조회 또는 생성", keyword);

        // 기존 키워드 검색
        Interest interest = interestRepository.findByKeyword(keyword);

        if (interest != null) {
            // 벡터 정보 로드
            return loadVectorData(interest);
        } else {
            // 키워드 임베딩 API 호출
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("text", keyword);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            try {
                // 임베딩 가져오기
                ResponseEntity<String> response = restTemplate.postForEntity(
                        flaskBaseUrl + "/get_embedding",
                        request,
                        String.class);

                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode embeddingNode = rootNode.get("embedding");

                // 임베딩 배열로 변환
                float[] vector = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    vector[i] = embeddingNode.get(i).floatValue();
                }

                // 새 키워드 저장
                interest = Interest.builder()
                        .keyword(keyword)
                        .build();
                interest = interestRepository.save(interest);

                // 벡터 업데이트
                jdbcTemplate.update(
                        "UPDATE interest SET vector = ?::vector WHERE id = ?",
                        formatVectorForPgVector(vector),
                        interest.getId()
                );

                // 벡터 설정
                interest.setVector(vector);

                return interest;
            } catch (Exception e) {
                logger.error("키워드 '{}' 임베딩 생성 중 오류: {}", keyword, e.getMessage(), e);
                throw new RuntimeException("키워드 임베딩 생성 중 오류: " + e.getMessage(), e);
            }
        }
    }

    /**
     * 두 벡터의 중간점 계산 (Flask API 호출)
     */
    public float[] calculateVectorMidpoint(float[] vector1, float[] vector2, float weight) {
        logger.debug("두 벡터의 중간점 계산 (가중치: {})", weight);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("vector1", vector1);
        requestBody.put("vector2", vector2);
        requestBody.put("weight", weight);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    flaskBaseUrl + "/vector_midpoint",
                    request,
                    String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode midpointNode = rootNode.get("midpoint");

            // 중간점 벡터 배열로 변환
            float[] midpoint = new float[midpointNode.size()];
            for (int i = 0; i < midpointNode.size(); i++) {
                midpoint[i] = midpointNode.get(i).floatValue();
            }

            return midpoint;
        } catch (Exception e) {
            logger.error("벡터 중간점 계산 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("벡터 중간점 계산 중 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 두 텍스트 간의 키워드 중간점 계산 (Flask API 호출)
     */
    public Map<String, Object> calculateKeywordVectorMidpoint(String text1, String text2, float weight) {
        logger.debug("두 텍스트 간의 키워드 중간점 계산 (가중치: {})", weight);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text1", text1);
        requestBody.put("text2", text2);
        requestBody.put("weight", weight);
        requestBody.put("top_n", 5);  // 상위 5개 키워드 추출

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    flaskBaseUrl + "/keyword_vector_midpoint",
                    request,
                    String.class);

            // 응답을 Map으로 변환
            return objectMapper.readValue(response.getBody(), Map.class);
        } catch (Exception e) {
            logger.error("키워드 벡터 중간점 계산 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("키워드 벡터 중간점 계산 중 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 두 텍스트 간의 유사도 계산 (Flask API 호출)
     */
    public double calculateTextSimilarity(String text1, String text2) {
        logger.debug("두 텍스트 간의 유사도 계산");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("text1", text1);
        requestBody.put("text2", text2);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    flaskBaseUrl + "/compare_keywords",
                    request,
                    String.class);

            JsonNode rootNode = objectMapper.readTree(response.getBody());
            return rootNode.get("cosine_similarity").asDouble();
        } catch (Exception e) {
            logger.error("텍스트 유사도 계산 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("텍스트 유사도 계산 중 오류: " + e.getMessage(), e);
        }
    }

    /**
     * 모든 키워드 목록 조회 (JPA 사용)
     */
    public List<Interest> getAllInterests() {
        List<Interest> interests = interestRepository.findAll();

        // 벡터 정보 로드 (JDBC)
        return interests.stream()
                .map(this::loadVectorData)
                .collect(Collectors.toList());
    }

    /**
     * 유사한 키워드 검색 (코사인 유사도 기준) - JDBC 사용
     */
    public List<Interest> findSimilarInterests(float[] vector, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM interest ORDER BY vector <=> ?::vector LIMIT ?",
                new InterestRowMapper(),
                formatVectorForPgVector(vector),
                limit
        );
    }

    /**
     * 유사도 임계값 이상의 키워드 검색
     */
    public List<Interest> findSimilarInterestsAboveThreshold(float[] vector, double threshold) {
        logger.debug("유사도 임계값 {}% 이상의 키워드 검색", threshold * 100);

        // PostgreSQL의 코사인 유사도는 <=> 연산자로 계산됨
        // 1 - (<=> 값)이 코사인 유사도가 됨
        double distanceThreshold = 1.0 - threshold;

        return jdbcTemplate.query(
                "SELECT * FROM interest WHERE 1 - (vector <=> ?::vector) >= ? ORDER BY vector <=> ?::vector",
                new InterestRowMapper(),
                formatVectorForPgVector(vector),
                threshold,
                formatVectorForPgVector(vector)
        );
    }

    /**
     * JPA로 로드한 Interest에 벡터 정보 추가하기
     */
    private Interest loadVectorData(Interest interest) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM interest WHERE id = ?",
                new InterestRowMapper(),
                interest.getId()
        );
    }

    /**
     * float 배열을 PostgreSQL의 vector 타입으로 변환하기 위한 포맷팅
     */
    private String formatVectorForPgVector(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}