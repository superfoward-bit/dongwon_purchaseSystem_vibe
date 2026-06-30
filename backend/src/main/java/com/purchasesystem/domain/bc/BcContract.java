package com.purchasesystem.domain.bc;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 표준거래계약(거래기본계약) 헤더 (BC_CONTRACT). */
@Data
public class BcContract {
    private Long id;
    private String compCd;
    private String bcNo;
    private Integer bcRev;
    private String bcTitle;
    private String bcTyp;
    private String bcTypNm;
    private String vdCd;
    private String vdNm;
    private String purcGrpCd;
    private String chrgUsrId;
    private String chrgUsrNm;
    private String validSd;
    private String validEd;
    private String payCond;
    private String delyCond;
    private String currCd;
    private String autoRenewYn;
    private Integer noticeDays;
    private Long prevId;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;
    // 만료예정 표시용 (D-day)
    private Integer ddayLeft;

    private List<BcTerm> terms = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private com.purchasesystem.domain.approval.AppApproval approval;
}
