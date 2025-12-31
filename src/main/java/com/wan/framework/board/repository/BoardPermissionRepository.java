package com.wan.framework.board.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.PermissionType;
import com.wan.framework.board.domain.BoardPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardPermissionRepository extends JpaRepository<BoardPermission, Long> {

    Optional<BoardPermission> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);

    List<BoardPermission> findByBoardMetaIdAndDataStateCodeNot(Long boardMetaId, DataStateCode dataStateCode);

    List<BoardPermission> findByBoardMetaIdAndPermissionTypeAndDataStateCodeNot(
        Long boardMetaId, PermissionType permissionType, DataStateCode dataStateCode);

    Optional<BoardPermission> findByBoardMetaIdAndRoleOrUserIdAndPermissionTypeAndDataStateCodeNot(
        Long boardMetaId, String roleOrUserId, PermissionType permissionType, DataStateCode dataStateCode);

    @Query("SELECT bp FROM BoardPermission bp WHERE bp.boardMeta.id = :boardMetaId " +
           "AND bp.roleOrUserId IN :roleOrUserIds AND bp.permissionType = :permissionType " +
           "AND bp.dataStateCode <> :deletedCode")
    List<BoardPermission> findByBoardMetaAndRolesOrUserId(
        @Param("boardMetaId") Long boardMetaId,
        @Param("roleOrUserIds") List<String> roleOrUserIds,
        @Param("permissionType") PermissionType permissionType,
        @Param("deletedCode") DataStateCode deletedCode);
}
