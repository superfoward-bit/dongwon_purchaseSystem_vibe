package com.purchasesystem.domain.cl;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 정산마감 헤더 (CL_CLOSE). */
@Data
public class ClClose {
    private Long id;
    private String compCd;
    private String closeNo;
    private String closeYm;
    private String vdCd;
    private String vdNm;
    private BigDecimal totSuplAmt;
    private BigDecimal totVatAmt;
    private BigDecimal totAmt;
    private BigDecimal adjAmt;
    private BigDecimal netAmt;
    private Integer grCnt;
    private String payTermCd;
    private String payTermNm;
    private String payMethodCd;
    private String payMethodNm;
    private String payDueYmd;
    private String lockYn;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<ClCloseGr> grs = new ArrayList<>();
    private List<ClCloseAdj> adjs = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private com.purchasesystem.domain.approval.AppApproval approval;
}
