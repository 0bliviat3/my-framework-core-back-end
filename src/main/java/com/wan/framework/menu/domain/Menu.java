package com.wan.framework.menu.domain;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.program.domain.Program;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.AbleState.ABLE;
import static com.wan.framework.base.constant.DataStateCode.I;
import static com.wan.framework.base.constant.DataStateCode.U;

@Entity
@Table(name = "t_menu")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Menu parent;

    @ManyToOne
    @JoinColumn(name = "program_id", referencedColumnName = "pid")
    private Program program;

    @Column(name = "menu_name", nullable = false, length = 100)
    private String name;
    private String type;

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
        this.dataStateCode = U;
    }
}
