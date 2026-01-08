package com.wan.framework.board.service;

import com.wan.framework.user.constant.RoleType;
import com.wan.framework.user.domain.User;
import com.wan.framework.user.repositroy.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.wan.framework.base.constant.DataStateCode.D;

/**
 * 게시판 권한 검증 서비스
 * - 작성자 확인
 * - 관리자 권한 확인
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BoardPermissionService {

    private final UserRepository userRepository;

    /**
     * 사용자가 관리자인지 확인
     */
    @Transactional(readOnly = true)
    public boolean isAdmin(String userId) {
        return userRepository.findByUserIdAndDataCodeNot(userId, D)
                .map(user -> user.getRoles().contains(RoleType.ROLE_ADMIN))
                .orElse(false);
    }

    /**
     * 사용자가 매니저인지 확인
     */
    @Transactional(readOnly = true)
    public boolean isManager(String userId) {
        return userRepository.findByUserIdAndDataCodeNot(userId, D)
                .map(user -> user.getRoles().contains(RoleType.ROLE_MANAGER))
                .orElse(false);
    }

    /**
     * 사용자가 관리 권한(ADMIN 또는 MANAGER)을 가지고 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean hasManagementRole(String userId) {
        return userRepository.findByUserIdAndDataCodeNot(userId, D)
                .map(user -> user.getRoles().contains(RoleType.ROLE_ADMIN) ||
                             user.getRoles().contains(RoleType.ROLE_MANAGER))
                .orElse(false);
    }

    /**
     * 작성자 본인이거나 관리자인지 확인
     */
    @Transactional(readOnly = true)
    public boolean canModify(String authorId, String userId) {
        // 작성자 본인이면 허용
        if (authorId.equals(userId)) {
            return true;
        }
        // 관리자 또는 매니저이면 허용
        return hasManagementRole(userId);
    }

    /**
     * 작성자 본인이거나 관리자인지 확인 (삭제용)
     */
    @Transactional(readOnly = true)
    public boolean canDelete(String authorId, String userId) {
        // 작성자 본인이면 허용
        if (authorId.equals(userId)) {
            return true;
        }
        // 관리자 또는 매니저이면 허용
        return hasManagementRole(userId);
    }
}
