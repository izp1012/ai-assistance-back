package com.uf.assistance.domain.chat;

import com.uf.assistance.domain.ai.AISubscription;
import com.uf.assistance.domain.keyword.ChatKeyword;
import com.uf.assistance.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_tb")
@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //000001
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  //User 와 다대일관계
    @NotNull
    @JoinColumn(name = "sender_id")
    private User sender;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 1000)
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column
    private ChatStatus Status;

    @ManyToOne(fetch = FetchType.LAZY)  //AISubscription 과 다대일 관계
    @JoinColumn(name = "subscription_id")
    private AISubscription aiSubscription;

    @CreatedDate
    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ChatKeyword> chatKeywords = new ArrayList<>();
}
