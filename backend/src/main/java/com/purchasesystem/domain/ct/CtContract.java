package com.purchasesystem.domain.ct;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 단가계약 헤더 (CT_CONTRACT). */
@Data
public class CtContract {
    private Long id;
    private String compCd;
    private String ctNo;
    private Integer ctRev;
    private String ctTitle;
    private String ctTyp;
    private String vdCd;
    private String vdNm;
    private String purcGrpCd;
    private String chrgUsrId;
    private String validSd;
    private String validEd;
    private String currCd;
    private BigDecimal suplAmt;
    private BigDecimal vatAmt;
    private BigDecimal totAmt;
    private String srcTyp;
    private Long srcId;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<CtItem> items = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private com.purchasesystem.domain.approval.AppApproval approval;
}
