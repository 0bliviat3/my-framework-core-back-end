package com.wan.framework.menu.service;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.domain.MenuTree;
import com.wan.framework.menu.dto.MenuDTO;
import com.wan.framework.menu.dto.MenuTreeNodeDTO;
import com.wan.framework.menu.exception.MenuException;
import com.wan.framework.menu.mapper.MenuMapper;
import com.wan.framework.menu.repositoty.MenuRepository;
import com.wan.framework.program.domain.Program;
import com.wan.framework.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.base.constant.DataStateCode.U;
import static com.wan.framework.menu.constant.MenuExceptionMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MenuService {

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
                    .orElseThrow(() -> new MenuException(NOT_FOUND_PARENT));
            menu.setParent(parent);
        }

        // program 설정 (nullable)
        if (request.getProgramId() != null) {
            Program program = programRepository.findByIdAndDataStateCodeNot(request.getProgramId(), D)
                    .orElseThrow(() -> new MenuException(NOT_FOUND_PROGRAM));
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
                .orElseThrow(() -> new MenuException(NOT_FOUND_MENU));
        return menuMapper.toDTO(menu);
    }

    /**
     * 모든 메뉴 조회
     */
    @Transactional(readOnly = true)
    public List<MenuDTO> findAll() {
        return menuRepository.findAllByDataStateCodeNot(D)
                .stream()
                .map(menuMapper::toDTO).toList();
    }

    /**
     * 모든 메뉴 조회
     */
    @Transactional(readOnly = true)
    public MenuTreeNodeDTO findAllTree() {
        Map<Long, MenuTreeNodeDTO> menuTreeNodeDTOs = menuRepository.findAllByDataStateCodeNot(D).stream()
                .map(it -> new MenuTreeNodeDTO(menuMapper.toDTO(it), null))
                .collect(Collectors.toMap(menuTreeNodeDTO -> menuTreeNodeDTO.getMenuDTO().getId(), dto -> dto));
        return new MenuTree(menuTreeNodeDTOs).getMenus();
    }

    /**
     * 권한별 메뉴 조회
     */
    @Transactional(readOnly = true)
    public MenuTreeNodeDTO findAllTreeByRoles(String roles) {
        Map<Long, MenuTreeNodeDTO> menuTreeNodeDTOs = menuRepository.findAllByDataStateCodeNotAndRoles(D, roles).stream()
                .map(it -> new MenuTreeNodeDTO(menuMapper.toDTO(it), null))
                .collect(Collectors.toMap(menuTreeNodeDTO -> menuTreeNodeDTO.getMenuDTO().getId(), dto -> dto));
        return new MenuTree(menuTreeNodeDTOs).getMenus();
    }

    /**
     * 메뉴 수정
     */
    public MenuDTO updateMenu(Long id, MenuDTO request) {
        Menu menu = menuRepository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new MenuException(NOT_FOUND_MENU));

        menuMapper.updateEntityFromDto(request, menu);

        // parent 변경 (nullable)
        if (request.getParentId() != null) {
            Menu parent = menuRepository.findByIdAndDataStateCodeNot(request.getParentId(), D)
                    .orElseThrow(() -> new MenuException(NOT_FOUND_PARENT));
            menu.setParent(parent);
        } else {
            menu.setParent(null);
        }

        // program 변경 (nullable)
        if (request.getProgramId() != null) {
            Program program = programRepository.findByIdAndDataStateCodeNot(request.getProgramId(), D)
                    .orElseThrow(() -> new MenuException(NOT_FOUND_PROGRAM));
            menu.setProgram(program);
        } else {
            menu.setProgram(null);
        }

        menu.setDataStateCode(U);

        return menuMapper.toDTO(menu);
    }

    /**
     * 메뉴 삭제
     */
    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = menuRepository.findByIdAndDataStateCodeNot(id, D)
                .orElseThrow(() -> new MenuException(NOT_FOUND_MENU));
        menu.setDataStateCode(D);
    }
}

