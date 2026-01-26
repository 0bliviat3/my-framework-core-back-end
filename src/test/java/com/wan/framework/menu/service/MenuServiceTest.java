package com.wan.framework.menu.service;

import com.wan.framework.base.constant.DataStateCode;
import com.wan.framework.menu.dto.MenuDTO;
import com.wan.framework.menu.repositoty.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuRepository menuRepository;

    @Test
    @DisplayName("최상위 메뉴 삭제 시 하위 메뉴도 함께 삭제")
    void deleteMenu_WithChildren_ShouldDeleteAllDescendants() {
        // Given: 3단계 메뉴 구조
        MenuDTO rootDTO = createMenu("Root", null);
        MenuDTO child1DTO = createMenu("Child1", rootDTO.getId());
        MenuDTO child2DTO = createMenu("Child2", rootDTO.getId());
        MenuDTO grandChildDTO = createMenu("GrandChild", child1DTO.getId());

        // When: 최상위 메뉴 삭제
        menuService.deleteMenu(rootDTO.getId());

        // Then: 모든 하위 메뉴도 삭제됨
        assertThat(menuRepository.findByIdAndDataStateCodeNot(rootDTO.getId(), DataStateCode.D))
                .isEmpty();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(child1DTO.getId(), DataStateCode.D))
                .isEmpty();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(child2DTO.getId(), DataStateCode.D))
                .isEmpty();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(grandChildDTO.getId(), DataStateCode.D))
                .isEmpty();
    }

    @Test
    @DisplayName("중간 메뉴 삭제 시 하위만 삭제, 상위는 유지")
    void deleteMenu_MiddleNode_ShouldDeleteOnlyDescendants() {
        // Given: 3단계 메뉴 구조
        MenuDTO rootDTO = createMenu("Root", null);
        MenuDTO childDTO = createMenu("Child", rootDTO.getId());
        MenuDTO grandChildDTO = createMenu("GrandChild", childDTO.getId());

        // When: 중간 메뉴 삭제
        menuService.deleteMenu(childDTO.getId());

        // Then: 부모는 유지, 본인과 하위만 삭제
        assertThat(menuRepository.findByIdAndDataStateCodeNot(rootDTO.getId(), DataStateCode.D))
                .isPresent(); // 부모는 유지
        assertThat(menuRepository.findByIdAndDataStateCodeNot(childDTO.getId(), DataStateCode.D))
                .isEmpty();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(grandChildDTO.getId(), DataStateCode.D))
                .isEmpty();
    }

    @Test
    @DisplayName("리프 노드 삭제 시 자신만 삭제")
    void deleteMenu_LeafNode_ShouldDeleteOnlyItself() {
        // Given: 2단계 메뉴 구조
        MenuDTO rootDTO = createMenu("Root", null);
        MenuDTO leafDTO = createMenu("Leaf", rootDTO.getId());

        // When: 리프 노드 삭제
        menuService.deleteMenu(leafDTO.getId());

        // Then: 부모는 유지, 본인만 삭제
        assertThat(menuRepository.findByIdAndDataStateCodeNot(rootDTO.getId(), DataStateCode.D))
                .isPresent();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(leafDTO.getId(), DataStateCode.D))
                .isEmpty();
    }

    @Test
    @DisplayName("복잡한 트리 구조에서 삭제 테스트")
    void deleteMenu_ComplexTree_ShouldDeleteCorrectly() {
        // Given: 복잡한 트리 구조
        //       Root
        //      /    \
        //   Child1  Child2
        //   /   \
        // GC1   GC2
        MenuDTO rootDTO = createMenu("Root", null);
        MenuDTO child1DTO = createMenu("Child1", rootDTO.getId());
        MenuDTO child2DTO = createMenu("Child2", rootDTO.getId());
        MenuDTO grandChild1DTO = createMenu("GrandChild1", child1DTO.getId());
        MenuDTO grandChild2DTO = createMenu("GrandChild2", child1DTO.getId());

        // When: Child1 삭제 (GC1, GC2도 삭제되어야 함)
        menuService.deleteMenu(child1DTO.getId());

        // Then: Root와 Child2는 유지, Child1과 그 하위는 삭제
        assertThat(menuRepository.findByIdAndDataStateCodeNot(rootDTO.getId(), DataStateCode.D))
                .isPresent();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(child2DTO.getId(), DataStateCode.D))
                .isPresent();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(child1DTO.getId(), DataStateCode.D))
                .isEmpty();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(grandChild1DTO.getId(), DataStateCode.D))
                .isEmpty();
        assertThat(menuRepository.findByIdAndDataStateCodeNot(grandChild2DTO.getId(), DataStateCode.D))
                .isEmpty();
    }

    /**
     * 테스트용 메뉴 생성 헬퍼 메서드
     */
    private MenuDTO createMenu(String name, Long parentId) {
        MenuDTO menuDTO = MenuDTO.builder()
                .name(name)
                .type("MENU")
                .parentId(parentId)
                .build();
        return menuService.createMenu(menuDTO);
    }
}
