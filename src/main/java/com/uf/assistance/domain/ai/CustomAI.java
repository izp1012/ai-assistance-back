package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.user.User;
import com.uf.assistance.dto.ai.CustomAIReqDto;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CustomAI 엔티티
 * 관리자가 정의한 AI 를 가져와
 * 사용자가 Customize 하는 AI 정의
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "custom_ai_tb")
@Entity
public class CustomAI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @Size(max = 500)
    @Column
    private String description;

    private String imageUrl;  // 파일 경로 or URL 저장

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_prompt")
    private BaseAI baseAI;

    @Size(max = 5000)
    @Column(length = 5000)
    private String customPrompt;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean hidden;

    @Column(nullable = false)
    @ColumnDefault("true")
    private boolean active;

    @Column(nullable = false)
    @ColumnDefault("false")
    private boolean approved;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @CreatedBy
    private User createdBy;

    @CreatedDate
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    @LastModifiedBy
    private User updatedBy;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        if (updatedBy == null) {
            updatedBy = createdBy;  // 생성 시 updatedAt을 createdAt과 동일하게 설정
        }
    }
    /**
     * 변수를 값으로 대체하여 완성된 프롬프트 생성
     * @param variables 변수 맵 (변수명 -> 값)
     * @return 변수가 대체된 완성된 프롬프트
     */
    public String format(Map<String, String> variables) {

        String template = "";
        template = baseAI.getBasePrompt() + customPrompt;
        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        Pattern pattern = Pattern.compile("\\{\\{(\\w+)\\}\\}");
        Matcher matcher = pattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = variables.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 기본 프롬프트와 사용자 정의 프롬프트 결합
     * @param basePrompt 기본 프롬프트
     * @param customPrompt 사용자 정의 프롬프트
     * @return 결합된 프롬프트
     */
    public static String combine(String basePrompt, String customPrompt) {
        if (customPrompt == null || customPrompt.trim().isEmpty()) {
            return basePrompt;
        }

        return basePrompt + "\n\n" + customPrompt;
    }

    public CustomAI update(CustomAIReqDto customAIReqDto, BaseAI baseAI, User updatedByuser, String imageUrl) {
        this.name = customAIReqDto.getName();
        this.description = customAIReqDto.getDescription();
        this.customPrompt = customAIReqDto.getCustomPrompt();
        this.imageUrl = imageUrl;
        this.baseAI = baseAI;
        this.active = customAIReqDto.isActive();
        this.hidden = customAIReqDto.isHidden();
        this.approved = customAIReqDto.isApproved();
        this.updatedBy = updatedByuser;
        return this;
    }
}

