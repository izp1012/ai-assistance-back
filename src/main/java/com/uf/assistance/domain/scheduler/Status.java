package com.uf.assistance.domain.scheduler;

import lombok.Getter;

@Getter
public enum Status {
    NEW(1, "신규"),
    ENABLED(2, "활성화"),
    DISABLED(3, "비활성화"),
    PAUSED(4, "일시정지"),
    ERROR(5, "에러"),
    ONETIME(6, "1회성");

    private final int code;
    private final String description;
    Status(int code, String description) {
        this.code = code;
        this.description = description;
    }
}