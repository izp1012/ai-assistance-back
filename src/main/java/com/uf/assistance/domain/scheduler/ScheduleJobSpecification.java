package com.uf.assistance.domain.scheduler;

import org.springframework.data.jpa.domain.Specification;

public class ScheduleJobSpecification {

    // 사용자 ID 조건
    public static Specification<ScheduledJob> hasUserId(String userId) {
        return (root, query, criteriaBuilder) ->
                userId == null ? null : criteriaBuilder.equal(root.get("user").get("userId"), userId);
    }

    // 숨김 여부 조건
    public static Specification<ScheduledJob> hasAiSubscriptionId(Long aiSubscriptionId) {
        return (root, query, criteriaBuilder) ->
                aiSubscriptionId == null ? null : criteriaBuilder.equal(root.get("aiSubscription").get("id"), aiSubscriptionId);
    }
}
