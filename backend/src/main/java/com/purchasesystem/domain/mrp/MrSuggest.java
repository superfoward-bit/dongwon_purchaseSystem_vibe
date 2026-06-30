package com.purchasesystem.domain.mrp;

import lombok.Data;
import java.math.BigDecimal;

/** MRP 발주제안 (MR_SUGGEST). */
@Data
public class MrSuggest {
    private Long id;
    private String compCd;
    private String runYmd;
    private String itemCd;
    private String itemNm;
    private String unitCd;
    private BigDecimal curQty;
    private BigDecimal onOrderQty;
    private BigDecimal availQty;
    private BigDecimal safetyStock;
    private BigDecimal reorderPoint;
    private BigDecimal shortageQty;
    private BigDecimal suggestQty;
    private String repVdCd;
    private String repVdNm;
    private String sts;
    private String stsNm;
    private String srcPrNo;
    private String regId;
    // 발주정책(계산용)
    private BigDecimal orderQty;
    private BigDecimal moq;
}
