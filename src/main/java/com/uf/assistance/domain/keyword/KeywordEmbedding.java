package com.uf.assistance.domain.keyword;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword_embedding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String keyword;

    @Column(columnDefinition = "vector(384)", nullable = false)
    @Convert(converter = VectorConverter.class)
    private float[] embedding;
}