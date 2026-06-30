package com.purchasesystem.domain.code;

import lombok.Data;

/**
 * 공통코드 상세 (CM_CODE).
 */
@Data
public class CmCode {
    private Long id;
    private String compCd;
    private String grpCd;
    private String cd;
    private String cdNmKo;
    private String cdNmEn;
    private Integer sortNo;
    private String attr1;
    private String attr2;
    private String attr3;
    private String attr4;
    private String useYn;
    private String regId;
    private String modId;
}
