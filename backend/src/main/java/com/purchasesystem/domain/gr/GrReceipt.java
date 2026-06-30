package com.purchasesystem.domain.gr;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 입고/검수 헤더 (GR_RECEIPT). */
@Data
public class GrReceipt {
    private Long id;
    private String compCd;
    private String grNo;
    private Long poId;
    private String poNo;
    private String vdCd;
    private String vdNm;
    private String operOrgCd;
    private String whCd;
    private String whNm;
    private String delyNo;
    private String grYmd;
    private String inspUsrId;
    private String inspYmd;
    private BigDecimal totAmt;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<GrItem> items = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private com.purchasesystem.domain.approval.AppApproval approval;
}
