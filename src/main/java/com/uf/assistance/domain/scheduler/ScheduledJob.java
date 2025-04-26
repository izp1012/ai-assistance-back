package com.uf.assistance.domain.scheduler;

import com.uf.assistance.domain.ai.AISubscription;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_jobs")
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobName;

    private String jobGroup;

    private String cronExpression;

    private String jobType; // 실행할 로직 종류 (ex: "SendChat", "CheckData")

    @Column(columnDefinition="TEXT")
    private String jobData; // JSON 형식의 파라미터

    @Setter
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "ai_subscription_id")
    private AISubscription aiSubscription; // 관계 설정

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum Status {
        ENABLED, DISABLED, PAUSED
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
