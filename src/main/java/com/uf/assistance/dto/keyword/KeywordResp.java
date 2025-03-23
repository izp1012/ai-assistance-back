package com.uf.assistance.dto.keyword;

import lombok.Data;

import java.util.List;

@Data
public class KeywordResp {
    private List<String> keywords;
    private List<Double> scores;
    private Integer keyword_count;
    private Boolean is_dynamic;
    private Integer top_n;
}
