package com.uf.assistance.dto.keyword;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeywordReq {
    private String text;
    private Integer top_n;
    private Integer min_keywords;
    private Integer max_keywords;
    private Integer chars_per_keyword;
    private Integer words_per_keyword;
    private Integer min_ngram;
    private Integer max_ngram;
}