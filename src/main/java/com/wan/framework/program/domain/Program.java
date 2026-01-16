package com.wan.framework.program.domain;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.AbleState.ABLE;
import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_program")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "program_id")
    private Long id;

    @Column(name = "program_name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "front_path", length = 255)
    private String frontPath;

    @Column(name = "program_path", nullable = true, length = 255)
    private String path;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "data_code", nullable = false)
    @Enumerated(EnumType.STRING)
    private DataStateCode dataStateCode;

    @Column(name = "able_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private AbleState ableState;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.ableState = ABLE;
        this.dataStateCode = I;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
