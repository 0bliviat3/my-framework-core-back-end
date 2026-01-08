package com.wan.framework.program.domain;

import com.wan.framework.permission.domain.ApiRegistry;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Program ↔ API 매핑 엔티티
 * - 프로그램(화면)이 사용하는 API 목록 관리
 * - Menu → Program → API 연계로 메뉴별 필요 권한 자동 계산
 */
@Entity
@Table(name = "t_program_api_mapping",
    uniqueConstraints = @UniqueConstraint(columnNames = {"program_id", "api_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ProgramApiMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiRegistry apiRegistry;

    @Column(name = "required", nullable = false)
    @Builder.Default
    private Boolean required = true;  // 필수 API 여부 (화면 렌더링에 필수)

    @Column(name = "description", length = 500)
    private String description;  // 이 API를 사용하는 이유

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.required == null) {
            this.required = true;
        }
    }

    // 비즈니스 메서드
    public void setRequired(Boolean required) {
        this.required = required;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
