package com.wan.framework.user.service;

import com.wan.framework.user.constant.RoleType;
import com.wan.framework.user.dto.UserDTO;
import com.wan.framework.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.wan.framework.user.constant.UserExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {

    private final UserService userService;

    private final PasswordService passwordService;

    // ---------- User existence helper ----------

    /**
     * 사용자 존재 여부 확인.
     */
    public boolean isExistUserId(String userId) {
        return userService.existsById(userId);
    }

    // ---------- Core flows ----------

    /**
     * 회원가입
     * - 일반 사용자 생성
     * - ROLE_USER 권한 자동 부여
     *
     * @param userDTO 사용자 정보 (userId, password, name)
     * @throws UserException ID가 중복된 경우
     */
    @Transactional
    public void signUp(UserDTO userDTO) {
        // 1. 사용자 ID 중복 확인
        if (isExistUserId(userDTO.getUserId())) {
            throw new UserException(USED_ID);
        }

        // 2. 비밀번호 암호화
        String saltBase64 = passwordService.generateSaltBase64();
        String hashed = passwordService.hashPassword(userDTO.getPassword(), saltBase64);

        // 3. ROLE_USER 권한 부여
        UserDTO toSave = UserDTO.builder()
                .userId(userDTO.getUserId())
                .password(hashed)
                .name(userDTO.getName())
                .passwordSalt(saltBase64)
                .roles(Set.of(RoleType.ROLE_USER))  // 일반 사용자 권한
                .build();

        userService.saveUser(toSave);
        log.info("일반 사용자 회원가입 완료: {}", userDTO.getUserId());
    }

    /**
     * 로그인 검증 후 사용자 정보 반환 (비밀번호는 제거)
     */
    public UserDTO signIn(UserDTO userDTO) {
        validateSignIn(userDTO);
        return userService.findById(userDTO.getUserId()).removePass();
    }

    /**
     * 로그인 검증용: id 존재 & password 일치 여부 확인
     */
    private void validateSignIn(UserDTO userDTO) {
        validateId(userDTO.getUserId());
        validatePass(userDTO);
    }

    private void validateId(String userId) {
        if (!isExistUserId(userId)) {
            throw new UserException(INVALID_ID);
        }
    }

    private void validatePass(UserDTO userDTO) {
        if (!verifyUser(userDTO)) {
            throw new UserException(INVALID_PASSWORD);
        }
    }

    /**
     * 특정 사용자 비밀번호 검증 (입력값과 DB 비교)
     */
    public boolean verifyUser(UserDTO inputDTO) {
        UserDTO stored = userService.findById(inputDTO.getUserId());
        String saltBase64 = stored.getPasswordSalt();
        String storedHash = stored.getPassword();
        return passwordService.verifyPassword(inputDTO.getPassword(), saltBase64, storedHash);
    }

    /**
     * 수정: 보안 정책에 따라 비밀번호 재입력(로그인 검증)을 요구. 이후 새 솔트 생성 및 저장.
     */
    public UserDTO modifyUser(UserDTO userDTO) {
        // 보안: 수정 전 본인 확인 (트랜잭션 밖에서 검증)
        validateSignIn(userDTO);

        // 실제 수정 로직은 별도 트랜잭션으로 처리
        return modifyUserInternal(userDTO);
    }

    /**
     * 사용자 정보 수정 내부 로직 (트랜잭션 처리)
     */
    @Transactional
    private UserDTO modifyUserInternal(UserDTO userDTO) {
        String newSalt = passwordService.generateSaltBase64();
        String newHash = passwordService.hashPassword(userDTO.getPassword(), newSalt);

        userDTO.setPassword(newHash);
        userDTO.setPasswordSalt(newSalt);

        return userService.modifyUser(userDTO).removePass();
    }

    /**
     * 삭제: 본인 확인 후 삭제 처리
     */
    public UserDTO deleteUser(UserDTO userDTO) {
        // 본인 확인 (트랜잭션 밖에서 검증)
        validateSignIn(userDTO);

        // 실제 삭제 로직은 별도 트랜잭션으로 처리
        return deleteUserInternal(userDTO);
    }

    /**
     * 사용자 삭제 내부 로직 (트랜잭션 처리)
     */
    @Transactional
    private UserDTO deleteUserInternal(UserDTO userDTO) {
        return userService.deleteUser(userDTO).removePass();
    }

    // ---------- 조회 ----------

    public Page<UserDTO> findAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

    public UserDTO findById(String userId) {
        return userService.findById(userId).removePass();
    }

    /**
     * 초기 관리자 계정 생성
     * - 관리자 계정이 없을 때만 생성 가능
     * - ROLE_ADMIN 권한 자동 부여
     *
     * @param userDTO 관리자 정보 (userId, password, name)
     * @throws UserException 이미 관리자 계정이 존재하거나 ID가 중복된 경우
     */
    @Transactional
    public void createInitialAdmin(UserDTO userDTO) {
        // 1. 관리자 계정이 이미 존재하는지 확인
        if (userService.hasAdminAccount()) {
            throw new UserException(USED_ID); // 또는 별도 예외 메시지 사용 가능
        }

        // 2. 사용자 ID 중복 확인
        if (isExistUserId(userDTO.getUserId())) {
            throw new UserException(USED_ID);
        }

        // 3. 비밀번호 암호화
        String saltBase64 = passwordService.generateSaltBase64();
        String hashed = passwordService.hashPassword(userDTO.getPassword(), saltBase64);

        // 4. ROLE_ADMIN 권한 부여
        UserDTO toSave = UserDTO.builder()
                .userId(userDTO.getUserId())
                .password(hashed)
                .name(userDTO.getName())
                .passwordSalt(saltBase64)
                .roles(Set.of(RoleType.ROLE_ADMIN, RoleType.ROLE_USER))  // 관리자 + 일반 사용자 권한
                .build();

        userService.saveUser(toSave);
        log.info("초기 관리자 계정 생성 완료: {}", userDTO.getUserId());
    }

}