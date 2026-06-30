package com.purchasesystem.domain.po;

import lombok.Data;

import java.math.BigDecimal;

/** 발주 품목 (PO_ORDER_ITEM). */
@Data
public class PoItem {
    private Long id;
    private Long orderId;
    private Integer lineNo;
    private Long prItemId;
    private String itemCd;
    private String itemNm;
    private String spec;
    private String unitCd;
    private BigDecimal qty;
    private BigDecimal prc;
    private String taxTyp;
    private BigDecimal suplAmt;
    private BigDecimal vatAmt;
    private BigDecimal amt;
    private String dlvYmd;
    private String reqDlvYmd;
    private String whCd;
    private BigDecimal grQty;
    private String remark;
}
