package com.uf.assistance.domain.chat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.uf.assistance.domain.room.Room;
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

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

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

    @ManyToOne(fetch = FetchType.LAZY)  //Room 과 다대일 관계
    @JoinColumn(name = "room_id")
    private Room room;

    @CreatedDate
    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private MessageType type;

}
