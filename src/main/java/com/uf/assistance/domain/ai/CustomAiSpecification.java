package com.uf.assistance.domain.ai;

import org.springframework.data.jpa.domain.Specification;

public class CustomAiSpecification {

    // 활성화 여부 조건
    public static Specification<CustomAI> hasActive(Boolean active) {
        return (root, query, criteriaBuilder) ->
                active == null ? null : criteriaBuilder.equal(root.get("active"), active);
    }

    // 숨김 여부 조건
    public static Specification<CustomAI> hasHidden(Boolean hidden) {
        return (root, query, criteriaBuilder) ->
                hidden == null ? null : criteriaBuilder.equal(root.get("hidden"), hidden);
    }

    // 생성자 조건
    public static Specification<CustomAI> hasCreateUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            if (userId == null || userId.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("createdBy").get("id"), userId);
        };
    }
}
