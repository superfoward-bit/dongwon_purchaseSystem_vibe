package com.purchasesystem.domain.menu;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.menu.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;

    /** 사용자 메뉴를 트리 구조로 반환 */
    public List<CmMenu> getUserMenuTree(String compCd, String usrId) {
        List<CmMenu> flat = menuMapper.findMenusByUser(compCd, usrId);
        return toTree(flat);
    }

    /** 전체 메뉴 트리 (관리용) */
    public List<CmMenu> getAllMenuTree() {
        return toTree(menuMapper.findAllMenus());
    }

    public List<CmMenu> getAllMenuFlat() {
        return menuMapper.findAllMenus();
    }

    @Transactional
    public void createMenu(CmMenu menu, String userId) {
        if (menuMapper.countByMenuId(menu.getMenuId()) > 0) {
            throw new BusinessException("이미 존재하는 메뉴ID입니다: " + menu.getMenuId());
        }
        menu.setRegId(userId);
        menuMapper.insertMenu(menu);
    }

    @Transactional
    public void updateMenu(CmMenu menu, String userId) {
        menu.setModId(userId);
        if (menuMapper.updateMenu(menu) == 0) {
            throw new BusinessException("수정 대상이 없습니다.");
        }
    }

    @Transactional
    public void deleteMenu(Long id, String menuId, String userId) {
        if (menuId != null && menuMapper.countChildren(menuId) > 0) {
            throw new BusinessException("하위 메뉴가 있어 삭제할 수 없습니다.");
        }
        menuMapper.deleteMenu(id, userId);
    }

    private List<CmMenu> toTree(List<CmMenu> flat) {
        Map<String, CmMenu> map = new LinkedHashMap<>();
        for (CmMenu m : flat) {
            map.put(m.getMenuId(), m);
        }
        List<CmMenu> roots = new ArrayList<>();
        for (CmMenu m : flat) {
            if (m.getUpMenuId() != null && map.containsKey(m.getUpMenuId())) {
                map.get(m.getUpMenuId()).getChildren().add(m);
            } else {
                roots.add(m);
            }
        }
        return roots;
    }
}
