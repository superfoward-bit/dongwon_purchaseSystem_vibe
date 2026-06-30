package com.purchasesystem.domain.item;

import lombok.Data;

/** 품목분류 (IT_CATEGORY). */
@Data
public class ItCategory {
    private Long id;
    private String cateCd;
    private String cateNm;
    private String upCateCd;
    private Integer cateLvl;
    private Integer sortNo;
    private String leafYn;
    private String useYn;
    private String regId;
    private String modId;
}
