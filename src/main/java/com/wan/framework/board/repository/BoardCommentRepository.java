package com.wan.framework.board.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.board.domain.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {

    Optional<BoardComment> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);

    // 특정 게시글의 최상위 댓글만 조회 (부모 댓글이 없는 것)
    List<BoardComment> findByBoardDataIdAndParentIsNullAndDataStateCodeNotOrderByCreatedAtAsc(
        Long boardDataId, DataStateCode dataStateCode);

    // 특정 게시글의 모든 댓글 조회
    List<BoardComment> findByBoardDataIdAndDataStateCodeNotOrderByCreatedAtAsc(
        Long boardDataId, DataStateCode dataStateCode);

    // 특정 댓글의 대댓글 조회
    List<BoardComment> findByParentIdAndDataStateCodeNotOrderByCreatedAtAsc(
        Long parentId, DataStateCode dataStateCode);

    // 특정 게시글의 댓글 수 (삭제 제외)
    long countByBoardDataIdAndDataStateCodeNot(Long boardDataId, DataStateCode dataStateCode);

    // 특정 사용자의 댓글 수
    long countByAuthorIdAndDataStateCodeNot(String authorId, DataStateCode dataStateCode);

    // 특정 게시글의 모든 댓글을 부모-자식 관계로 가져오기 (fetch join)
    @Query("SELECT DISTINCT c FROM BoardComment c " +
           "LEFT JOIN FETCH c.children " +
           "WHERE c.boardData.id = :boardDataId " +
           "AND c.parent IS NULL " +
           "AND c.dataStateCode <> :deletedCode " +
           "ORDER BY c.createdAt ASC")
    List<BoardComment> findAllWithChildrenByBoardDataId(
        @Param("boardDataId") Long boardDataId,
        @Param("deletedCode") DataStateCode deletedCode);
}
