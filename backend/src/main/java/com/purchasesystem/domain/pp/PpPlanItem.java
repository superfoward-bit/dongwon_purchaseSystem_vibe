package com.purchasesystem.domain.pp;

import lombok.Data;
import java.math.BigDecimal;

/** 구매계획 품목 (PP_PLAN_ITEM). */
@Data
public class PpPlanItem {
    private Long id;
    private Long planId;
    private Integer lineNo;
    private String itemCd;
    private String itemNm;
    private String unitCd;
    private BigDecimal planQty;
    private BigDecimal planAmt;
    private BigDecimal actualAmt;   // 입고실적(계획연도)
    private String remark;
}
