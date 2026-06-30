package com.purchasesystem.domain.pa;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 매입조정 (PA_ADJUST). */
@Data
public class PaAdjust {
    private Long id;
    private String compCd;
    private String adjNo;
    private String adjTyp;
    private String adjTypNm;
    private String vdCd;
    private String vdNm;
    private Long grId;
    private String grNo;
    private String adjYm;
    private String taxTyp;
    private BigDecimal suplAdjAmt;
    private BigDecimal vatAdjAmt;
    private BigDecimal totAdjAmt;
    private String reason;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private com.purchasesystem.domain.approval.AppApproval approval;
}
