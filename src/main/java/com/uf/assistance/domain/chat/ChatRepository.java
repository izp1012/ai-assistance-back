package com.uf.assistance.domain.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByAiSubscriptionIdOrderByTimestamp(Long aiSubscriptionId);

    List<Chat> findBySender_UserIdOrderByTimestamp(String userId);

    Page<Chat> findByAiSubscriptionId(Long aiSubscriptionId, Pageable pageable);

    @Query("""
    SELECT c FROM Chat c
    WHERE c.id IN (
        SELECT MAX(cm.id) FROM Chat cm
        WHERE cm.sender.userId = :userId
        GROUP BY cm.aiSubscription.id
        )
    """)
    List<Chat> findLatestMessageIdAndSender(@Param("userId") String userId);

    @Query("""
    SELECT c FROM Chat c
    WHERE c.id IN (
        SELECT MAX(cm.id) FROM Chat cm
        WHERE cm.sender.userId = :userId
            AND cm.type = 'ASSISTANT'
        GROUP BY cm.aiSubscription.id
        )
    """)
    List<Chat> findLatestASSISTANTMessageBySender(@Param("userId") String userId);
}

