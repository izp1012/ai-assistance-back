package com.uf.assistance.domain.interest;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interest")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    @Column(columnDefinition = "float8[]")
    private double[] vector;

    @Column(nullable = false)
    private Integer count;

    // 편의 메서드: 호출 횟수 증가
    public void incrementCount() {
        if (this.count == null) {
            this.count = 1;
        } else {
            this.count++;
        }
    }
}