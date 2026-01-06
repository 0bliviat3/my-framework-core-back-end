package com.wan.framework.board.mapper;

import com.wan.framework.board.domain.BoardMeta;
import com.wan.framework.board.domain.BoardPermission;
import com.wan.framework.board.dto.BoardPermissionDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-01-06T10:51:58+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.15 (OpenLogic)"
)
@Component
public class BoardPermissionMapperImpl implements BoardPermissionMapper {

    @Override
    public BoardPermissionDTO toDto(BoardPermission entity) {
        if ( entity == null ) {
            return null;
        }

        BoardPermissionDTO.BoardPermissionDTOBuilder boardPermissionDTO = BoardPermissionDTO.builder();

        boardPermissionDTO.boardMetaId( entityBoardMetaId( entity ) );
        boardPermissionDTO.id( entity.getId() );
        boardPermissionDTO.roleOrUserId( entity.getRoleOrUserId() );
        boardPermissionDTO.isRole( entity.getIsRole() );
        boardPermissionDTO.permissionType( entity.getPermissionType() );
        boardPermissionDTO.allowed( entity.getAllowed() );
        boardPermissionDTO.dataStateCode( entity.getDataStateCode() );

        return boardPermissionDTO.build();
    }

    @Override
    public BoardPermission toEntity(BoardPermissionDTO dto) {
        if ( dto == null ) {
            return null;
        }

        BoardPermission.BoardPermissionBuilder boardPermission = BoardPermission.builder();

        boardPermission.boardMeta( boardPermissionDTOToBoardMeta( dto ) );
        boardPermission.id( dto.getId() );
        boardPermission.roleOrUserId( dto.getRoleOrUserId() );
        boardPermission.isRole( dto.getIsRole() );
        boardPermission.permissionType( dto.getPermissionType() );
        boardPermission.allowed( dto.getAllowed() );
        boardPermission.dataStateCode( dto.getDataStateCode() );

        return boardPermission.build();
    }

    @Override
    public void updateEntityFromDto(BoardPermissionDTO dto, BoardPermission entity) {
        if ( dto == null ) {
            return;
        }

        if ( dto.getRoleOrUserId() != null ) {
            entity.setRoleOrUserId( dto.getRoleOrUserId() );
        }
        if ( dto.getIsRole() != null ) {
            entity.setIsRole( dto.getIsRole() );
        }
        if ( dto.getPermissionType() != null ) {
            entity.setPermissionType( dto.getPermissionType() );
        }
        if ( dto.getAllowed() != null ) {
            entity.setAllowed( dto.getAllowed() );
        }
    }

    private Long entityBoardMetaId(BoardPermission boardPermission) {
        if ( boardPermission == null ) {
            return null;
        }
        BoardMeta boardMeta = boardPermission.getBoardMeta();
        if ( boardMeta == null ) {
            return null;
        }
        Long id = boardMeta.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected BoardMeta boardPermissionDTOToBoardMeta(BoardPermissionDTO boardPermissionDTO) {
        if ( boardPermissionDTO == null ) {
            return null;
        }

        BoardMeta.BoardMetaBuilder boardMeta = BoardMeta.builder();

        boardMeta.id( boardPermissionDTO.getBoardMetaId() );

        return boardMeta.build();
    }
}
