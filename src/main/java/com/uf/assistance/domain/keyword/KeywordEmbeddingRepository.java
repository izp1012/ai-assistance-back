package com.uf.assistance.domain.keyword;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KeywordEmbeddingRepository extends JpaRepository<KeywordEmbedding, Long> {
    Optional<KeywordEmbedding> findByKeyword(String keyword);
}