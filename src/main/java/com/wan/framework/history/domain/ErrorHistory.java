package com.wan.framework.history.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static com.wan.framework.base.constant.AbleState.ABLE;
import static com.wan.framework.base.constant.DataStateCode.I;

@Entity
@Table(name = "t_error_history")
@Setter
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorHistory {

    @Id
    @GeneratedValue
    @Column(name = "error_id")
    private Long errorId;

    @Column(name = "request_url")
    private String requestURL;

    @Column(name = "request_param", length = 2500)
    private String requestParam;

    @Column(name = "error_message", length = 2500)
    private String errorMessage;

    @Column(name = "event_time")
    private LocalDateTime eventTime;

    @Column(name = "stack_trace", length = 5000)
    private String stackTrace;

    @PrePersist
    protected void onCreate() {
        this.eventTime = LocalDateTime.now();
    }

}
