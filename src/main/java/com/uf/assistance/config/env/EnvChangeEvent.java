package com.uf.assistance.config.env;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 환경 설정 값이 변경되었을 때 발행되는 이벤트
 */
@Getter
public class EnvChangeEvent extends ApplicationEvent {
    private final String key;
    private final String oldValue;
    private final String newValue;
    private final EventType type;

    /**
     * 설정 변경 이벤트 생성자
     */
    public EnvChangeEvent(Object source, String key, String oldValue, String newValue, EventType type) {
        super(source);
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.type = type;
    }

    /**
     * 간단한 생성자 - 이벤트 타입 자동 결정
     */
    public EnvChangeEvent(Object source, String key, String oldValue, String newValue) {
        super(source);
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;

        if (oldValue == null && newValue != null) {
            this.type = EventType.CREATE;
        } else if (oldValue != null && newValue == null) {
            this.type = EventType.DELETE;
        } else {
            this.type = EventType.UPDATE;
        }
    }
    /**
     * 값이 실제로 변경되었는지 확인
     */
    public boolean isValueChanged() {
        if (oldValue == null && newValue == null) {
            return false;
        }
        if (oldValue == null || newValue == null) {
            return true;
        }
        return !oldValue.equals(newValue);
    }

    @Override
    public String toString() {
        return "EnvChangeEvent{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", oldValue='" + (isSensitiveKey(key) ? "*****" : oldValue) + '\'' +
                ", newValue='" + (isSensitiveKey(key) ? "*****" : newValue) + '\'' +
                '}';
    }

    /**
     * 민감한 정보가 포함된 키인지 확인
     */
    private boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("key") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("password") ||
                lowerKey.contains("token");
    }
}
