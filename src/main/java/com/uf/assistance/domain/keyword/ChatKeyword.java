package com.uf.assistance.domain.keyword;

import com.uf.assistance.domain.chat.Chat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_keyword",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "interest_id"})
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

    // Interest는 이제 JPA 엔티티이므로 그대로 ManyToOne 관계 유지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(nullable = false, updatable = false)
    private final LocalDateTime linkedAt = LocalDateTime.now();
}