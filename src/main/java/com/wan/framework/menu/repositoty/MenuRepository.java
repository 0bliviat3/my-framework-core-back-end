package com.wan.framework.menu.repositoty;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.menu.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);
    List<Menu> findAllByDataStateCodeNot(DataStateCode dataStateCode);

    List<Menu> findAllByDataStateCodeNotAndRoles(DataStateCode dataStateCode, String roles);

    boolean existsByNameAndDataStateCodeNot(String name, DataStateCode dataStateCode);

    boolean existsByNameAndIdNotAndDataStateCodeNot(String name, Long id, DataStateCode dataStateCode);
}
