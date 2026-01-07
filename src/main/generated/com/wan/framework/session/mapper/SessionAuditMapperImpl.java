package com.wan.framework.session.mapper;

import com.wan.framework.session.domain.SessionAudit;
import com.wan.framework.session.dto.SessionAuditDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-07T13:23:51+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class SessionAuditMapperImpl implements SessionAuditMapper {

    @Override
    public SessionAuditDTO toDto(SessionAudit sessionAudit) {
        if ( sessionAudit == null ) {
            return null;
        }

        SessionAuditDTO.SessionAuditDTOBuilder sessionAuditDTO = SessionAuditDTO.builder();

        sessionAuditDTO.id( sessionAudit.getId() );
        sessionAuditDTO.sessionId( sessionAudit.getSessionId() );
        sessionAuditDTO.userId( sessionAudit.getUserId() );
        sessionAuditDTO.eventType( sessionAudit.getEventType() );
        sessionAuditDTO.ipAddress( sessionAudit.getIpAddress() );
        sessionAuditDTO.userAgent( sessionAudit.getUserAgent() );
        sessionAuditDTO.eventTime( sessionAudit.getEventTime() );
        sessionAuditDTO.additionalInfo( sessionAudit.getAdditionalInfo() );
        sessionAuditDTO.createTime( sessionAudit.getCreateTime() );
        sessionAuditDTO.modifiedTime( sessionAudit.getModifiedTime() );
        sessionAuditDTO.dataState( sessionAudit.getDataState() );

        return sessionAuditDTO.build();
    }

    @Override
    public SessionAudit toEntity(SessionAuditDTO dto) {
        if ( dto == null ) {
            return null;
        }

        SessionAudit.SessionAuditBuilder sessionAudit = SessionAudit.builder();

        sessionAudit.id( dto.getId() );
        sessionAudit.sessionId( dto.getSessionId() );
        sessionAudit.userId( dto.getUserId() );
        sessionAudit.eventType( dto.getEventType() );
        sessionAudit.ipAddress( dto.getIpAddress() );
        sessionAudit.userAgent( dto.getUserAgent() );
        sessionAudit.eventTime( dto.getEventTime() );
        sessionAudit.additionalInfo( dto.getAdditionalInfo() );

        return sessionAudit.build();
    }
}
