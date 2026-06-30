package com.purchasesystem.domain.stock;

import lombok.Data;
import java.math.BigDecimal;

/** 재고 수불부 (ST_LEDGER). */
@Data
public class StLedger {
    private Long id;
    private String compCd;
    private String transTyp;
    private String transTypNm;
    private String whCd;
    private String whNm;
    private String itemCd;
    private String itemNm;
    private String lotNo;
    private BigDecimal inQty;
    private BigDecimal outQty;
    private BigDecimal balQty;
    private String unitCd;
    private String srcTyp;
    private Long srcId;
    private String srcNo;
    private String transYmd;
    private String remark;
    private String regId;
}
