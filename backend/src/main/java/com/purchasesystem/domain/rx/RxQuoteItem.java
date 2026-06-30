package com.purchasesystem.domain.rx;

import lombok.Data;

import java.math.BigDecimal;

/** 협력사 견적 품목 응답 (RX_QUOTE_ITEM). */
@Data
public class RxQuoteItem {
    private Long id;
    private Long rfxId;
    private String vdCd;
    private Long rfxItemId;
    private BigDecimal offerPrc;
    private BigDecimal offerAmt;
    private String dlvYmd;
    private String remark;
}
