package com.uf.assistance.domain.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uf.assistance.domain.keyword.ChatKeyword;
import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chat_tb")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "sender_id")
    private User sender;

    @NotBlank
    @Size(min = 1, max = 1000)
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private ChatStatus Status;

    @CreatedDate
    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ChatKeyword> chatKeywords = new ArrayList<>();
}
