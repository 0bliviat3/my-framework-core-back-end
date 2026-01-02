package com.wan.framework.apikey.dto;

import com.wan.framework.base.constant.AbleState;
import com.wan.framework.base.constant.DataStateCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyDTO {

    private Long id;
    private String rawApiKey; // 생성 시에만 노출, 이후 null
    private String apiKeyPrefix; // 검색/표시용 (예: "sk_test_1234")
    private String description;
    private String createdBy;
    private String updatedBy;
    private AbleState ableState;
    private LocalDateTime expiredAt;
    private LocalDateTime lastUsedAt;
    private Long usageCount;
    private DataStateCode dataStateCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    // 권한 목록
    private List<String> permissions;
}
