package com.wan.framework.session.mapper;

import com.wan.framework.session.domain.SessionAudit;
import com.wan.framework.session.dto.SessionAuditDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 세션 감사 로그 Mapper
 */
@Mapper(componentModel = "spring")
public interface SessionAuditMapper {

    SessionAuditDTO toDto(SessionAudit sessionAudit);

    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "modifiedTime", ignore = true)
    @Mapping(target = "dataState", ignore = true)
    SessionAudit toEntity(SessionAuditDTO dto);
}
