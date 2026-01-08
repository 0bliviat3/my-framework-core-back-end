package com.wan.framework.menu.service;

import com.wan.framework.menu.domain.Menu;
import com.wan.framework.menu.dto.MenuDTO;
import com.wan.framework.menu.exception.MenuException;
import com.wan.framework.menu.repositoty.MenuRepository;
import com.wan.framework.program.service.ProgramPermissionIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.wan.framework.base.constant.DataStateCode.D;
import static com.wan.framework.menu.constant.MenuExceptionMessage.NOT_FOUND_MENU;

/**
 * Menu-Program-API 연계 서비스
 * - 메뉴별 필요한 API 권한 자동 계산
 * - Role이 메뉴를 볼 수 있는지 확인
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuPermissionService {

    private final MenuRepository menuRepository;
    private final ProgramPermissionIntegrationService programPermissionIntegrationService;

    /**
     * 메뉴에 필요한 모든 API 계산 (재귀적)
     * - 해당 메뉴와 모든 부모 메뉴의 프로그램에서 필요한 API 목록
     *
     * @param menuId 메뉴 ID
     * @return API 식별자 Set (service_id::method::uri)
     */
    @Transactional(readOnly = true)
    public Set<String> getRequiredApisForMenu(Long menuId) {
        Menu menu = menuRepository.findByIdAndDataStateCodeNot(menuId, D)
                .orElseThrow(() -> new MenuException(NOT_FOUND_MENU));

        Set<String> requiredApis = new HashSet<>();

        // 현재 메뉴의 프로그램에서 필요한 API
        if (menu.getProgram() != null) {
            Set<String> apis = programPermissionIntegrationService
                    .calculateRequiredApisForMenu(menu.getProgram().getId());
            requiredApis.addAll(apis);
        }

        // 부모 메뉴의 프로그램에서 필요한 API (재귀)
        Menu parent = menu.getParent();
        while (parent != null) {
            if (parent.getProgram() != null) {
                Set<String> parentApis = programPermissionIntegrationService
                        .calculateRequiredApisForMenu(parent.getProgram().getId());
                requiredApis.addAll(parentApis);
            }
            parent = parent.getParent();
        }

        log.debug("Menu {} requires {} APIs", menu.getName(), requiredApis.size());
        return requiredApis;
    }

    /**
     * Role이 특정 메뉴를 볼 수 있는지 확인
     * - 메뉴에 필요한 모든 API에 대한 권한이 있는지 확인
     *
     * @param menuId 메뉴 ID
     * @param roleApis Role이 접근 가능한 API 식별자 Set
     * @return true if role can access menu
     */
    @Transactional(readOnly = true)
    public boolean canAccessMenu(Long menuId, Set<String> roleApis) {
        Set<String> requiredApis = getRequiredApisForMenu(menuId);

        // 필요한 API가 없으면 접근 가능 (공개 메뉴)
        if (requiredApis.isEmpty()) {
            return true;
        }

        // Role이 모든 필수 API에 접근 가능한지 확인
        return roleApis.containsAll(requiredApis);
    }

    /**
     * Role별 접근 가능한 메뉴 목록 필터링
     *
     * @param allMenus 전체 메뉴 목록
     * @param roleApis Role이 접근 가능한 API 식별자 Set
     * @return 접근 가능한 메뉴 목록
     */
    @Transactional(readOnly = true)
    public List<MenuDTO> filterAccessibleMenus(List<MenuDTO> allMenus, Set<String> roleApis) {
        return allMenus.stream()
                .filter(menu -> canAccessMenu(menu.getId(), roleApis))
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 모든 Role을 기반으로 접근 가능한 API Set 생성
     *
     * @param roles 사용자의 Role 목록 (예: ["ROLE_USER", "ROLE_MANAGER"])
     * @return 접근 가능한 API 식별자 Set
     */
    public Set<String> calculateUserApiAccess(List<String> roles) {
        // 실제로는 PermissionCacheService를 사용하여 Role별 API 권한 조회
        // 여기서는 인터페이스만 제공
        Set<String> userApis = new HashSet<>();

        for (String roleCode : roles) {
            // TODO: PermissionCacheService.getRoleApis(roleCode) 호출
            // userApis.addAll(permissionCacheService.getRoleApis(roleCode));
        }

        return userApis;
    }

    /**
     * 메뉴 ID 목록에서 접근 가능한 메뉴만 필터링
     *
     * @param menuIds 메뉴 ID 목록
     * @param roleApis Role이 접근 가능한 API Set
     * @return 접근 가능한 메뉴 ID 목록
     */
    @Transactional(readOnly = true)
    public List<Long> filterAccessibleMenuIds(List<Long> menuIds, Set<String> roleApis) {
        return menuIds.stream()
                .filter(menuId -> canAccessMenu(menuId, roleApis))
                .collect(Collectors.toList());
    }
}
