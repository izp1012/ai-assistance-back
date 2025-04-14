package com.uf.assistance.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {

    @Value("${image.base.url}")
    private String imageBaseUrl;

    public String getImageBaseUrl() {
        return imageBaseUrl;
    }
}
