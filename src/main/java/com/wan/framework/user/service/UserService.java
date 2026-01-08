package com.wan.framework.user.service;

import com.wan.framework.user.constant.RoleType;
import com.wan.framework.user.domain.User;
import com.wan.framework.user.dto.UserDTO;
import com.wan.framework.user.mapper.UserMapper;
import com.wan.framework.user.repositroy.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.wan.framework.base.constant.DataStateCode.D;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserDTO saveUser(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        user = userRepository.save(user);
        return userMapper.toDto(user);
    }

    public UserDTO findById(String userId) {
        User user = userRepository.findByUserIdAndDataCodeNot(userId, D)
                .orElseThrow(EntityNotFoundException::new);
        return userMapper.toDto(user);
    }

    public Page<UserDTO> findAll(Pageable pageable) {
        Page<User> userPage =  userRepository.findAllByDataCodeNot(pageable, D);
        return userPage.map(it -> userMapper.toDto(it).removePass());
    }

    @Transactional
    public UserDTO modifyUser(UserDTO userDTO) {
        User user = userRepository.findByUserIdAndDataCodeNot(userDTO.getUserId(), D)
                .orElseThrow(EntityNotFoundException::new);
        String password = Optional.ofNullable(userDTO.getPassword())
                .orElse(user.getPassword());
        String name = Optional.ofNullable(userDTO.getName())
                .orElse(user.getName());
        String passwordSalt = Optional.ofNullable(userDTO.getPasswordSalt())
                .orElse(user.getPasswordSalt());

        // 불변성 유지를 위해 비즈니스 메서드 사용
        user.updatePassword(password, passwordSalt);
        user.updateName(name);

        return userMapper.toDto(user);
    }

    @Transactional
    public UserDTO deleteUser(UserDTO userDTO) {
        User user = userRepository.findByUserIdAndDataCodeNot(userDTO.getUserId(), D)
                .orElseThrow(EntityNotFoundException::new);

        // 불변성 유지를 위해 비즈니스 메서드 사용
        user.updateDataCode(D);

        return userMapper.toDto(user);
    }


    public boolean existsById(String userId) {
        return userRepository.existsById(userId);
    }

    /**
     * 관리자 계정 존재 여부 확인
     * - ROLE_ADMIN 권한을 가진 활성 사용자가 있는지 확인
     *
     * @return 관리자 계정 존재 여부
     */
    public boolean hasAdminAccount() {
        return userRepository.findAll().stream()
                .filter(user -> user.getDataCode() != D)  // 삭제되지 않은 사용자만
                .anyMatch(user -> user.getRoles() != null &&
                        user.getRoles().contains(RoleType.ROLE_ADMIN));
    }

}
