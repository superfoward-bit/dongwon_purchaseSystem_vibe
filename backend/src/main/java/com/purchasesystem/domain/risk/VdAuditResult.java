package com.purchasesystem.domain.risk;

import lombok.Data;

/** 협력사 감시 항목결과 (VD_AUDIT_RESULT). */
@Data
public class VdAuditResult {
    private Long id;
    private Long auditId;
    private Integer itemSeq;
    private String auditItemNm;
    private String result;     // SAFE/WATCH/RISK
    private String finding;
    private String actionReq;
}
