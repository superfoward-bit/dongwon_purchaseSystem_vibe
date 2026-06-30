package com.purchasesystem.domain.code;

import lombok.Data;

/**
 * 공통코드 그룹 (CM_CODE_GRP).
 */
@Data
public class CmCodeGrp {
    private Long id;
    private String compCd;
    private String grpCd;
    private String grpNm;
    private String description;
    private String useYn;
}
