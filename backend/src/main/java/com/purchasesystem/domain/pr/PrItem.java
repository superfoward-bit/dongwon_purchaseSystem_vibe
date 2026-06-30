package com.purchasesystem.domain.pr;

import lombok.Data;

import java.math.BigDecimal;

/** 구매요청 품목 (PR_ITEM). */
@Data
public class PrItem {
    private Long id;
    private Long requestId;
    private Integer lineNo;
    private String itemCd;
    private String newItemYn;
    private String itemNm;
    private String spec;
    private String unitCd;
    private BigDecimal qty;
    private BigDecimal estPrc;
    private BigDecimal amt;
    private String hopeDlvYmd;
    private String dlvPlace;
    private String remark;
}
