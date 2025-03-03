package com.uf.assistance.dto.env;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 환경 설정 값 전송 객체
 */
@Getter
@Setter
public class EnvDto {

    @NotBlank(message = "설정 키는 필수입니다")
    private String settingKey;

    private String settingValue;

    private String description;

    private LocalDateTime updatedAt;

    private String updatedBy;
}
