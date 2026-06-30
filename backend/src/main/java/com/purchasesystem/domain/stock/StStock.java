package com.purchasesystem.domain.stock;

import lombok.Data;
import java.math.BigDecimal;

/** 재고 현황 (ST_STOCK). */
@Data
public class StStock {
    private Long id;
    private String compCd;
    private String whCd;
    private String whNm;
    private String itemCd;
    private String itemNm;
    private String lotNo;
    private String mfgYmd;
    private String expYmd;
    private String unitCd;
    private BigDecimal qty;
    private String lastInYmd;
    private String lastOutYmd;
}
