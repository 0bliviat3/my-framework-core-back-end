package com.wan.framework.user.service;

import com.wan.framework.user.dto.UserDTO;
import com.wan.framework.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * userService.existsById가 있으면 사용하고, 없으면 findById 예외 처리 방식으로 폴백.
     */
    public boolean isExistUserId(String userId) {
        return userService.existsById(userId);
    }

    // ---------- Core flows ----------

    /**
     * 회원가입
     */
    @Transactional
    public void signUp(UserDTO userDTO) {
        if (isExistUserId(userDTO.getUserId())) {
            throw new UserException(USED_ID);
        }

        String saltBase64 = passwordService.generateSaltBase64();
        String hashed = passwordService.hashPassword(userDTO.getPassword(), saltBase64);

        UserDTO toSave = UserDTO.builder()
                .userId(userDTO.getUserId())
                .password(hashed)
                .name(userDTO.getName())
                .passwordSalt(saltBase64)
                .build();

        userService.saveUser(toSave);
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
    @Transactional
    public UserDTO modifyUser(UserDTO userDTO) {
        // 보안: 수정 전 본인 확인
        validateSignIn(userDTO);

        String newSalt = passwordService.generateSaltBase64();
        String newHash = passwordService.hashPassword(userDTO.getPassword(), newSalt);

        userDTO.setPassword(newHash);
        userDTO.setPasswordSalt(newSalt);

        return userService.modifyUser(userDTO).removePass();
    }

    /**
     * 삭제: 본인 확인 후 삭제 처리
     */
    @Transactional
    public UserDTO deleteUser(UserDTO userDTO) {
        validateSignIn(userDTO);
        return userService.deleteUser(userDTO).removePass();
    }

    // ---------- 조회 ----------

    public Page<UserDTO> findAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

}