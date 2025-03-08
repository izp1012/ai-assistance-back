package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * AI 엔티티
 * 개발자가 정의하고 사용자가 구독할 수 있는 AI 정의
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "ai_tb")
@Entity
public class AI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    @Column
    private String description;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false)
    private String aiProvider; // OpenAI, Anthropic 등

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User developer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_prompt_id")
    private PromptTemplate basePrompt;

    @Size(max = 5000)
    @Column(length = 5000)
    private String customPrompt;

    @Column(nullable = false)
    private boolean isPublic;

    @Column(nullable = false)
    private boolean isActive;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

