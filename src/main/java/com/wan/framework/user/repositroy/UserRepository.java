package com.wan.framework.user.repositroy;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserIdAndDataCodeNot(String userId, DataStateCode dataStateCode);
    Page<User> findAllByDataCodeNot(Pageable pageable, DataStateCode dataStateCode);

    /**
     * 특정 Role을 가진 활성 사용자 존재 여부 확인
     * - User-Role ManyToMany 매핑 테이블 조인
     * - 삭제되지 않은 사용자만 확인
     *
     * @param roleCode 확인할 Role 코드 (예: ROLE_ADMIN)
     * @param deletedCode 삭제 상태 코드 (D)
     * @return 해당 Role을 가진 사용자가 존재하면 true
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
           "FROM User u JOIN u.roleEntities r " +
           "WHERE r.roleCode = :roleCode AND u.dataCode <> :deletedCode")
    boolean existsByRoleCodeAndNotDeleted(@Param("roleCode") String roleCode,
                                          @Param("deletedCode") DataStateCode deletedCode);

    /**
     * 특정 Role을 가진 활성 사용자 목록 조회
     *
     * @param roleCode 조회할 Role 코드
     * @param deletedCode 삭제 상태 코드 (D)
     * @return 해당 Role을 가진 사용자 목록
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roleEntities r " +
           "WHERE r.roleCode = :roleCode AND u.dataCode <> :deletedCode")
    List<User> findByRoleCodeAndNotDeleted(@Param("roleCode") String roleCode,
                                           @Param("deletedCode") DataStateCode deletedCode);
}
