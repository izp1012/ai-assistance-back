package com.uf.assistance.service;

import com.uf.assistance.dto.keyword.KeywordReq;
import com.uf.assistance.dto.keyword.KeywordResp;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Setter
public class KeywordClientService {

    @Value("${flask.api.base-url}")
    private String flaskBaseUrl;

    private final RestTemplate restTemplate;

    public KeywordClientService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

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
}