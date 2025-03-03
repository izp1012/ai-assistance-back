package com.uf.assistance.config.env;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;

import java.util.List;

/**
 * 환경 설정 감사 로그에 접근하는 리포지토리
 */
@Repository
public interface EnvAuditLogRepository extends JpaRepository<EnvAuditLog, Long> {

    /**
     * 특정 설정 키의 변경 이력 조회
     */
    List<EnvAuditLog> findBySettingKeyOrderByChangedAtDesc(String settingKey);

    /**
     * 특정 기간 동안의 설정 변경 이력 조회
     */
    List<EnvAuditLog> findByChangedAtBetweenOrderByChangedAtDesc(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 사용자가 변경한 설정 이력 조회
     */
    List<EnvAuditLog> findByChangedByOrderByChangedAtDesc(String changedBy);

    /**
     * 특정 작업에 대한 설정 변경 이력 조회 (CREATE, UPDATE, DELETE)
     */
    List<EnvAuditLog> findByActionTypeOrderByChangedAtDesc(String actionType);

    /**
     * 페이징 처리된 설정 변경 이력 조회
     */
    Page<EnvAuditLog> findAllByOrderByChangedAtDesc(Pageable pageable);

    /**
     * 특정 설정 키에 대한 페이징 처리된 변경 이력 조회
     */
    Page<EnvAuditLog> findBySettingKeyOrderByChangedAtDesc(String settingKey, Pageable pageable);

    /**
     * 특정 값으로 변경된 설정 이력 조회
     */
    List<EnvAuditLog> findByNewSettingValueOrderByChangedAtDesc(String newSettingValue);

    /**
     * 특정 이전 값이 변경된 설정 이력 조회
     */
    List<EnvAuditLog> findByOldSettingValueOrderByChangedAtDesc(String oldSettingValue);
}