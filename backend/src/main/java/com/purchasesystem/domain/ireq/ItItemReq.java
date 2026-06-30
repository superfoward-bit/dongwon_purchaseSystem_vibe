package com.purchasesystem.domain.ireq;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import com.purchasesystem.domain.item.ItCateAttr;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 품목 등록요청 (IT_ITEM_REQ). */
@Data
public class ItItemReq {
    private Long id;
    private String compCd;
    private String reqNo;
    private String itemNm;
    private String itemNmEn;
    private String cateCd;
    private String cateNm;
    private String spec;
    private String unitCd;
    private String itemTyp;
    private String taxTyp;
    private String reqDesc;
    private String sts;
    private String stsNm;
    private String createdItemCd;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<ItCateAttr> attrList = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
