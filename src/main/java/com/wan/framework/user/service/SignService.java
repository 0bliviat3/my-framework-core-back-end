package com.wan.framework.user.service;

import com.wan.framework.user.dto.UserDTO;
import com.wan.framework.user.exception.UserException;
import com.wan.framework.user.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import static com.wan.framework.user.constant.UserExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {

    @Value("${password.salt.size}")
    private Integer saltSize;
    @Value("${password.iteration}")
    private String passwordIteration;

    private final UserService userService;


    private void validateHash(UserDTO userDTO) {
        String password = userDTO.getPassword();
        String salt = userDTO.getPasswordSalt();
        if (password == null || salt == null) {
            throw new UserException(FAIL_HASH_PASSWORD);
        }
    }

    private String hashPassword(UserDTO userDTO) {
        validateHash(userDTO);
        String password = userDTO.getPassword();
        int iteration = Integer.parseInt(passwordIteration);
        String salt = userDTO.getPasswordSalt();
        return PasswordUtil.hashPassword(password, salt, iteration);
    }

    private String generateSalt() {
        try {
            return Base64.getEncoder()
                    .encodeToString(SecureRandom.getInstanceStrong().generateSeed(saltSize));
        } catch (NoSuchAlgorithmException e) {
            throw new UserException(FAIL_CREATE_SALT, e);
        }

    }

    public boolean isExistUserId(String userId) {
        try {
            userService.findById(userId);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean verifyUser(UserDTO inputDTO) {
        UserDTO storedDTO = userService.findById(inputDTO.getUserId());
        String rawPassword = inputDTO.getPassword();
        String salt = storedDTO.getPasswordSalt();
        int iteration = Integer.parseInt(passwordIteration);
        String storedHashedPassword = storedDTO.getPassword();

        return PasswordUtil.verifyPassword(rawPassword, salt, iteration, storedHashedPassword);
    }


    private void saveUser(UserDTO userDTO) {
        String salt = generateSalt();
        String hashWord = hashPassword(
                UserDTO.builder()
                        .password(userDTO.getPassword())
                        .passwordSalt(salt)
                        .build()
        );
        userService.saveUser(
                UserDTO.builder()
                        .userId(userDTO.getUserId())
                        .password(hashWord)
                        .name(userDTO.getName())
                        .passwordSalt(salt)
                        .build()
        );
    }

    public void signUp(UserDTO userDTO) {
        if (isExistUserId(userDTO.getUserId())) {
            throw new UserException(USED_ID);
        }
        saveUser(userDTO);
    }

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

    public UserDTO signIn(UserDTO userDTO) {
        validateSignIn(userDTO);
        return userService.findById(userDTO.getUserId()).removePass();
    }

    /*
    [로그인 보안 정책]
    수정, 삭제시엔 비밀번호를 다시 입력받도록 한다.
    또한 삭제시 현재 세션인 경우 세션에서 제거할수 있도록 dto를 리턴해준다.
     */
    public UserDTO modifyUser(UserDTO userDTO) {
        isExistUserId(userDTO.getUserId());
        String salt = generateSalt();
        String hashWord = hashPassword(
                UserDTO.builder()
                        .password(userDTO.getPassword())
                        .passwordSalt(salt)
                        .build()
        );
        userDTO.setPassword(hashWord);
        userDTO.setPasswordSalt(salt);
        return userService
                .modifyUser(userDTO)
                .removePass();
    }

    public UserDTO deleteUser(UserDTO userDTO) {
        validateSignIn(userDTO);
        return userService
                .deleteUser(userDTO)
                .removePass();
    }

    public Page<UserDTO> findAll(Pageable pageable) {
        return userService.findAll(pageable);
    }

}
