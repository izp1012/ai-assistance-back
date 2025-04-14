package com.uf.assistance.service;

import java.util.List;

// KeywordGenerationService 인터페이스
public interface KeywordGenerationService {
    List<String> generateMiddleKeywords(List<String> keywords1, List<String> keywords2);
}

