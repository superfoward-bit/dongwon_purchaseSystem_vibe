package com.purchasesystem.domain.rx;

import lombok.Data;

import java.math.BigDecimal;

/** 견적 품목 (RX_RFX_ITEM). */
@Data
public class RxItem {
    private Long id;
    private Long rfxId;
    private Integer lineNo;
    private Long prItemId;
    private String itemCd;
    private String itemNm;
    private String spec;
    private String unitCd;
    private BigDecimal qty;
    private BigDecimal basePrc;
}
