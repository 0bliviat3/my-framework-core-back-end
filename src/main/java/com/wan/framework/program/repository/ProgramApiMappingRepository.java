package com.wan.framework.program.repository;

import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.program.domain.Program;
import com.wan.framework.program.domain.ProgramApiMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Program-API Mapping Repository
 */
public interface ProgramApiMappingRepository extends JpaRepository<ProgramApiMapping, Long> {

    /**
     * Program별 API 목록 조회
     */
    @Query("SELECT m FROM ProgramApiMapping m JOIN FETCH m.apiRegistry WHERE m.program.id = :programId")
    List<ProgramApiMapping> findByProgramIdWithApi(@Param("programId") Long programId);

    /**
     * Program별 필수 API 목록 조회
     */
    @Query("SELECT m FROM ProgramApiMapping m JOIN FETCH m.apiRegistry " +
           "WHERE m.program.id = :programId AND m.required = true")
    List<ProgramApiMapping> findRequiredApisByProgramId(@Param("programId") Long programId);

    /**
     * Program과 API로 매핑 조회
     */
    Optional<ProgramApiMapping> findByProgramAndApiRegistry(Program program, ApiRegistry apiRegistry);

    /**
     * Program에 속한 모든 매핑 삭제
     */
    @Modifying
    @Query("DELETE FROM ProgramApiMapping m WHERE m.program = :program")
    void deleteByProgram(@Param("program") Program program);

    /**
     * API에 속한 모든 매핑 삭제
     */
    @Modifying
    @Query("DELETE FROM ProgramApiMapping m WHERE m.apiRegistry = :apiRegistry")
    void deleteByApiRegistry(@Param("apiRegistry") ApiRegistry apiRegistry);

    /**
     * 매핑 존재 여부 확인
     */
    boolean existsByProgramAndApiRegistry(Program program, ApiRegistry apiRegistry);
}
