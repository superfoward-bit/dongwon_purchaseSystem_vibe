package com.purchasesystem.domain.po;

import lombok.Data;

import java.math.BigDecimal;

/** 발주 분할납품 일정 (PO_DLV_SCHEDULE). */
@Data
public class PoDlvSchedule {
    private Long id;
    private Long orderId;
    private Long poItemId;
    private Integer lineNo;
    private String itemCd;
    private String itemNm;
    private BigDecimal planQty;
    private String planYmd;
    private String dlvPlace;
    private String sts;
    private String remark;
}
