package com.uf.assistance.domain.keyword;

import com.uf.assistance.domain.chat.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatKeywordRepository extends JpaRepository<ChatKeyword, Long> {
    List<ChatKeyword> findByChat(Chat chat);
}