package com.uf.assistance.domain.keyword;


import jakarta.persistence.*;
import lombok.*;

/**
 * 관심사 키워드와 벡터 정보를 담는 모델 클래스
 * JPA와 JDBC 템플릿을 함께 사용하는 하이브리드 방식
 */
@Entity
@Table(name = "interest")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "vector") // 벡터는 크기가 크므로 toString에서 제외
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;

    // vector 필드는 JPA에서 관리하지 않음
    @Transient
    private float[] vector;

}