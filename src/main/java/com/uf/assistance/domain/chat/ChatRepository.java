package com.uf.assistance.domain.chat;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.message.ChatRespDto;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByAiSubscriptionIdOrderByTimestamp(Long aiSubscriptionId);

    List<Chat> findBySenderIdOrderByTimestamp(Long userId);

    Page<Chat> findByAiSubscriptionId(Long roomId, Pageable pageable);

    @Query("""
    SELECT c FROM Chat c
    WHERE c.id IN (
        SELECT MAX(cm.id) FROM Chat cm
        WHERE cm.sender.id = :userId
        GROUP BY cm.aiSubscription.id
    )
""")
    List<Chat> findLatestMessageByCustomAiIdAndSender(@Param("userId") Long userId);
}

