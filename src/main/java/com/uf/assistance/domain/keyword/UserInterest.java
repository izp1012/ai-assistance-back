package com.uf.assistance.domain.keyword;

import com.uf.assistance.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_interest",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "interest_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

    @Column(nullable = false)
    private int count;

    /**
     * 카운트 증가 메서드
     */
    public void incrementCount() {
        this.count++;
    }
}