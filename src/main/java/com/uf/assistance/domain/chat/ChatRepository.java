package com.uf.assistance.domain.chat;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByAiSubscriptionIdOrderByTimestamp(Long aiSubscriptionId);

    Page<Chat> findByAiSubscriptionId(Long roomId, Pageable pageable);
}

