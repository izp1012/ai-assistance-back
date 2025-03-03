package com.uf.assistance.config.env;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 환경 설정 값을 저장하는 엔티티
 */
@Entity
@Table(name = "env_settings")
@Getter
@Setter
public class EnvEntry {

    @Id
    @NotBlank
    @Column(name = "setting_key", nullable = false, unique = true)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // 생성자
    public EnvEntry() {
    }

    public EnvEntry(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public EnvEntry(String settingKey, String settingValue, String description, String updatedBy) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "EnvEntry{" +
                "settingKey='" + settingKey + '\'' +
                ", settingValue='" + (settingKey.toLowerCase().contains("key") ||
                settingKey.toLowerCase().contains("secret") ||
                settingKey.toLowerCase().contains("password") ? "*****" : settingValue) + '\'' +
                ", description='" + description + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}