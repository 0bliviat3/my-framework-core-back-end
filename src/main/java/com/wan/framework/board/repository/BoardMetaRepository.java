package com.wan.framework.board.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.domain.BoardMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardMetaRepository extends JpaRepository<BoardMeta, Long> {

    Optional<BoardMeta> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);
    Page<BoardMeta> findAllByDataStateCodeNot(Pageable pageable, DataStateCode dataStateCode);
    boolean existsByTitleAndDataStateCodeNot(String name, DataStateCode dataStateCode);
}
