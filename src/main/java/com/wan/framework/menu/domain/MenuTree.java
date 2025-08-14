package com.wan.framework.menu.domain;

import com.wan.framework.menu.dto.MenuTreeNodeDTO;
import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
public class MenuTree {

    private final Map<Long, MenuTreeNodeDTO> menuTreeNodeDTOs;

    public MenuTreeNodeDTO getMenus() {
        MenuTreeNodeDTO root = null;
        for (MenuTreeNodeDTO node : menuTreeNodeDTOs.values()) {
            Long parentId = node.getMenuDTO().getParentId();
            if (parentId == null) {
                // root
                root = node;
                continue;
            }
            MenuTreeNodeDTO parent = menuTreeNodeDTOs.get(parentId);
            parent.addChild(node);
        }
        return root;
    }
}
