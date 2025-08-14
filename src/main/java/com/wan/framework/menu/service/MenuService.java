package com.wan.framework.menu.service;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.domain.MenuTree;
import com.wan.framework.menu.dto.MenuDTO;
import com.wan.framework.menu.dto.MenuTreeNodeDTO;
import com.wan.framework.menu.mapper.MenuMapper;
import com.wan.framework.menu.repositoty.MenuRepository;
import com.wan.framework.program.domain.Program;
import com.wan.framework.program.repository.ProgramRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

    //TODO: runtime exception -> 예외 정의해서 예외 공통 처리 할것
    private final MenuRepository menuRepository;
    private final ProgramRepository programRepository; // Program 매핑용
    private final MenuMapper menuMapper;

    /**
     * 메뉴 생성
     */
    public MenuDTO createMenu(MenuDTO request) {
        Menu menu = menuMapper.toEntity(request);

        // parent 설정 (nullable)
        if (request.getParentId() != null) {
            Menu parent = menuRepository.findByIdAndDataStateCodeNot(request.getParentId(), D)
                    .orElseThrow(() -> new RuntimeException("Parent menu not found"));
            menu.setParent(parent);
        }

        // program 설정 (nullable)
        if (request.getProgramId() != null) {
            Program program = programRepository.findByIdAndDataStateCodeNot(request.getProgramId(), D)
                    .orElseThrow(() -> new RuntimeException("Program not found"));
            menu.setProgram(program);
        }

        Menu saved = menuRepository.save(menu);
        return menuMapper.toDTO(saved);
    }

    /**
     * 메뉴 단건 조회
     */
    @Transactional(readOnly = true)
    public MenuDTO getMenu(Long id) {
        Menu menu = menuRepository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new RuntimeException("Menu not found"));
        return menuMapper.toDTO(menu);
    }

    /**
     * 모든 메뉴 조회
     */
    @Transactional(readOnly = true)
    public MenuTreeNodeDTO findAll() {
        Map<Long, MenuTreeNodeDTO> menuTreeNodeDTOs = menuRepository.findAllByDataStateCodeNot(D).stream()
                .map(it -> new MenuTreeNodeDTO(menuMapper.toDTO(it), null))
                .collect(Collectors.toMap(menuTreeNodeDTO -> menuTreeNodeDTO.getMenuDTO().getId(), dto -> dto));
        return new MenuTree(menuTreeNodeDTOs).getMenus();
    }

    /**
     * 메뉴 수정
     */
    public MenuDTO updateMenu(Long id, MenuDTO request) {
        Menu menu = menuRepository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        menuMapper.updateEntityFromDto(request, menu);

        // parent 변경 (nullable)
        if (request.getParentId() != null) {
            Menu parent = menuRepository.findByIdAndDataStateCodeNot(request.getParentId(), D)
                    .orElseThrow(() -> new RuntimeException("Parent menu not found"));
            menu.setParent(parent);
        } else {
            menu.setParent(null);
        }

        // program 변경 (nullable)
        if (request.getProgramId() != null) {
            Program program = programRepository.findByIdAndDataStateCodeNot(request.getProgramId(), D)
                    .orElseThrow(() -> new RuntimeException("Program not found"));
            menu.setProgram(program);
        } else {
            menu.setProgram(null);
        }

        return menuMapper.toDTO(menu);
    }

    /**
     * 메뉴 삭제
     */
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(EntityNotFoundException::new);
        menu.setDataStateCode(D);
    }
}

