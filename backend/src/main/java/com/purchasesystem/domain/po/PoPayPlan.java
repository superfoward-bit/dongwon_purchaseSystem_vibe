package com.purchasesystem.domain.po;

import lombok.Data;
import java.math.BigDecimal;

/** 발주 분할결제(선금/기성/잔금) 계획 (PO_PAY_PLAN). */
@Data
public class PoPayPlan {
    private Long id;
    private Long orderId;
    private Integer lineNo;
    private String payTyp;
    private String payTypNm;
    private String payNm;
    private BigDecimal rate;
    private BigDecimal amt;
    private String planYmd;
    private String paidYn;
    private String remark;
}
