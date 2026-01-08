package com.wan.framework.board.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.constant.BoardDataStatus;
import com.wan.framework.board.domain.BoardData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardDataRepository extends JpaRepository<BoardData, Long> {

    @EntityGraph(attributePaths = {"boardMeta"})
    Optional<BoardData> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);

    @EntityGraph(attributePaths = {"boardMeta"})
    Page<BoardData> findByBoardMetaIdAndDataStateCodeNotOrderByStatusDescCreatedAtDesc(
        Long boardMetaId, DataStateCode dataStateCode, Pageable pageable);

    @EntityGraph(attributePaths = {"boardMeta"})
    Page<BoardData> findByBoardMetaIdAndStatusAndDataStateCodeNotOrderByCreatedAtDesc(
        Long boardMetaId, BoardDataStatus status, DataStateCode dataStateCode, Pageable pageable);

    @EntityGraph(attributePaths = {"boardMeta"})
    Page<BoardData> findByBoardMetaIdAndStatusInAndDataStateCodeNotOrderByStatusDescCreatedAtDesc(
        Long boardMetaId, List<BoardDataStatus> statuses, DataStateCode dataStateCode, Pageable pageable);

    // 제목 검색
    @EntityGraph(attributePaths = {"boardMeta"})
    Page<BoardData> findByBoardMetaIdAndTitleContainingAndStatusInAndDataStateCodeNotOrderByStatusDescCreatedAtDesc(
        Long boardMetaId, String title, List<BoardDataStatus> statuses, DataStateCode dataStateCode, Pageable pageable);

    // 작성자 검색
    @EntityGraph(attributePaths = {"boardMeta"})
    Page<BoardData> findByBoardMetaIdAndAuthorIdAndStatusInAndDataStateCodeNotOrderByCreatedAtDesc(
        Long boardMetaId, String authorId, List<BoardDataStatus> statuses, DataStateCode dataStateCode, Pageable pageable);

    // 이전글 조회
    @EntityGraph(attributePaths = {"boardMeta"})
    @Query("SELECT bd FROM BoardData bd WHERE bd.boardMeta.id = :boardMetaId " +
           "AND bd.id < :currentId AND bd.status IN :statuses AND bd.dataStateCode <> :deletedCode " +
           "ORDER BY bd.id DESC")
    Optional<BoardData> findPrevious(@Param("boardMetaId") Long boardMetaId,
                                     @Param("currentId") Long currentId,
                                     @Param("statuses") List<BoardDataStatus> statuses,
                                     @Param("deletedCode") DataStateCode deletedCode);

    // 다음글 조회
    @EntityGraph(attributePaths = {"boardMeta"})
    @Query("SELECT bd FROM BoardData bd WHERE bd.boardMeta.id = :boardMetaId " +
           "AND bd.id > :currentId AND bd.status IN :statuses AND bd.dataStateCode <> :deletedCode " +
           "ORDER BY bd.id ASC")
    Optional<BoardData> findNext(@Param("boardMetaId") Long boardMetaId,
                                 @Param("currentId") Long currentId,
                                 @Param("statuses") List<BoardDataStatus> statuses,
                                 @Param("deletedCode") DataStateCode deletedCode);

    // 특정 사용자의 게시글 수
    long countByAuthorIdAndDataStateCodeNot(String authorId, DataStateCode dataStateCode);
}
