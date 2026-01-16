package com.wan.framework.base.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA Auditing 기반 Entity
 * - 생성자/수정자 자동 기록
 * - 생성일시/수정일시 자동 기록
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    /**
     * 생성자 (세션에서 자동 설정)
     */
    @CreatedBy
    @Column(name = "created_by", nullable = true, length = 50, updatable = false)
    private String createdBy;

    /**
     * 생성일시 (자동 설정)
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정자 (세션에서 자동 설정)
     */
    @LastModifiedBy
    @Column(name = "updated_by", nullable = true, length = 50)
    private String updatedBy;

    /**
     * 수정일시 (자동 설정)
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
