package com.wan.framework.menu.service;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.domain.MenuTree;
import com.wan.framework.menu.dto.MenuDTO;
import com.wan.framework.menu.dto.MenuTreeNodeDTO;
import com.wan.framework.menu.exception.MenuException;
import com.wan.framework.menu.mapper.MenuMapper;
import com.wan.framework.menu.repositoty.MenuRepository;
import com.wan.framework.permission.domain.Role;
import com.wan.framework.permission.repository.RoleRepository;
import com.wan.framework.program.domain.Program;
import com.wan.framework.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final RoleRepository roleRepository; // Role 매핑용
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

        // roles 설정 (Role Entity 기반)
        if (request.getRoleCodes() != null && !request.getRoleCodes().isEmpty()) {
            Set<Role> roleEntities = new HashSet<>();
            for (String roleCode : request.getRoleCodes()) {
                Role role = roleRepository.findByRoleCode(roleCode)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));
                roleEntities.add(role);
            }
            menu.setRoles(roleEntities);
            log.info("Menu roles set: roleCodes={}, roleEntities count={}", request.getRoleCodes(), roleEntities.size());
        } else {
            log.warn("No roleCodes provided for menu creation");
        }

        Menu saved = menuRepository.save(menu);
        log.info("Menu saved: menuId={}, roles count={}", saved.getId(), saved.getRoles().size());
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
     * 권한별 메뉴 조회 (Role Entity 기반)
     * - 세션에서 가져온 roleCodes 리스트로 메뉴 조회
     * - Role Entity JOIN 쿼리 사용하여 성능 최적화
     */
    @Transactional(readOnly = true)
    public MenuTreeNodeDTO findAllTreeByRoles(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            // roleCodes가 없으면 빈 트리 반환
            return new MenuTree(Map.of()).getMenus();
        }

        // Role Entity Join 쿼리 사용
        List<Menu> menus = menuRepository.findByRoleCodes(roleCodes, D);

        Map<Long, MenuTreeNodeDTO> menuMap = menus.stream()
                .distinct()
                .map(menu -> new MenuTreeNodeDTO(menuMapper.toDTO(menu), null))
                .collect(Collectors.toMap(
                        node -> node.getMenuDTO().getId(),
                        node -> node,
                        (existing, replacement) -> existing
                ));

        return new MenuTree(menuMap).getMenus();
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

        // roles 변경 (Role Entity 기반)
        // request.getRoleCodes()가 null이 아니면 항상 업데이트 (empty도 처리)
        if (request.getRoleCodes() != null) {
            menu.getRoles().clear(); // 기존 roles 제거

            if (!request.getRoleCodes().isEmpty()) {
                Set<Role> roleEntities = new HashSet<>();
                for (String roleCode : request.getRoleCodes()) {
                    Role role = roleRepository.findByRoleCode(roleCode)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleCode));
                    roleEntities.add(role);
                }
                menu.getRoles().addAll(roleEntities); // 새 roles 추가
            }
            log.info("Menu roles updated: menuId={}, roleCodes={}", id, request.getRoleCodes());
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

