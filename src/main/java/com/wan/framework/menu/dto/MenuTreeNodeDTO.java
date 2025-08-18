package com.wan.framework.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public class MenuTreeNodeDTO {
    private MenuDTO menuDTO;
    private List<MenuTreeNodeDTO> children;

    public void addChild(MenuTreeNodeDTO child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }
}
