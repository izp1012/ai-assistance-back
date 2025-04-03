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
        objectMapper = new ObjectMapper();
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

        // API 응답 생성
        ObjectNode responseJson = objectMapper.createObjectNode();
        ArrayNode keywordsNode = responseJson.putArray("keywords");
        keywordsNode.add("테스트");

        ArrayNode embeddingsNode = responseJson.putArray("embeddings");
        ArrayNode embeddingVector = embeddingsNode.addArray();
        embeddingVector.add(0.1f).add(0.2f).add(0.3f);

        // 기존 Interest 반환
        Interest existingInterest = Interest.builder()
                .id(1L)
                .keyword("테스트")
                .build();

        when(interestRepository.findByKeyword("테스트")).thenReturn(existingInterest);

        // 업데이트된 Interest 반환
        Interest updatedInterest = Interest.builder()
                .id(1L)
                .keyword("테스트")
                .build();

        when(interestRepository.save(any(Interest.class))).thenReturn(updatedInterest);

        // JDBC 템플릿 업데이트 (벡터 정보 업데이트)
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
        verify(interestRepository).save(any(Interest.class));

        // JDBC 업데이트 검증 (벡터 업데이트)
        verify(jdbcTemplate).update(
                eq("UPDATE interest SET vector = ?::vector WHERE id = ?"),
                eq("[0.1,0.2,0.3]"),
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

}