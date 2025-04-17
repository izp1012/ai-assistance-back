package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * AI 엔티티
 * 개발자가 정의하고 CustomAI 의 기반이 되는 AI
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "base_ai_tb")
@Entity
public class BaseAI {

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

    @NotBlank
    @Column(length = 5000)
    private String basePrompt;

    @Column(nullable = false)
    @ColumnDefault("true")  // DB 스키마에 기본값 설정
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (updatedBy == null) {
            updatedBy = createdBy;  // 생성 시 updatedAt을 createdAt과 동일하게 설정
        }
    }
}

