package com.uf.assistance.config.env;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 환경 설정 변경 이력을 저장하는 엔티티
 */
@Entity
@Table(name = "env_audit_log")
@Getter
@Setter
public class EnvAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String key;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "old_description", columnDefinition = "TEXT")
    private String oldDescription;

    @Column(name = "new_description", columnDefinition = "TEXT")
    private String newDescription;

    @Column(nullable = false)
    private String action;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "client_ip")
    private String clientIp;

    // 생성자
    public EnvAuditLog() {
    }

    public EnvAuditLog(String key, String oldValue, String newValue,
                       String oldDescription, String newDescription,
                       String action, String changedBy) {
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.oldDescription = oldDescription;
        this.newDescription = newDescription;
        this.action = action;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }
}
