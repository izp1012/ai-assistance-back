package com.uf.assistance.config.env;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 환경 설정 값이 변경되었을 때 발행되는 이벤트
 */
@Getter
public class EnvChangeEvent extends ApplicationEvent {

    private final String settingKey;
    private final String oldSettingValue;
    private final String newSettingValue;
    private final EventType eventType;

    /**
     * 설정 변경 이벤트 생성자
     */
    public EnvChangeEvent(Object source, String settingKey, String oldSettingValue, String newSettingValue, EventType eventType) {
        super(source);
        this.settingKey = settingKey;
        this.oldSettingValue = oldSettingValue;
        this.newSettingValue = newSettingValue;
        this.eventType = eventType;
    }

    /**
     * 간단한 생성자 - 이벤트 타입 자동 결정
     */
    public EnvChangeEvent(Object source, String settingKey, String oldSettingValue, String newSettingValue) {
        super(source);
        this.settingKey = settingKey;
        this.oldSettingValue = oldSettingValue;
        this.newSettingValue = newSettingValue;

        if (oldSettingValue == null && newSettingValue != null) {
            this.eventType = EventType.CREATE;
        } else if (oldSettingValue != null && newSettingValue == null) {
            this.eventType = EventType.DELETE;
        } else {
            this.eventType = EventType.UPDATE;
        }
    }

    /**
     * 값이 실제로 변경되었는지 확인
     */
    public boolean isValueChanged() {
        if (oldSettingValue == null && newSettingValue == null) {
            return false;
        }
        if (oldSettingValue == null || newSettingValue == null) {
            return true;
        }
        return !oldSettingValue.equals(newSettingValue);
    }

    @Override
    public String toString() {
        return "EnvChangeEvent{" +
                "settingKey='" + settingKey + '\'' +
                ", eventType=" + eventType +
                ", oldSettingValue='" + (isSensitiveKey(settingKey) ? "*****" : oldSettingValue) + '\'' +
                ", newSettingValue='" + (isSensitiveKey(settingKey) ? "*****" : newSettingValue) + '\'' +
                '}';
    }

    /**
     * 민감한 정보가 포함된 키인지 확인
     */
    private boolean isSensitiveKey(String settingKey) {
        if (settingKey == null) {
            return false;
        }
        String lowerKey = settingKey.toLowerCase();
        return lowerKey.contains("key") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("password") ||
                lowerKey.contains("token");
    }
}
