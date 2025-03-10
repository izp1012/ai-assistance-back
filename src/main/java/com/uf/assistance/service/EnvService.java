package com.uf.assistance.service;

import com.uf.assistance.config.env.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
/*
 * 환경 설정 값을 관리하는 서비스
 */
@Service
public class EnvService {

    private static final Logger logger = LoggerFactory.getLogger(EnvService.class);

    private final EnvEntryRepository envRepository;
    private final EnvAuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public EnvService(
            EnvEntryRepository envRepository,
            EnvAuditLogRepository auditLogRepository,
            ApplicationEventPublisher eventPublisher) {
        this.envRepository = envRepository;
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 모든 설정 값 조회
     */
    @Transactional(readOnly = true)
    public List<EnvEntry> getAllSettings() {
        return envRepository.findAll();
    }

    /**
     * 키로 설정 값 조회
     */
    @Transactional(readOnly = true)
    public Optional<EnvEntry> getSettingByKey(String settingKey) {
        return envRepository.findBySettingKey(settingKey);
    }

    /**
     * 키로 설정 값 문자열 조회
     */
    @Transactional(readOnly = true)
    public String getSettingValueByKey(String settingKey) {
        return envRepository.findBySettingKey(settingKey)
                .map(EnvEntry::getSettingValue)
                .orElse(null);
    }

    /**
     * 키로 설정 값 조회 (기본값 지원)
     */
    @Transactional(readOnly = true)
    public String getSettingValueByKey(String settingKey, String defaultValue) {
        return envRepository.findBySettingKey(settingKey)
                .map(EnvEntry::getSettingValue)
                .orElse(defaultValue);
    }

    /**
     * 설정 값 저장 또는 업데이트
     */
    @Transactional
    public EnvEntry saveSetting(EnvEntry envEntry) {
        logger.info("설정 저장: {}", envEntry.getSettingKey());

        // 이전 설정 조회
        Optional<EnvEntry> existingEntry = envRepository.findBySettingKey(envEntry.getSettingKey());
        boolean isNew = !existingEntry.isPresent();

        String oldSettingValue = null;
        String oldDescription = null;

        if (!isNew) {
            EnvEntry existing = existingEntry.get();
            oldSettingValue = existing.getSettingValue();
            oldDescription = existing.getDescription();
        }

        // 마지막 업데이트 시간 설정
        envEntry.setUpdatedAt(LocalDateTime.now());

        // 데이터베이스에 저장
        EnvEntry saved = envRepository.save(envEntry);

        // 감사 로그 저장
        String actionType = isNew ? "CREATE" : "UPDATE";
        EnvAuditLog auditLog = new EnvAuditLog(
                envEntry.getSettingKey(),
                oldSettingValue,
                envEntry.getSettingValue(),
                oldDescription,
                envEntry.getDescription(),
                actionType,
                envEntry.getUpdatedBy()
        );
        auditLogRepository.save(auditLog);

        // 설정 변경 이벤트 발행 (값이나 설명이 변경된 경우만)
        if (!isNew && (
                isValueChanged(oldSettingValue, envEntry.getSettingValue()) ||
                        isValueChanged(oldDescription, envEntry.getDescription())
        )) {
            EnvChangeEvent event = new EnvChangeEvent(
                    this,
                    envEntry.getSettingKey(),
                    oldSettingValue,
                    envEntry.getSettingValue(),
                    isNew ? EventType.CREATE : EventType.UPDATE
            );

            eventPublisher.publishEvent(event);
            logger.debug("설정 변경 이벤트 발행: {}", envEntry.getSettingKey());
        }

        return saved;
    }

    /**
     * 값이 변경되었는지 확인
     */
    private boolean isValueChanged(String oldValue, String newValue) {
        if (oldValue == null && newValue == null) {
            return false;
        }
        if (oldValue == null || newValue == null) {
            return true;
        }
        return !oldValue.equals(newValue);
    }

    /**
     * 여러 설정 값 일괄 저장
     */
    @Transactional
    public List<EnvEntry> saveAllSettings(List<EnvEntry> envEntries) {
        return envEntries.stream()
                .map(this::saveSetting)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 키로 설정 값 삭제
     */
    @Transactional
    public void deleteSetting(String settingKey) {
        logger.info("설정 삭제: {}", settingKey);

        // 이전 값 조회
        Optional<EnvEntry> existingEntry = envRepository.findBySettingKey(settingKey);

        if (existingEntry.isPresent()) {
            EnvEntry existing = existingEntry.get();
            String oldSettingValue = existing.getSettingValue();
            String oldDescription = existing.getDescription();

            // 데이터베이스에서 삭제
            envRepository.deleteBySettingKey(settingKey);

            // 감사 로그 저장
            EnvAuditLog auditLog = new EnvAuditLog(
                    settingKey,
                    oldSettingValue,
                    null,
                    oldDescription,
                    null,
                    "DELETE",
                    "system"
            );
            auditLogRepository.save(auditLog);

            // 설정 변경 이벤트 발행
            EnvChangeEvent event = new EnvChangeEvent(
                    this,
                    settingKey,
                    oldSettingValue,
                    null,
                    EventType.DELETE
            );

            eventPublisher.publishEvent(event);
            logger.debug("설정 삭제 이벤트 발행: {}", settingKey);
        }
    }

    /**
     * 접두사로 시작하는 설정 값 조회 (그룹별)
     */
    public List<EnvEntry> getSettingsByPrefix(String prefix) {
        return envRepository.findBySettingKeyStartingWith(prefix);
    }

    /**
     * 설정 값 존재 여부 확인
     */
    public boolean settingExists(String settingKey) {
        return envRepository.existsBySettingKey(settingKey);
    }

    /**
     * 패턴으로 설정 검색
     */
    public List<EnvEntry> searchSettings(String pattern) {
        return envRepository.findBySettingKeyContaining(pattern);
    }

    /**
     * 특정 값을 가진 설정 검색
     */
    public List<EnvEntry> searchSettingsByValue(String settingValue) {
        return envRepository.findBySettingValue(settingValue);
    }

    /**
     * 특정 값 패턴을 포함하는 설정 검색
     */
    public List<EnvEntry> searchSettingsByValuePattern(String valuePattern) {
        return envRepository.findBySettingValueContaining(valuePattern);
    }

    /**
     * 키와 값을 기반으로 설정 검색
     */
    public Map<String, Object> getSettingWithDetails(String settingKey) {
        Map<String, Object> result = new HashMap<>();

        Optional<EnvEntry> entry = envRepository.findBySettingKey(settingKey);
        if (entry.isPresent()) {
            EnvEntry setting = entry.get();
            result.put("settingKey", setting.getSettingKey());
            result.put("settingValue", setting.getSettingValue());
            result.put("description", setting.getDescription());
            result.put("updatedAt", setting.getUpdatedAt());
            result.put("updatedBy", setting.getUpdatedBy());
        }

        return result;
    }

    /**
     * 특정 기간 동안 업데이트된 설정 조회
     */
    public List<EnvEntry> getSettingsUpdatedBetween(LocalDateTime start, LocalDateTime end) {
        return envRepository.findByUpdatedAtBetween(start, end);
    }

    /**
     * 특정 사용자가 업데이트한 설정 조회
     */
    public List<EnvEntry> getSettingsUpdatedBy(String user) {
        return envRepository.findByUpdatedBy(user);
    }

    // 감사 로그 관련 메서드

    /**
     * 설정 변경 이력 조회
     */
    public List<EnvAuditLog> getSettingHistory(String settingKey) {
        return auditLogRepository.findBySettingKeyOrderByChangedAtDesc(settingKey);
    }

    /**
     * 모든 설정 변경 이력 페이징 조회
     */
    public Page<EnvAuditLog> getAllSettingHistory(Pageable pageable) {
        return auditLogRepository.findAllByOrderByChangedAtDesc(pageable);
    }

    /**
     * 특정 기간 동안의 설정 변경 이력 조회
     */
    public List<EnvAuditLog> getSettingHistoryBetween(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByChangedAtBetweenOrderByChangedAtDesc(start, end);
    }

    /**
     * 특정 사용자의 설정 변경 이력 조회
     */
    public List<EnvAuditLog> getSettingHistoryByUser(String username) {
        return auditLogRepository.findByChangedByOrderByChangedAtDesc(username);
    }

    /**
     * 특정 작업 유형의 설정 변경 이력 조회
     */
    public List<EnvAuditLog> getSettingHistoryByActionType(String actionType) {
        return auditLogRepository.findByActionTypeOrderByChangedAtDesc(actionType);
    }

    /**
     * 특정 설정 값으로 변경된 이력 조회
     */
    public List<EnvAuditLog> getSettingHistoryByNewValue(String newSettingValue) {
        return auditLogRepository.findByNewSettingValueOrderByChangedAtDesc(newSettingValue);
    }
}