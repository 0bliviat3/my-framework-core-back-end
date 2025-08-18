package com.wan.framework.program.web;

import com.wan.framework.program.dto.ProgramDTO;
import com.wan.framework.program.service.ProgramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramService programService;

    @PostMapping
    public ResponseEntity<String> createProgram(@RequestBody ProgramDTO programDTO) {
        programService.saveProgram(programDTO);
        return ResponseEntity.ok("프로그램 생성완료");
    }

    @PutMapping
    public ProgramDTO modifyProgram(@RequestBody ProgramDTO programDTO) {
        return programService.modifyProgram(programDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProgram(@PathVariable Long id) {
        ProgramDTO deletedProgramDTO = programService.deleteProgram(id);
        return ResponseEntity.ok("프로그램 삭제 성공");
    }

    @GetMapping
    public Page<ProgramDTO> getProgramList(
            @RequestParam(value = "page", defaultValue = "0") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);
        return programService.findAll(pageRequest);
    }

    @GetMapping("/{id}")
    public ProgramDTO getProgramByName(@PathVariable Long programId) {
        return programService.findById(programId);
    }

}
