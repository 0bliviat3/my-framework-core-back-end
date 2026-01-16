package com.wan.framework.menu.repositoty;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);
    List<Menu> findAllByDataStateCodeNot(DataStateCode dataStateCode);

    boolean existsByNameAndDataStateCodeNot(String name, DataStateCode dataStateCode);

    boolean existsByNameAndIdNotAndDataStateCodeNot(String name, Long id, DataStateCode dataStateCode);

    /**
     * 권한 기반 메뉴 조회 (Role Entity Join)
     * - Role Entity FK 관계 사용
     */
    @Query("SELECT DISTINCT m FROM Menu m " +
            "JOIN m.roles r " +
            "WHERE r.roleCode IN :roleCodes " +
            "AND m.dataStateCode != :dataStateCode")
    List<Menu> findByRoleCodes(
            @Param("roleCodes") List<String> roleCodes,
            @Param("dataStateCode") DataStateCode dataStateCode
    );
}
