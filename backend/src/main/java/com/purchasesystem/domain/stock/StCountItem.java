package com.purchasesystem.domain.stock;

import lombok.Data;
import java.math.BigDecimal;

/** 재고실사 품목 (ST_COUNT_ITEM). */
@Data
public class StCountItem {
    private Long id;
    private Long countId;
    private Integer lineNo;
    private String itemCd;
    private String itemNm;
    private String lotNo;
    private String unitCd;
    private BigDecimal bookQty;
    private BigDecimal realQty;
    private BigDecimal diffQty;
    private String remark;
}
