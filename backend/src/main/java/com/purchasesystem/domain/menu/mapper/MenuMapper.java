package com.purchasesystem.domain.menu.mapper;

import com.purchasesystem.domain.menu.CmMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MenuMapper {

    /** 사용자가 접근 가능한 메뉴 (역할권한 기준, 상위메뉴 포함) */
    List<CmMenu> findMenusByUser(@Param("compCd") String compCd, @Param("usrId") String usrId);

    List<CmMenu> findAllMenus();

    int countByMenuId(@Param("menuId") String menuId);

    int insertMenu(CmMenu menu);

    int updateMenu(CmMenu menu);

    int deleteMenu(@Param("id") Long id, @Param("modId") String modId);

    int countChildren(@Param("menuId") String menuId);
}
