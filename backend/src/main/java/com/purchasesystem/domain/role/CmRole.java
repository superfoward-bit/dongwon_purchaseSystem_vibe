package com.purchasesystem.domain.role;

import lombok.Data;

/**
 * 역할 (CM_ROLE).
 */
@Data
public class CmRole {
    private Long id;
    private String roleId;
    private String roleNm;
    private String description;
    private String useYn;
    private String regId;
    private String modId;
}
