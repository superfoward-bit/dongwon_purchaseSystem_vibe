package com.purchasesystem.domain.pr;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 구매요청 헤더 (PR_REQUEST). */
@Data
public class PrRequest {
    private Long id;
    private String compCd;
    private String prNo;
    private String prTitle;
    private String prTyp;
    private String purcGrpCd;
    private String reqDeptCd;
    private String reqUsrId;
    private String operOrgCd;
    private String reqYmd;        // yyyy-MM-dd
    private String hopeDlvYmd;
    private String currCd;
    private String budgetCd;
    private String budgetNm;
    private BigDecimal totAmt;
    private String sts;
    private String stsNm;         // 상태명(표시용)
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<PrItem> items = new ArrayList<>();
    private List<CmStatusFlow> actions;   // 현재 상태에서 가능한 액션
    private List<DocStatusHis> history;    // 진행이력
    private com.purchasesystem.domain.approval.AppApproval approval;  // 결재 현황
}
