package com.uf.assistance.domain.ai;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uf.assistance.domain.chat.Chat;
import com.uf.assistance.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * AI 구독 엔티티
 * 사용자가 특정 AI를 구독한 관계 정의
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ai_subscription_tb",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "ai_id"}))
@Entity
public class AISubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_id", nullable = false)
    private CustomAI customAI;

    @Column(name="active")
    private boolean isActive;

    @CreatedDate
    @Column(updatable = false, name="created_by")
    private LocalDateTime subscribedAt;

    @Column
    private LocalDateTime lastUsedAt;

    @Builder.Default
    @OneToMany(mappedBy = "aiSubscription", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Chat> messages = new ArrayList<>();
}