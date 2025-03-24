package com.uf.assistance.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AISubScriptionReqDto {
    private Long userId;
    private Long customAiId;
}
