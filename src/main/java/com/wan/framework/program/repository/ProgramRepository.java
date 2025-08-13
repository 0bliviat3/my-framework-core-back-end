package com.wan.framework.program.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.program.domain.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, String> {

    Optional<Program> findByIdAndDataStateCodeNot(Long id, DataStateCode dataStateCode);
    Page<Program> findAllByDataStateCodeNot(Pageable pageable, DataStateCode dataStateCode);

    boolean existsByNameAndDataStateCodeNot(String name, DataStateCode dataStateCode);

    boolean existsByNameAndIdNotAndDataStateCodeNot(String name, Long id, DataStateCode dataStateCode);
}
