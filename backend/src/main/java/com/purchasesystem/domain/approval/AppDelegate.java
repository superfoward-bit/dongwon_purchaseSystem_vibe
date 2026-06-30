package com.purchasesystem.domain.approval;

import lombok.Data;

/** 결재 위임/대결 (AP_DELEGATE). */
@Data
public class AppDelegate {
    private Long id;
    private String compCd;
    private String fromUsrId;
    private String fromUsrNm;
    private String toUsrId;
    private String toUsrNm;
    private String validSd;
    private String validEd;
    private String remark;
    private String useYn;
    private String regId;
}
