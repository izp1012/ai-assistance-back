package com.uf.assistance.domain.keyword;

import com.uf.assistance.domain.chat.Chat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_keyword",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "keyword_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private KeywordEmbedding keyword;

    @Column(nullable = false, updatable = false)
    private final LocalDateTime linkedAt = LocalDateTime.now();
}