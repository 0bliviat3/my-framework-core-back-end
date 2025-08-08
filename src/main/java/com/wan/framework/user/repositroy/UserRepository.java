package com.wan.framework.user.repositroy;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUserIdAndDataCodeNot(String userId, DataStateCode dataStateCode);
    Page<User> findAllByDataCodeNot(Pageable pageable, DataStateCode dataStateCode);
}
