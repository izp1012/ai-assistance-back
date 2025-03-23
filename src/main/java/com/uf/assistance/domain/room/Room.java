package com.uf.assistance.domain.room;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@Table(name = "room_tb")
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private String name;

    @NotNull
    @Column
    private String description;

    @CreatedDate
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

}