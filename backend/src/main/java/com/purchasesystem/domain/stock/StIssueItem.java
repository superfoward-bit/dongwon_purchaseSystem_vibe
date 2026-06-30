package com.purchasesystem.domain.stock;

import lombok.Data;
import java.math.BigDecimal;

/** 재고 출고 품목 (ST_ISSUE_ITEM). */
@Data
public class StIssueItem {
    private Long id;
    private Long issueId;
    private Integer lineNo;
    private String itemCd;
    private String itemNm;
    private String lotNo;
    private String unitCd;
    private BigDecimal qty;
    private String remark;
}
