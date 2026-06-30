package com.purchasesystem.domain.role;

import lombok.Data;

/**
 * 역할별 메뉴권한 (CM_ROLE_FUNC).
 */
@Data
public class CmRoleFunc {
    private String roleId;
    private String menuId;
    private String authCd;   // READ/SAVE/APPROVAL/MANAGEMENT
}
