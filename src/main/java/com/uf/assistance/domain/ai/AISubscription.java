package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


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
    private AI ai;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime subscribedAt;

    @Column
    private LocalDateTime lastUsedAt;
}