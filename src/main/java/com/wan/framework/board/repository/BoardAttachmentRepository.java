package com.wan.framework.board.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.domain.BoardAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardAttachmentRepository extends JpaRepository<BoardAttachment, Long> {

    Optional<BoardAttachment> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);

    List<BoardAttachment> findByBoardDataIdAndDataStateCodeNotOrderByDisplayOrder(
        Long boardDataId, DataStateCode dataStateCode);

    long countByBoardDataIdAndDataStateCodeNot(Long boardDataId, DataStateCode dataStateCode);

    long countByUploadedByAndDataStateCodeNot(String uploadedBy, DataStateCode dataStateCode);

    // 특정 게시글의 첨부파일 총 크기
    @Query("SELECT COALESCE(SUM(ba.fileSize), 0) FROM BoardAttachment ba " +
           "WHERE ba.boardData.id = :boardDataId AND ba.dataStateCode <> :dataStateCode")
    Long sumFileSizeByBoardDataIdAndDataStateCodeNot(@Param("boardDataId") Long boardDataId,
                                                       @Param("dataStateCode") DataStateCode dataStateCode);
}
