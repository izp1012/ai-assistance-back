package com.uf.assistance.config.interest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PgVectorConfig {

    private final DataSource dataSource;

    public PgVectorConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    /**
     * 애플리케이션 시작 시 pgvector 확장이 설치되어 있는지 확인합니다.
     */
    @Bean
    public void initPgVector() {
        JdbcTemplate jdbcTemplate = jdbcTemplate();

        // 테이블이 없으면 생성
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS interest (" +
                        "  id SERIAL PRIMARY KEY," +
                        "  keyword VARCHAR(255) NOT NULL," +
                        "  vector vector(384)," +  // 사용하는 모델에 맞게 벡터 차원 조정 필요
                        "  count INTEGER NOT NULL DEFAULT 1" +
                        ")"
        );

        // 벡터 인덱스 생성 (선택사항 - 대용량 데이터일 경우 추천)
        jdbcTemplate.execute(
                "CREATE INDEX IF NOT EXISTS interest_vector_idx ON interest USING ivfflat (vector vector_cosine_ops)"
        );
    }
}