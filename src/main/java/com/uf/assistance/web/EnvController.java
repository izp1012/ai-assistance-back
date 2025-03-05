package com.uf.assistance.web;

import com.uf.assistance.config.env.EnvAuditLog;
import com.uf.assistance.config.env.EnvEntry;
import com.uf.assistance.config.env.EnvHelper;
import com.uf.assistance.config.env.EnvPropertySourceConfig;
import com.uf.assistance.dto.ResponseDto;
import com.uf.assistance.dto.env.EnvDto;
import com.uf.assistance.service.EnvService;
import com.uf.assistance.util.CustomDateUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/env")
public class EnvController {

    private static final Logger logger = LoggerFactory.getLogger(EnvController.class);

    private final EnvService envService;
    private final EnvHelper envHelper;
    private final EnvPropertySourceConfig propertySourceConfig;

    @Autowired
    public EnvController(
            EnvService envService,
            EnvHelper envHelper,
            EnvPropertySourceConfig propertySourceConfig) {
        this.envService = envService;
        this.envHelper = envHelper;
        this.propertySourceConfig = propertySourceConfig;
    }

    /**
     * 모든 설정 값 조회
     */
    @GetMapping
    public ResponseEntity<?> getAllSettings() {
        List<EnvEntry> settings = envService.getAllSettings();
        List<EnvDto> dtos = settings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(new ResponseDto<>(1, "설정 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), dtos), HttpStatus.OK);
    }

    /**
     * 키로 특정 설정 값 조회
     */
    @GetMapping("/{settingKey}")
    public ResponseEntity<?> getSettingByKey(@PathVariable String settingKey) {
        return envService.getSettingByKey(settingKey)
                .map(entry -> {
                    EnvDto dto = convertToDto(entry);
                    return new ResponseEntity<>(new ResponseDto<>(1, "설정 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), dto), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(new ResponseDto<>(-1, "설정을 찾을 수 없음", CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND));
    }

    /**
     * 접두사로 시작하는 설정 값 조회 (그룹별)
     */
    @GetMapping("/prefix/{prefix}")
    public ResponseEntity<?> getSettingsByPrefix(@PathVariable String prefix) {
        List<EnvEntry> settings = envService.getSettingsByPrefix(prefix);
        List<EnvDto> dtos = settings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(new ResponseDto<>(1, "접두사로 설정 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), dtos), HttpStatus.OK);
    }

    /**
     * 패턴으로 설정 값 검색
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchSettings(@RequestParam String pattern) {
        List<EnvEntry> settings = envService.searchSettings(pattern);
        List<EnvDto> dtos = settings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(new ResponseDto<>(1, "설정 검색 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), dtos), HttpStatus.OK);
    }

    /**
     * 설정 값으로 검색
     */
    @GetMapping("/search-by-value")
    public ResponseEntity<?> searchSettingsByValue(@RequestParam String valuePattern) {
        List<EnvEntry> settings = envService.searchSettingsByValue(valuePattern);
        List<EnvDto> dtos = settings.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseEntity<>(new ResponseDto<>(1, "설정 값으로 검색 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), dtos), HttpStatus.OK);
    }

    /**
     * 설정 값 생성
     */
    @PostMapping
    public ResponseEntity<?> createSetting(
            @Valid @RequestBody EnvDto envDto,
            HttpServletRequest request) {

        EnvEntry envEntry = convertToEntity(envDto);

        // 업데이트한 사용자 정보 설정
        envEntry.setUpdatedBy(getUserIdentifier(request));

        EnvEntry saved = envService.saveSetting(envEntry);

        // 환경 헬퍼 캐시 초기화
        envHelper.clearCache();

        return new ResponseEntity<>(new ResponseDto<>(1, "설정 생성 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), convertToDto(saved)), HttpStatus.CREATED);
    }

    /**
     * 설정 값 일괄 생성
     */
    @PostMapping("/batch")
    public ResponseEntity<?> createSettings(
            @Valid @RequestBody List<EnvDto> envDtos,
            HttpServletRequest request) {

        String updatedBy = getUserIdentifier(request);

        List<EnvEntry> entities = envDtos.stream()
                .map(this::convertToEntity)
                .peek(entry -> entry.setUpdatedBy(updatedBy))
                .collect(Collectors.toList());

        List<EnvEntry> saved = envService.saveAllSettings(entities);

        // 환경 헬퍼 캐시 초기화
        envHelper.clearCache();

        List<EnvDto> result = saved.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new ResponseDto<>(1, "설정 일괄 생성 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), result), HttpStatus.CREATED);
    }

    /**
     * 설정 값 업데이트
     */
    @PutMapping("/{settingKey}")
    public ResponseEntity<?> updateSetting(
            @PathVariable String settingKey,
            @Valid @RequestBody EnvDto envDto,
            HttpServletRequest request) {

        if (!settingKey.equals(envDto.getSettingKey())) {
            return new ResponseEntity<>(new ResponseDto<>(-1, "경로 변수와 요청 본문의 키가 일치하지 않음", CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.BAD_REQUEST);
        }

        return envService.getSettingByKey(settingKey)
                .map(existing -> {
                    EnvEntry envEntry = convertToEntity(envDto);
                    envEntry.setUpdatedBy(getUserIdentifier(request));

                    EnvEntry updated = envService.saveSetting(envEntry);

                    // 환경 헬퍼 캐시 초기화
                    envHelper.clearCache();

                    return new ResponseEntity<>(new ResponseDto<>(1, "설정 업데이트 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), convertToDto(updated)), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(new ResponseDto<>(-1, "업데이트할 설정을 찾을 수 없음", CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND));
    }

    /**
     * 설정 값 삭제
     */
    @DeleteMapping("/{settingKey}")
    public ResponseEntity<?> deleteSetting(
            @PathVariable String settingKey,
            HttpServletRequest request) {

        return envService.getSettingByKey(settingKey)
                .map(existing -> {
                    envService.deleteSetting(settingKey);

                    // 환경 헬퍼 캐시 초기화
                    envHelper.clearCache();

                    return new ResponseEntity<>(new ResponseDto<>(1, "설정 삭제 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(new ResponseDto<>(-1, "삭제할 설정을 찾을 수 없음", CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.NOT_FOUND));
    }

    /**
     * 설정 변경 이력 조회
     */
    @GetMapping("/{settingKey}/history")
    public ResponseEntity<?> getSettingHistory(@PathVariable String settingKey) {
        List<EnvAuditLog> history = envService.getSettingHistory(settingKey);
        return new ResponseEntity<>(new ResponseDto<>(1, "설정 이력 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), history), HttpStatus.OK);
    }

    /**
     * 모든 설정 변경 이력 페이징 조회
     */
    @GetMapping("/history")
    public ResponseEntity<?> getAllSettingHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("changedAt").descending());
        Page<EnvAuditLog> history = envService.getAllSettingHistory(pageable);

        return new ResponseEntity<>(new ResponseDto<>(1, "모든 설정 이력 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), history), HttpStatus.OK);
    }

    /**
     * 특정 기간 동안 업데이트된 설정 조회
     */
    @GetMapping("/updated")
    public ResponseEntity<?> getSettingsUpdatedBetween(
            @RequestParam String from,
            @RequestParam String to) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime fromDate = LocalDateTime.parse(from, formatter);
            LocalDateTime toDate = LocalDateTime.parse(to, formatter);

            List<EnvEntry> settings = envService.getSettingsUpdatedBetween(fromDate, toDate);
            List<EnvDto> dtos = settings.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(new ResponseDto<>(1, "기간별 설정 조회 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), dtos), HttpStatus.OK);
        } catch (Exception e) {
            logger.error("날짜 파싱 오류: {}", e.getMessage(), e);
            return new ResponseEntity<>(new ResponseDto<>(-1, "날짜 형식 오류", CustomDateUtil.toStringFormat(LocalDateTime.now()), null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 모든 설정 새로고침
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAllSettings() {
        Map<String, Object> result = propertySourceConfig.forceRefresh();

        // 환경 헬퍼 캐시도 초기화
        envHelper.clearCache();

        return new ResponseEntity<>(new ResponseDto<>(1, "설정 새로고침 성공", CustomDateUtil.toStringFormat(LocalDateTime.now()), result), HttpStatus.OK);
    }

    /**
     * 설정 엔티티를 DTO로 변환
     */
    private EnvDto convertToDto(EnvEntry entity) {
        EnvDto dto = new EnvDto();
        dto.setSettingKey(entity.getSettingKey());
        dto.setSettingValue(entity.getSettingValue());
        dto.setDescription(entity.getDescription());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setUpdatedBy(entity.getUpdatedBy());
        return dto;
    }

    /**
     * DTO를 설정 엔티티로 변환
     */
    private EnvEntry convertToEntity(EnvDto dto) {
        EnvEntry entity = new EnvEntry();
        entity.setSettingKey(dto.getSettingKey());
        entity.setSettingValue(dto.getSettingValue());
        entity.setDescription(dto.getDescription());
        entity.setUpdatedAt(dto.getUpdatedAt() != null ? dto.getUpdatedAt() : LocalDateTime.now());
        entity.setUpdatedBy(dto.getUpdatedBy());
        return entity;
    }

    /**
     * 요청에서 사용자 식별자 추출
     */
    private String getUserIdentifier(HttpServletRequest request) {
        // API 키나 JWT 토큰, 세션 등에서 사용자 정보를 추출
        // 여기서는 간단한 예시만 제공

        String username = Optional.ofNullable(request.getUserPrincipal())
                .map(principal -> principal.getName())
                .orElse("test");

        String remoteAddr = request.getRemoteAddr();

        return username + "@" + remoteAddr;
    }
}