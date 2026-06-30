package com.purchasesystem.domain.rx;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 견적 헤더 (RX_RFX). */
@Data
public class RxRfx {
    private Long id;
    private String compCd;
    private String rfxNo;
    private String rfxTyp;
    private String rfxTitle;
    private String purcGrpCd;
    private String chrgUsrId;
    private String openYmd;
    private String closeYmd;
    private String evalTyp;
    private String currCd;
    private String srcTyp;
    private Long srcId;
    private String selVdCd;
    private String selVdNm;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<RxItem> items = new ArrayList<>();
    private List<RxVendor> vendors = new ArrayList<>();
    private List<RxQuoteItem> quoteItems = new ArrayList<>();   // 비교용 전체 응답
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
