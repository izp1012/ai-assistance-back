package com.uf.assistance.domain.ai;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * CustomAI 엔티티에 대한 데이터 액세스 인터페이스
 */
@Repository
public interface CustomAIRepository extends JpaRepository<CustomAI, Long>, JpaSpecificationExecutor {

    @Override
    List<CustomAI> findAll();

    List<CustomAI> findAll(Specification spec);

    Page<CustomAI> findAll(Specification spec, Pageable pageable);
}