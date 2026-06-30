package com.purchasesystem.domain.gr;

import lombok.Data;

import java.math.BigDecimal;

/** 입고 품목 (GR_RECEIPT_ITEM). */
@Data
public class GrItem {
    private Long id;
    private Long receiptId;
    private Integer lineNo;
    private Long poItemId;
    private String itemCd;
    private String itemNm;
    private String spec;
    private String unitCd;
    private BigDecimal poQty;
    private BigDecimal grQty;
    private BigDecimal inspPassQty;
    private BigDecimal inspFailQty;
    private BigDecimal prc;
    private String taxTyp;
    private BigDecimal amt;
    private String inspResult;
    private String lotNo;
    private String mfgYmd;
    private String expYmd;
    private String remark;
    private BigDecimal remainQty;   // 전송용: 미입고 잔량(폼 참고)
}
