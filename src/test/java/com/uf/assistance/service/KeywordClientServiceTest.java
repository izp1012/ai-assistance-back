package com.uf.assistance.service;

import com.uf.assistance.dto.keyword.KeywordReq;
import com.uf.assistance.dto.keyword.KeywordResp;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class KeywordClientServiceTest {

    @Autowired
    private KeywordClientService keywordClientService;

    @Test
    public void extractKeywords_실제FlaskAPI호출_성공확인() {
        // given
//        KeywordReq request = new KeywordReq();
//        request.setText("AI is rapidly evolving and transforming industries.");
//
//        // when
//        KeywordResp response = keywordClientService.extractKeywords(request);
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.getKeywords()).isNotEmpty();
//        assertThat(response.getKeyword_count()).isGreaterThan(0);
//
//        // optional: 출력
//        System.out.println("추출된 키워드: " + response.getKeywords());
//        System.out.println("점수: " + response.getScores());
    }
}