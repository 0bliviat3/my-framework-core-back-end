package com.wan.framework.menu.domain;

import com.wan.framework.menu.dto.MenuTreeNodeDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
public class MenuTree {

    private final Map<Long, MenuTreeNodeDTO> menuTreeNodeDTOs;

    public MenuTreeNodeDTO getMenus() {
        MenuTreeNodeDTO root = null;
        List<MenuTreeNodeDTO> orphanNodes = new ArrayList<>();

        for (MenuTreeNodeDTO node : menuTreeNodeDTOs.values()) {
            Long parentId = node.getMenuDTO().getParentId();

            if (parentId == null) {
                // 루트 노드
                root = node;
                continue;
            }

            MenuTreeNodeDTO parent = menuTreeNodeDTOs.get(parentId);
            if (parent == null) {
                // 고아 노드 감지 - NPE 방지
                log.warn("Orphan menu node detected: menuId={}, menuName={}, missingParentId={}",
                        node.getMenuDTO().getId(),
                        node.getMenuDTO().getName(),
                        parentId);
                orphanNodes.add(node);
                continue;  // 트리에 추가하지 않고 건너뜀
            }

            parent.addChild(node);
        }

        // 고아 노드 통계 로그
        if (!orphanNodes.isEmpty()) {
            log.error("Total orphan nodes found: count={}, menuIds={}",
                    orphanNodes.size(),
                    orphanNodes.stream()
                            .map(n -> n.getMenuDTO().getId())
                            .collect(Collectors.toList()));
        }

        return root;
    }
}
