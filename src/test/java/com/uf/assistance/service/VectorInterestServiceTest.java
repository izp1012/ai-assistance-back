package com.uf.assistance.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uf.assistance.domain.keyword.Interest;
import com.uf.assistance.domain.keyword.InterestRepository;
import com.uf.assistance.dto.keyword.KeywordReq;
import com.uf.assistance.dto.keyword.KeywordResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.client.RestTemplate;

import java.sql.Array;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VectorInterestServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private InterestRepository interestRepository;

    @Mock
    private ResultSet resultSet;

    @Mock
    private Array sqlArray;

    private ObjectMapper objectMapper;
    private VectorInterestService vectorInterestService;
    private final String flaskBaseUrl = "http://3.39.234.47:5001";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();  // 실제 ObjectMapper 객체 생성
        objectMapper = spy(objectMapper);   // spy로 감싸기
        vectorInterestService = new VectorInterestService(jdbcTemplate, restTemplate, objectMapper, interestRepository, flaskBaseUrl);
    }

    @Test
    void extractKeywords_ShouldReturnKeywordResponse() {
        // Given
        KeywordReq request = new KeywordReq();
        request.setText("테스트 텍스트입니다");

        KeywordResp expectedResponse = new KeywordResp();
        expectedResponse.setKeywords(List.of("테스트", "텍스트"));

        when(restTemplate.exchange(
                eq(flaskBaseUrl + "/extract_keywords"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(KeywordResp.class)))
                .thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // When
        KeywordResp actualResponse = vectorInterestService.extractKeywords(request);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getKeywords(), actualResponse.getKeywords());
        verify(restTemplate).exchange(
                eq(flaskBaseUrl + "/extract_keywords"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(KeywordResp.class));
    }

    @Test
    void processTextAndSaveInterest_ShouldSaveNewInterest() throws Exception {
        // Given
        String text = "테스트 텍스트입니다";

        // API 응답 생성
        ObjectNode responseJson = objectMapper.createObjectNode();
        ArrayNode keywordsNode = responseJson.putArray("keywords");
        keywordsNode.add("테스트");

        ArrayNode embeddingsNode = responseJson.putArray("embeddings");
        ArrayNode embeddingVector = embeddingsNode.addArray();
        embeddingVector.add(0.1f).add(0.2f).add(0.3f);

        // Interest가 존재하지 않는 경우 (새로운 키워드)
        when(interestRepository.findByKeyword("테스트")).thenReturn(null);

        // 새로 저장된 Interest 반환
        Interest newInterest = Interest.builder()
                .id(1L)
                .keyword("테스트")
                .build();

        when(interestRepository.save(any(Interest.class))).thenReturn(newInterest);

        // JDBC 템플릿 업데이트 (벡터 정보 저장)
        when(jdbcTemplate.update(
                eq("UPDATE interest SET vector = ?::vector WHERE id = ?"),
                eq("[0.1,0.2,0.3]"),
                eq(1L)))
                .thenReturn(1);

        when(restTemplate.postForEntity(
                eq(flaskBaseUrl + "/get_keyword_embeddings"),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson.toString(), HttpStatus.OK));

        // When
        Interest result = vectorInterestService.processTextAndSaveInterest(text);

        // Then
        assertNotNull(result);
        assertEquals("테스트", result.getKeyword());
        assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, result.getVector());

        // JPA 저장 검증
        ArgumentCaptor<Interest> interestCaptor = ArgumentCaptor.forClass(Interest.class);
        verify(interestRepository).save(interestCaptor.capture());
        assertEquals("테스트", interestCaptor.getValue().getKeyword());

        // JDBC 업데이트 검증 (벡터 저장)
        verify(jdbcTemplate).update(
                eq("UPDATE interest SET vector = ?::vector WHERE id = ?"),
                eq("[0.1,0.2,0.3]"),
                eq(1L));
    }

    @Test
    void processTextAndSaveInterest_ShouldUpdateExistingInterest() throws Exception {
        // Given
        String text = "테스트 텍스트입니다";

        // API 응답 JSON 준비
        String responseJson = "{" +
                "\"keywords\": [\"테스트\"]," +
                "\"embeddings\": [[0.1, 0.2, 0.3]]" +
                "}";

        // Mock 노드 생성
        JsonNode mockedRootNode = mock(JsonNode.class);
        JsonNode mockedKeywordsNode = mock(JsonNode.class);
        JsonNode mockedKeywordNode = mock(JsonNode.class);
        JsonNode mockedEmbeddingsNode = mock(JsonNode.class);
        JsonNode mockedEmbeddingNode = mock(JsonNode.class);

        // Mock 동작 설정
        when(mockedRootNode.get("keywords")).thenReturn(mockedKeywordsNode);
        when(mockedKeywordsNode.get(0)).thenReturn(mockedKeywordNode);
        when(mockedKeywordNode.asText()).thenReturn("테스트");

        when(mockedRootNode.get("embeddings")).thenReturn(mockedEmbeddingsNode);
        when(mockedEmbeddingsNode.get(0)).thenReturn(mockedEmbeddingNode);
        when(mockedEmbeddingNode.size()).thenReturn(3);

        // embedding vector 설정
        JsonNode vector0 = mock(JsonNode.class);
        JsonNode vector1 = mock(JsonNode.class);
        JsonNode vector2 = mock(JsonNode.class);
        when(mockedEmbeddingNode.get(0)).thenReturn(vector0);
        when(mockedEmbeddingNode.get(1)).thenReturn(vector1);
        when(mockedEmbeddingNode.get(2)).thenReturn(vector2);
        when(vector0.floatValue()).thenReturn(0.1f);
        when(vector1.floatValue()).thenReturn(0.2f);
        when(vector2.floatValue()).thenReturn(0.3f);

        // API 호출 응답 설정
        when(restTemplate.postForEntity(
                eq(flaskBaseUrl + "/get_keyword_embeddings"),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson, HttpStatus.OK));

        // objectMapper.readTree 메서드가 호출될 때 미리 준비한 mock 노드 반환
        doReturn(mockedRootNode).when(objectMapper).readTree(responseJson);

        // 기존 Interest 설정
        Interest existingInterest = Interest.builder()
                .id(1L)
                .keyword("테스트")
                .build();

        when(interestRepository.findByKeyword("테스트")).thenReturn(existingInterest);
        when(interestRepository.save(any(Interest.class))).thenReturn(existingInterest);

        // JDBC 템플릿 설정
        when(jdbcTemplate.update(
                eq("UPDATE interest SET vector = ?::vector WHERE id = ?"),
                anyString(),
                eq(1L)))
                .thenReturn(1);

        // When
        Interest result = vectorInterestService.processTextAndSaveInterest(text);

        // Then
        assertNotNull(result);
        assertEquals("테스트", result.getKeyword());

        // 검증
        verify(restTemplate).postForEntity(
                eq(flaskBaseUrl + "/get_keyword_embeddings"),
                any(HttpEntity.class),
                eq(String.class));

        verify(objectMapper).readTree(responseJson);
        verify(interestRepository).save(any(Interest.class));
        verify(jdbcTemplate).update(
                eq("UPDATE interest SET vector = ?::vector WHERE id = ?"),
                anyString(),
                eq(1L));
    }

    @Test
    void getAllInterests_ShouldReturnAllInterestsWithVectors() {
        // Given
        List<Interest> jpaInterests = List.of(
                Interest.builder().id(1L).keyword("테스트1").build(),
                Interest.builder().id(2L).keyword("테스트2").build()
        );

        // JPA 리포지토리 반환 값 설정
        when(interestRepository.findAll()).thenReturn(jpaInterests);

        // JDBC 쿼리로 벡터 정보 로드 시뮬레이션
        Interest interest1WithVector = Interest.builder()
                .id(1L)
                .keyword("테스트1")
                .vector(new float[]{0.1f, 0.2f, 0.3f})
                .build();

        Interest interest2WithVector = Interest.builder()
                .id(2L)
                .keyword("테스트2")
                .vector(new float[]{0.4f, 0.5f, 0.6f})
                .build();

        when(jdbcTemplate.queryForObject(
                eq("SELECT * FROM interest WHERE id = ?"),
                any(VectorInterestService.InterestRowMapper.class),
                eq(1L)))
                .thenReturn(interest1WithVector);

        when(jdbcTemplate.queryForObject(
                eq("SELECT * FROM interest WHERE id = ?"),
                any(VectorInterestService.InterestRowMapper.class),
                eq(2L)))
                .thenReturn(interest2WithVector);

        // When
        List<Interest> result = vectorInterestService.getAllInterests();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("테스트1", result.get(0).getKeyword());
        assertEquals("테스트2", result.get(1).getKeyword());
        assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, result.get(0).getVector());
        assertArrayEquals(new float[]{0.4f, 0.5f, 0.6f}, result.get(1).getVector());

        // JPA 호출 검증
        verify(interestRepository).findAll();

        // JDBC 호출 검증 (벡터 정보 로드)
        verify(jdbcTemplate, times(2)).queryForObject(
                eq("SELECT * FROM interest WHERE id = ?"),
                any(VectorInterestService.InterestRowMapper.class),
                anyLong());
    }


    @Test
    void findSimilarInterests_ShouldReturnSimilarInterests() {
        // Given
        float[] vector = new float[]{0.1f, 0.2f, 0.3f};
        int limit = 2;
        List<Interest> expectedInterests = List.of(
                Interest.builder().id(1L).keyword("테스트1").vector(new float[]{0.1f, 0.2f, 0.3f}).build(),
                Interest.builder().id(2L).keyword("테스트2").vector(new float[]{0.4f, 0.5f, 0.6f}).build()
        );

        when(jdbcTemplate.query(
                eq("SELECT * FROM interest ORDER BY vector <=> ?::vector LIMIT ?"),
                any(VectorInterestService.InterestRowMapper.class),
                eq("[0.1,0.2,0.3]"),
                eq(limit)))
                .thenReturn(expectedInterests);

        // When
        List<Interest> result = vectorInterestService.findSimilarInterests(vector, limit);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("테스트1", result.get(0).getKeyword());
        verify(jdbcTemplate).query(
                eq("SELECT * FROM interest ORDER BY vector <=> ?::vector LIMIT ?"),
                any(VectorInterestService.InterestRowMapper.class),
                eq("[0.1,0.2,0.3]"),
                eq(limit));
    }

    @Test
    void testInterestRowMapper() throws Exception {
        // Given
        VectorInterestService.InterestRowMapper rowMapper = new VectorInterestService.InterestRowMapper();

        // ResultSet 모의 객체 설정
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("keyword")).thenReturn("테스트");

        // vector 배열 모의 객체 설정
        Float[] vectorArray = new Float[]{0.1f, 0.2f, 0.3f};
        when(resultSet.getArray("vector")).thenReturn(sqlArray);
        when(sqlArray.getArray()).thenReturn(vectorArray);

        // When
        Interest interest = rowMapper.mapRow(resultSet, 0);

        // Then
        assertNotNull(interest);
        assertEquals(1L, interest.getId());
        assertEquals("테스트", interest.getKeyword());
        assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, interest.getVector());
    }


    @Test
    void findOrCreateInterest_ShouldReturnExistingInterest() {
        // Given
        String keyword = "테스트";
        Interest existingInterest = Interest.builder()
                .id(1L)
                .keyword(keyword)
                .build();

        Interest interestWithVector = Interest.builder()
                .id(1L)
                .keyword(keyword)
                .vector(new float[]{0.1f, 0.2f, 0.3f})
                .build();

        when(interestRepository.findByKeyword(keyword)).thenReturn(existingInterest);
        when(jdbcTemplate.queryForObject(
                eq("SELECT * FROM interest WHERE id = ?"),
                any(VectorInterestService.InterestRowMapper.class),
                eq(1L)))
                .thenReturn(interestWithVector);

        // When
        Interest result = vectorInterestService.findOrCreateInterest(keyword);

        // Then
        assertNotNull(result);
        assertEquals(keyword, result.getKeyword());
        assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, result.getVector());
        verify(interestRepository).findByKeyword(keyword);
        verify(jdbcTemplate).queryForObject(
                eq("SELECT * FROM interest WHERE id = ?"),
                any(VectorInterestService.InterestRowMapper.class),
                eq(1L));
        // API 호출이 발생하지 않음을 검증
        verify(restTemplate, never()).postForEntity(
                eq(flaskBaseUrl + "/get_embedding"),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    void findOrCreateInterest_ShouldCreateNewInterest() throws Exception {
        // Given
        String keyword = "새키워드";

        // 키워드가 없는 경우
        when(interestRepository.findByKeyword(keyword)).thenReturn(null);

        // API 응답 생성
        ObjectNode responseJson = objectMapper.createObjectNode();
        ArrayNode embeddingNode = responseJson.putArray("embedding");
        embeddingNode.add(0.1f).add(0.2f).add(0.3f);

        when(restTemplate.postForEntity(
                eq(flaskBaseUrl + "/get_embedding"),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson.toString(), HttpStatus.OK));

        // 새로 저장된 Interest 반환
        Interest newInterest = Interest.builder()
                .id(1L)
                .keyword(keyword)
                .build();

        when(interestRepository.save(any(Interest.class))).thenReturn(newInterest);

        // When
        Interest result = vectorInterestService.findOrCreateInterest(keyword);

        // Then
        assertNotNull(result);
        assertEquals(keyword, result.getKeyword());
        assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f}, result.getVector());

        // 저장 검증
        verify(interestRepository).save(any(Interest.class));

        // API 호출 검증
        verify(restTemplate).postForEntity(
                eq(flaskBaseUrl + "/get_embedding"),
                any(HttpEntity.class),
                eq(String.class));

        // 벡터 업데이트 검증
        verify(jdbcTemplate).update(
                eq("UPDATE interest SET vector = ?::vector WHERE id = ?"),
                eq("[0.1,0.2,0.3]"),
                eq(1L));
    }

    @Test
    void calculateVectorMidpoint_ShouldReturnMidpoint() throws Exception {
        // Given
        float[] vector1 = new float[]{0.1f, 0.2f, 0.3f};
        float[] vector2 = new float[]{0.4f, 0.5f, 0.6f};
        float weight = 0.5f;

        // API 응답 생성
        ObjectNode responseJson = objectMapper.createObjectNode();
        ArrayNode midpointNode = responseJson.putArray("midpoint");
        midpointNode.add(0.25f).add(0.35f).add(0.45f);

        when(restTemplate.postForEntity(
                eq(flaskBaseUrl + "/vector_midpoint"),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson.toString(), HttpStatus.OK));

        // When
        float[] result = vectorInterestService.calculateVectorMidpoint(vector1, vector2, weight);

        // Then
        assertNotNull(result);
        assertEquals(3, result.length);
        assertArrayEquals(new float[]{0.25f, 0.35f, 0.45f}, result);

        // 요청 검증
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq(flaskBaseUrl + "/vector_midpoint"),
                requestCaptor.capture(),
                eq(String.class));

        // 요청 본문 검증
        Map<String, Object> requestBody = requestCaptor.getValue().getBody();
        assertNotNull(requestBody);
        assertEquals(weight, requestBody.get("weight"));
        assertArrayEquals(vector1, (float[]) requestBody.get("vector1"));
        assertArrayEquals(vector2, (float[]) requestBody.get("vector2"));
    }

    @Test
    void calculateKeywordVectorMidpoint_ShouldReturnMidpointData() throws Exception {
        // Given
        String text1 = "첫 번째 텍스트";
        String text2 = "두 번째 텍스트";
        float weight = 0.5f;

        // API 응답 생성 및 Map 변환
        Map<String, Object> responseMap = new HashMap<>();
        List<Double> midpoint = List.of(0.25, 0.35, 0.45);
        responseMap.put("midpoint", midpoint);
        responseMap.put("cosine_similarity", 0.85);

        Map<String, List<String>> extractedKeywords = new HashMap<>();
        extractedKeywords.put("text1", List.of("첫", "텍스트"));
        extractedKeywords.put("text2", List.of("두", "텍스트"));
        responseMap.put("extracted_keywords", extractedKeywords);

        // API 호출 응답 설정
        String mockResponseJson = "{\"mockResponse\":\"data\"}";  // 실제 내용은 중요하지 않음

        when(restTemplate.postForEntity(
                anyString(),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(mockResponseJson, HttpStatus.OK));

        // ObjectMapper spy에 대한 동작 설정 - readValue 메서드만 가로채서 우리가 원하는 결과 반환
        doReturn(responseMap).when(objectMapper).readValue(mockResponseJson, Map.class);

        // When
        Map<String, Object> result = vectorInterestService.calculateKeywordVectorMidpoint(text1, text2, weight);

        // Then
        assertNotNull(result);
        assertEquals(0.85, result.get("cosine_similarity"));
        assertEquals(midpoint, result.get("midpoint"));

        // 요청 파라미터 검증
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq(flaskBaseUrl + "/keyword_vector_midpoint"),
                requestCaptor.capture(),
                eq(String.class));

        Map<String, Object> requestBody = requestCaptor.getValue().getBody();
        assertEquals(text1, requestBody.get("text1"));
        assertEquals(text2, requestBody.get("text2"));
        assertEquals(weight, requestBody.get("weight"));
        assertEquals(5, requestBody.get("top_n"));
    }

    @Test
    void calculateTextSimilarity_ShouldReturnSimilarityScore() throws Exception {
        // Given
        String text1 = "첫 번째 텍스트";
        String text2 = "두 번째 텍스트";

        // API 응답 생성
        ObjectNode responseJson = objectMapper.createObjectNode();
        responseJson.put("cosine_similarity", 0.75);

        when(restTemplate.postForEntity(
                eq(flaskBaseUrl + "/compare_keywords"),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(new ResponseEntity<>(responseJson.toString(), HttpStatus.OK));

        // When
        double similarity = vectorInterestService.calculateTextSimilarity(text1, text2);

        // Then
        assertEquals(0.75, similarity);

        // 요청 검증
        ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                eq(flaskBaseUrl + "/compare_keywords"),
                requestCaptor.capture(),
                eq(String.class));

        // 요청 본문 검증
        Map<String, Object> requestBody = requestCaptor.getValue().getBody();
        assertNotNull(requestBody);
        assertEquals(text1, requestBody.get("text1"));
        assertEquals(text2, requestBody.get("text2"));
    }

    @Test
    void findSimilarInterestsAboveThreshold_ShouldReturnInterestsAboveThreshold() {
        // Given
        float[] vector = new float[]{0.1f, 0.2f, 0.3f};
        double threshold = 0.8;

        List<Interest> expectedInterests = List.of(
                Interest.builder().id(1L).keyword("테스트1").vector(new float[]{0.1f, 0.2f, 0.3f}).build(),
                Interest.builder().id(2L).keyword("테스트2").vector(new float[]{0.15f, 0.25f, 0.35f}).build()
        );

        when(jdbcTemplate.query(
                eq("SELECT * FROM interest WHERE 1 - (vector <=> ?::vector) >= ? ORDER BY vector <=> ?::vector"),
                any(VectorInterestService.InterestRowMapper.class),
                eq("[0.1,0.2,0.3]"),
                eq(threshold),
                eq("[0.1,0.2,0.3]")))
                .thenReturn(expectedInterests);

        // When
        List<Interest> result = vectorInterestService.findSimilarInterestsAboveThreshold(vector, threshold);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("테스트1", result.get(0).getKeyword());
        assertEquals("테스트2", result.get(1).getKeyword());

        verify(jdbcTemplate).query(
                eq("SELECT * FROM interest WHERE 1 - (vector <=> ?::vector) >= ? ORDER BY vector <=> ?::vector"),
                any(VectorInterestService.InterestRowMapper.class),
                eq("[0.1,0.2,0.3]"),
                eq(threshold),
                eq("[0.1,0.2,0.3]"));
    }

    @Test
    void formatVectorForPgVector_ShouldReturnCorrectFormat() throws Exception {
        // Given
        float[] vector = new float[]{0.1f, 0.2f, 0.3f};
        String expectedFormat = "[0.1,0.2,0.3]";

        // When
        String result = (String) vectorInterestService.getClass()
                .getDeclaredMethod("formatVectorForPgVector", float[].class)
                .invoke(vectorInterestService, vector);

        // Then
        assertEquals(expectedFormat, result);
    }

}