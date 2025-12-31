package com.wan.framework.board.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.domain.BoardFieldMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardFieldMetaRepository extends JpaRepository<BoardFieldMeta, Long> {

    Optional<BoardFieldMeta> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);

    List<BoardFieldMeta> findByBoardMetaIdAndDataStateCodeNotOrderByDisplayOrder(Long boardMetaId, DataStateCode dataStateCode);

    List<BoardFieldMeta> findByBoardMetaIdAndShowInListTrueAndDataStateCodeNotOrderByDisplayOrder(Long boardMetaId, DataStateCode dataStateCode);

    List<BoardFieldMeta> findByBoardMetaIdAndSearchableTrueAndDataStateCodeNot(Long boardMetaId, DataStateCode dataStateCode);

    boolean existsByBoardMetaIdAndFieldNameAndDataStateCodeNot(Long boardMetaId, String fieldName, DataStateCode dataStateCode);
}
