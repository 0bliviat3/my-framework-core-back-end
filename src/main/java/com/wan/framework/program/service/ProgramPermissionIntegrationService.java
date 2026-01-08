package com.wan.framework.program.service;

import com.wan.framework.permission.constant.ApiStatus;
import com.wan.framework.permission.domain.ApiRegistry;
import com.wan.framework.permission.repository.ApiRegistryRepository;
import com.wan.framework.program.domain.Program;
import com.wan.framework.program.domain.ProgramApiMapping;
import com.wan.framework.program.dto.ProgramApiMappingDTO;
import com.wan.framework.program.exception.ProgramException;
import com.wan.framework.program.repository.ProgramApiMappingRepository;
import com.wan.framework.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.permission.constant.PermissionExceptionMessage.API_NOT_FOUND;
import static com.wan.framework.program.constant.ProgramExceptionMessage.NOT_FOUND_PROGRAM;

/**
 * Program-Permission 통합 서비스
 * - Program과 ApiRegistry 매핑 관리
 * - Menu → Program → API 연계
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramPermissionIntegrationService {

    private final ProgramRepository programRepository;
    private final ApiRegistryRepository apiRegistryRepository;
    private final ProgramApiMappingRepository programApiMappingRepository;

    /**
     * Program에 API 매핑 추가
     */
    @Transactional
    public void addApiToProgram(Long programId, Long apiId, Boolean required, String description) {
        Program program = programRepository.findByIdAndDataStateCodeNot(programId, D)
                .orElseThrow(() -> new ProgramException(NOT_FOUND_PROGRAM));

        ApiRegistry api = apiRegistryRepository.findById(apiId)
                .orElseThrow(() -> new com.wan.framework.permission.exception.PermissionException(API_NOT_FOUND));

        // 기존 매핑 확인
        if (programApiMappingRepository.existsByProgramAndApiRegistry(program, api)) {
            log.warn("Mapping already exists: program={}, api={}", programId, apiId);
            return;
        }

        ProgramApiMapping mapping = ProgramApiMapping.builder()
                .program(program)
                .apiRegistry(api)
                .required(required != null ? required : true)
                .description(description)
                .build();

        programApiMappingRepository.save(mapping);

        log.info("Added API to Program: program={}, api={} {}",
                program.getName(), api.getHttpMethod(), api.getUriPattern());
    }

    /**
     * Program에서 API 매핑 제거
     */
    @Transactional
    public void removeApiFromProgram(Long programId, Long apiId) {
        Program program = programRepository.findByIdAndDataStateCodeNot(programId, D)
                .orElseThrow(() -> new ProgramException(NOT_FOUND_PROGRAM));

        ApiRegistry api = apiRegistryRepository.findById(apiId)
                .orElseThrow(() -> new com.wan.framework.permission.exception.PermissionException(API_NOT_FOUND));

        programApiMappingRepository.findByProgramAndApiRegistry(program, api)
                .ifPresent(programApiMappingRepository::delete);

        log.info("Removed API from Program: program={}, api={} {}",
                program.getName(), api.getHttpMethod(), api.getUriPattern());
    }

    /**
     * Program별 API 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ProgramApiMappingDTO> getProgramApis(Long programId) {
        List<ProgramApiMapping> mappings = programApiMappingRepository.findByProgramIdWithApi(programId);

        return mappings.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Program별 필수 API 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ApiRegistry> getRequiredApis(Long programId) {
        List<ProgramApiMapping> mappings = programApiMappingRepository.findRequiredApisByProgramId(programId);

        return mappings.stream()
                .map(ProgramApiMapping::getApiRegistry)
                .collect(Collectors.toList());
    }

    /**
     * Menu에 필요한 모든 API 계산 (Menu → Program → APIs)
     *
     * @param programId 메뉴에 연결된 프로그램 ID
     * @return 해당 화면을 보기 위해 필요한 API 목록
     */
    @Transactional(readOnly = true)
    public Set<String> calculateRequiredApisForMenu(Long programId) {
        if (programId == null) {
            return Set.of();
        }

        List<ProgramApiMapping> mappings = programApiMappingRepository.findRequiredApisByProgramId(programId);

        return mappings.stream()
                .map(ProgramApiMapping::getApiRegistry)
                .filter(api -> api.getStatus() == ApiStatus.ACTIVE)  // 활성 API만
                .map(ApiRegistry::getApiIdentifier)  // service_id::method::uri
                .collect(Collectors.toSet());
    }

    /**
     * 활성 API 목록 조회 (Program에 매핑하기 위한)
     */
    @Transactional(readOnly = true)
    public List<ApiRegistry> getActiveApis() {
        return apiRegistryRepository.findByStatus(ApiStatus.ACTIVE);
    }

    /**
     * Program 삭제 시 매핑도 함께 삭제
     */
    @Transactional
    public void deleteAllMappingsByProgram(Long programId) {
        Program program = programRepository.findByIdAndDataStateCodeNot(programId, D)
                .orElseThrow(() -> new ProgramException(NOT_FOUND_PROGRAM));

        programApiMappingRepository.deleteByProgram(program);

        log.info("Deleted all API mappings for Program: {}", program.getName());
    }

    // Helper Methods
    private ProgramApiMappingDTO toDTO(ProgramApiMapping mapping) {
        return ProgramApiMappingDTO.builder()
                .mappingId(mapping.getMappingId())
                .programId(mapping.getProgram().getId())
                .programName(mapping.getProgram().getName())
                .apiId(mapping.getApiRegistry().getApiId())
                .httpMethod(mapping.getApiRegistry().getHttpMethod())
                .uriPattern(mapping.getApiRegistry().getUriPattern())
                .required(mapping.getRequired())
                .description(mapping.getDescription())
                .createdAt(mapping.getCreatedAt())
                .build();
    }
}
