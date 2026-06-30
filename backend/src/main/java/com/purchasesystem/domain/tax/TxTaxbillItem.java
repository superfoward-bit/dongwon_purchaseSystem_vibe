package com.purchasesystem.domain.tax;

import lombok.Data;
import java.math.BigDecimal;

/** 세금계산서 품목명세 (TX_TAXBILL_ITEM). */
@Data
public class TxTaxbillItem {
    private Long id;
    private Long billId;
    private Integer lineNo;
    private String itemCd;
    private String itemNm;
    private String spec;
    private BigDecimal qty;
    private BigDecimal unitPrc;
    private BigDecimal supplyAmt;
    private BigDecimal vatAmt;
    private String remark;
    private String regId;
}
