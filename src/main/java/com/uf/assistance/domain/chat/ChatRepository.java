package com.uf.assistance.domain.chat;

import com.uf.assistance.domain.room.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByRoomIdOrderByTimestamp(Long roomId);

    Page<Chat> findByRoomId(Long roomId, Pageable pageable);
}

