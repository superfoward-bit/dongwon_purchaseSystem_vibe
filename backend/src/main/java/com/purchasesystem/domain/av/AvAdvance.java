package com.purchasesystem.domain.av;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 선급금 (AV_ADVANCE). */
@Data
public class AvAdvance {
    private Long id;
    private String compCd;
    private String advNo;
    private String vdCd;
    private String vdNm;
    private BigDecimal advAmt;
    private String payYmd;
    private String purpose;
    private BigDecimal balance;
    private BigDecimal repaidAmt;   // 반제합(표시용)
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<AvRepay> repays = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private com.purchasesystem.domain.approval.AppApproval approval;
}
