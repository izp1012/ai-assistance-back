package com.uf.assistance.config.interest;


import lombok.Data;

@Data
public class Interest {
    private Long id;
    private String keyword;
    private float[] vector;
    private Integer count;
}
