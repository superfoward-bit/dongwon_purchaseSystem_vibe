package com.purchasesystem.domain.risk;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 협력사 감시 (VD_AUDIT). */
@Data
public class VdAudit {
    private Long id;
    private String compCd;
    private String auditNo;
    private String vdCd;
    private String vdNm;
    private String auditTyp;
    private String auditTypNm;
    private String auditYmd;
    private String auditorId;
    private String resultGrade;
    private String resultGradeNm;
    private Integer findingCnt;
    private String actionReqYn;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<VdAuditResult> results = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
