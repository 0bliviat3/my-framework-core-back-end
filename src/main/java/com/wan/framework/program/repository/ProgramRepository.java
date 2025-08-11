package com.wan.framework.program.repository;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.program.domain.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, String> {

    Optional<Program> findByProgramIdAndDataCodeNot(Long programId, DataStateCode dataStateCode);
    Page<Program> findAllByDataCodeNot(Pageable pageable, DataStateCode dataStateCode);

    boolean existsByProgramNameAndDataCodeNot(String programName, DataStateCode dataCode);

    boolean existsByProgramNameAndIdNotAndDataCodeNot(String programName, Long id, DataStateCode dataCode);
}
