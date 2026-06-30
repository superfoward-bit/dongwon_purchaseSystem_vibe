package com.purchasesystem.domain.ct;

import lombok.Data;

import java.math.BigDecimal;

/** 단가계약 품목 (CT_CONTRACT_ITEM). */
@Data
public class CtItem {
    private Long id;
    private Long contractId;
    private Integer lineNo;
    private String itemCd;
    private String itemNm;
    private String spec;
    private String unitCd;
    private BigDecimal ctPrc;
    private String applySd;
    private String applyEd;
    private BigDecimal minQty;
    private BigDecimal maxQty;
    private String remark;
}
