package com.purchasesystem.domain.po;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 발주 헤더 (PO_ORDER). */
@Data
public class PoOrder {
    private Long id;
    private String compCd;
    private String poNo;
    private Integer poRev;
    private String poTitle;
    private String vdCd;
    private String vdNm;
    private String purcGrpCd;
    private String chrgUsrId;
    private String operOrgCd;
    private String srcTyp;
    private Long srcId;
    private String poYmd;
    private String dlvYmd;
    private String reqDlvYmd;
    private String dlvPlace;
    private String whCd;
    private String whNm;
    private String dlvCond;
    private String payCond;
    private String payTermCd;
    private String payTermNm;
    private String payMethodCd;
    private String payMethodNm;
    private String currCd;
    private BigDecimal suplAmt;
    private BigDecimal vatAmt;
    private BigDecimal totAmt;
    private String poTyp;
    private String poTypNm;
    private String purcTyp;
    private String purcTypNm;
    private String performBondYn;
    private BigDecimal performBondRate;
    private String maintBondYn;
    private BigDecimal maintBondRate;
    private String payPlanYn;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<PoItem> items = new ArrayList<>();
    private List<PoPayPlan> payPlans = new ArrayList<>();
    private List<PoDlvSchedule> dlvSchedules = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private com.purchasesystem.domain.approval.AppApproval approval;
}
