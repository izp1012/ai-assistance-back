package com.uf.assistance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uf.assistance.dto.keyword.KeywordReq;
import com.uf.assistance.dto.keyword.KeywordResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import static org.assertj.core.api.Assertions.assertThat;

public class KeywordClientServiceMockTest {

    private KeywordClientService keywordClientService;
    private MockRestServiceServer mockServer;
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        keywordClientService = new KeywordClientService(restTemplate);
        keywordClientService.setFlaskBaseUrl("http://localhost:5001");

        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void extractKeywords_null필드없는요청_정상mock응답확인() throws Exception {
        // given
        KeywordReq request = new KeywordReq();
        request.setText("Mock testing for AI keyword extraction.");
        request.setMin_keywords(3);  // 나머지 필드는 null → JSON에서 제외됨

        String mockResponseJson = """
        {
            "keywords": ["AI", "testing", "extraction"],
            "scores": [0.95, 0.88, 0.81],
            "keyword_count": 3,
            "is_dynamic": true,
            "top_n": 3
        }
        """;

        ObjectMapper objectMapper = new ObjectMapper();
        String expectedRequestJson = objectMapper.writeValueAsString(request);

        mockServer.expect(requestTo("http://localhost:5001/extract_keywords"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedRequestJson))  // null 필드 제외된 JSON
                .andRespond(withSuccess(mockResponseJson, MediaType.APPLICATION_JSON));

        // when
        KeywordResp response = keywordClientService.extractKeywords(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getKeywords()).containsExactly("AI", "testing", "extraction");
        assertThat(response.getScores()).hasSize(3);
        assertThat(response.getKeyword_count()).isEqualTo(3);
    }
}