package com.wan.framework.user.service;

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

        user.setPassword(password);
        user.setPasswordSalt(passwordSalt);
        user.setName(name);
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDTO deleteUser(UserDTO userDTO) {
        User user = userRepository.findByUserIdAndDataCodeNot(userDTO.getUserId(), D)
                .orElseThrow(EntityNotFoundException::new);
        user.setModifiedTime(LocalDateTime.now());
        user.setDataCode(D);
        return userMapper.toDto(user);
    }


    public boolean existsById(String userId) {
        return userRepository.existsById(userId);
    }

}
