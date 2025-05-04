package com.uf.assistance.domain.scheduler;

import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_jobs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"job_name", "job_group", "ai_subscription_id"})
})
@Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_name", nullable = false)
    private String jobName;

    @Column(name = "job_group", nullable = false)
    private String jobGroup;

    @Column(name = "job_class", nullable = false)
    private String jobClass;

    @Column(name = "cron_expression")
    private String cronExpression;

    private String jobType; // 실행할 로직 종류 (ex: "SendChat", "CheckData")

    @Column(columnDefinition="TEXT")
    private String jobData; // JSON 형식의 파라미터

    @Setter
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "ai_subscription_id")
    private AISubscription aiSubscription; // 관계 설정

    @Column(name = "last_execution")
    private LocalDateTime lastExecution;

    @Column(name = "next_execution")
    private LocalDateTime nextExecution;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    /**
     * 작업 실행 후 호출되는 메서드
     * 마지막 실행 시간을 업데이트
     */
    public void updateLastExecution() {
        this.lastExecution = LocalDateTime.now();
    }

    /**
     * 다음 실행 시간 업데이트
     * @param nextExecution 다음 실행 시간
     */
    public void updateNextExecution(LocalDateTime nextExecution) {
        this.nextExecution = nextExecution;
    }
}
