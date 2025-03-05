package com.uf.assistance.config.env;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 환경 설정 변경 이력을 저장하는 엔티티
 */
@Getter
@Setter
@Entity
@Table(name = "env_audit_log")
public class EnvAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false)
    private String settingKey;

    @Column(name = "old_setting_value", columnDefinition = "TEXT")
    private String oldSettingValue;

    @Column(name = "new_setting_value", columnDefinition = "TEXT")
    private String newSettingValue;

    @Column(name = "old_description", columnDefinition = "TEXT")
    private String oldDescription;

    @Column(name = "new_description", columnDefinition = "TEXT")
    private String newDescription;

    @Column(name = "action_type", nullable = false)
    private String actionType;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(name = "changed_by")
    private String changedBy;

    @Column(name = "client_ip")
    private String clientIp;

    // 생성자
    public EnvAuditLog() {
    }

    public EnvAuditLog(String settingKey, String oldSettingValue, String newSettingValue,
                       String oldDescription, String newDescription,
                       String actionType, String changedBy) {
        this.settingKey = settingKey;
        this.oldSettingValue = oldSettingValue;
        this.newSettingValue = newSettingValue;
        this.oldDescription = oldDescription;
        this.newDescription = newDescription;
        this.actionType = actionType;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }
}
