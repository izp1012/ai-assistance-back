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
    @Column(nullable = false, unique = true)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // 생성자
    public EnvEntry() {
    }

    public EnvEntry(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public EnvEntry(String key, String value, String description, String updatedBy) {
        this.key = key;
        this.value = value;
        this.description = description;
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "EnvEntry{" +
                "key='" + key + '\'' +
                ", value='" + (
                    key.toLowerCase().contains("key") ||
                    key.toLowerCase().contains("secret") ||
                    key.toLowerCase().contains("password") ? "*****" : value) + '\'' +
                ", description='" + description + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
