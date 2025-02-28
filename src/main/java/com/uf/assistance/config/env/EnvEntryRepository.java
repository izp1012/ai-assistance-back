package com.uf.assistance.config.env;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 환경 설정 값에 접근하는 리포지토리
 */
@Repository
public interface EnvEntryRepository extends JpaRepository<EnvEntry, String> {

    /**
     * 키로 설정 값 조회
     */
    Optional<EnvEntry> findByKey(String key);

    /**
     * 키 이름 패턴으로 설정 값 조회
     */
    @Query("SELECT e FROM EnvEntry e WHERE e.key LIKE :pattern")
    List<EnvEntry> findByKeyLike(@Param("pattern") String pattern);

    /**
     * 키 이름이 특정 접두사로 시작하는 설정 값 조회 (그룹별 조회에 사용)
     */
    List<EnvEntry> findByKeyStartingWith(String prefix);

    /**
     * 특정 기간 동안 업데이트된 설정 값 조회
     */
    List<EnvEntry> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * 특정 사용자가 업데이트한 설정 값 조회
     */
    List<EnvEntry> findByUpdatedBy(String updatedBy);

    /**
     * 키 이름이 패턴과 일치하는 설정 값 조회
     */
    @Query("SELECT e FROM EnvEntry e WHERE LOWER(e.key) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<EnvEntry> findByKeyContaining(@Param("pattern") String pattern);

    /**
     * 설명에 특정 텍스트가 포함된 설정 값 조회
     */
    @Query("SELECT e FROM EnvEntry e WHERE LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<EnvEntry> findByDescriptionContaining(@Param("text") String text);

    /**
     * 여러 키에 해당하는 설정 값 조회
     */
    List<EnvEntry> findByKeyIn(List<String> keys);

    /**
     * 특정 키가 있는지 확인
     */
    boolean existsByKey(String key);

    /**
     * 키 이름으로 설정 값 삭제
     */
    void deleteByKey(String key);
}