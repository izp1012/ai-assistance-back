package com.uf.assistance.domain.chat;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.domain.user.UserEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_tb")
@Entity
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //000001
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private User sender;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 1000)
    @Column(nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column
    private ChatStatus Status;

    @Column
    private Long roomId;

    @CreatedDate
    private LocalDateTime sendTime;

    @Builder
    public Chat(User sender, String text, ChatStatus status, Long roomId) {
        this.sender = sender;
        this.text = text;
        this.Status = status;
        this.roomId = roomId;
        this.sendTime = LocalDateTime.now();
    }
}
