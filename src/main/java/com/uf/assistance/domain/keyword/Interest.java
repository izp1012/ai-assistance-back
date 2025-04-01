package com.uf.assistance.domain.keyword;


import lombok.*;

/**
 * 관심사 키워드와 벡터 정보를 담는 모델 클래스
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "vector") // 벡터는 크기가 크므로 toString에서 제외
public class Interest {

    private Long id;
    private String keyword;
    private float[] vector;
    private Integer count;

    /**
     * 카운트 증가 메서드
     */
    public void incrementCount() {
        if (this.count == null) {
            this.count = 1;
        } else {
            this.count++;
        }
    }
}