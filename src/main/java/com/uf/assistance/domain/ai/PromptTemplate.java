package com.uf.assistance.domain.ai;

import com.uf.assistance.domain.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
 * 프롬프트 템플릿 엔티티
 * 운영자 및 개발자가 정의하는 AI 프롬프트 템플릿
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "prompt_template_tb")
@Entity
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String description;

    @NotBlank
    @Size(max = 10000)
    @Column(nullable = false, length = 10000)
    private String template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromptType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private boolean isActive;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    /**
     * 변수를 값으로 대체하여 완성된 프롬프트 생성
     * @param variables 변수 맵 (변수명 -> 값)
     * @return 변수가 대체된 완성된 프롬프트
     */
    public String format(Map<String, String> variables) {
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
}