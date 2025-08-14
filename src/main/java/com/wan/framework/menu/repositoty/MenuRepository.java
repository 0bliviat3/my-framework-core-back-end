package com.wan.framework.menu.repositoty;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.menu.domain.Menu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, String> {
    Optional<Menu> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);
    List<Menu> findAllByDataStateCodeNot(DataStateCode dataStateCode);

    boolean existsByNameAndDataStateCodeNot(String name, DataStateCode dataStateCode);

    boolean existsByNameAndIdNotAndDataStateCodeNot(String name, Long id, DataStateCode dataStateCode);
}
