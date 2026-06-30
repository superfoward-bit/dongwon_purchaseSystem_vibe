package com.purchasesystem.domain.item;

import lombok.Data;

/** 품목마스터 (IT_ITEM). */
@Data
public class ItItem {
    private Long id;
    private String compCd;
    private String itemCd;
    private String erpItemCd;
    private String itemNm;
    private String itemNmEn;
    private String spec;
    private String cateCd;
    private String cateNm;
    private String unitCd;
    private String itemTyp;
    private String itemTypNm;
    private String taxTyp;
    private String taxTypNm;
    private java.math.BigDecimal safetyStock;
    private java.math.BigDecimal reorderPoint;
    private java.math.BigDecimal orderQty;
    private java.math.BigDecimal moq;
    private Integer leadTime;
    private String repVdCd;
    private String repVdNm;
    private String itemSts;
    private String itemStsNm;
    private String remark;
    private String useYn;
    private String regId;
    private String modId;
}
