package com.wan.framework.program.service;

import com.wan.framework.program.domain.Program;
import com.wan.framework.program.dto.ProgramDTO;
import com.wan.framework.program.mapper.ProgramMapper;
import com.wan.framework.program.repository.ProgramRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.base.constant.DataStateCode.U;
import static com.wan.framework.program.constant.ProgramExceptionMessage.DUPLICATED_PROGRAM_NAME;

@Service
@RequiredArgsConstructor
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ProgramMapper programMapper;

    @Transactional
    public ProgramDTO saveProgram(ProgramDTO programDTO) {
        if (programRepository.existsByProgramNameAndDataCodeNot(programDTO.getName(), D)) {
            throw new DuplicateKeyException(DUPLICATED_PROGRAM_NAME.getMessage());
        }
        Program program = programMapper.toEntity(programDTO);
        program = programRepository.save(program);
        return programMapper.toDTO(program);
    }

    public ProgramDTO findById(Long programId) {
        Program program = programRepository.findByProgramIdAndDataCodeNot(programId, D)
                .orElseThrow(EntityExistsException::new);
        return programMapper.toDTO(program);
    }

    public Page<ProgramDTO> findAll(Pageable pageable) {
        Page<Program> programPage = programRepository.findAllByDataCodeNot(pageable, D);
        return programPage.map(programMapper::toDTO);
    }

    @Transactional
    public ProgramDTO modifyProgram(ProgramDTO programDTO) {
        if (programRepository.existsByProgramNameAndIdNotAndDataCodeNot(programDTO.getName(), programDTO.getId(), D)) {
            throw new DuplicateKeyException(DUPLICATED_PROGRAM_NAME.getMessage());
        }
        Program program = programRepository.findByProgramIdAndDataCodeNot(programDTO.getId(), D)
                .orElseThrow(EntityNotFoundException::new);
        programMapper.updateEntityFromDto(programDTO, program);
        program.setDataStateCode(U);
        return programMapper.toDTO(program);
    }

    @Transactional
    public ProgramDTO deleteProgram(Long programId) {
        Program program = programRepository.findByProgramIdAndDataCodeNot(programId, D)
                .orElseThrow(EntityNotFoundException::new);
        program.setDataStateCode(D);
        return programMapper.toDTO(program);
    }


    public boolean existsById(String programId) {
        return programRepository.existsById(programId);
    }

    public boolean existsByName(String programName) {
        return programRepository.existsByProgramNameAndDataCodeNot(programName, D);
    }

}
