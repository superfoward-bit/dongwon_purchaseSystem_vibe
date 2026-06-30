package com.purchasesystem.domain.qc;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 품질 부적합 (QC_NONCONF). */
@Data
public class QcNonconf {
    private Long id;
    private String compCd;
    private String ncNo;
    private String vdCd;
    private String vdNm;
    private String srcTyp;
    private Long srcId;
    private String refNo;
    private String itemCd;
    private String itemNm;
    private String ncTyp;
    private String ncTypNm;
    private String severity;
    private String severityNm;
    private String detectYmd;
    private String lotNo;
    private String mfgYmd;
    private String expYmd;
    private BigDecimal ncQty;
    private String ncDesc;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<QcAction> actionList = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
