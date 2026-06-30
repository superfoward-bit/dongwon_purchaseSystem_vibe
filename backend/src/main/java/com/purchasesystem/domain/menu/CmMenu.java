package com.purchasesystem.domain.menu;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 메뉴 (CM_MENU).
 */
@Data
public class CmMenu {
    private Long id;
    private String menuId;
    private String menuNm;
    private String upMenuId;
    private Integer menuLvl;
    private String url;
    private String icon;
    private Integer sortNo;
    private String authCd;            // 현재 사용자의 해당 메뉴 권한
    private String useYn;
    private String regId;
    private String modId;
    private List<CmMenu> children = new ArrayList<>();
}
