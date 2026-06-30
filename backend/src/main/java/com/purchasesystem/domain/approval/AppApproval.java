package com.purchasesystem.domain.approval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 전자결재 마스터 (AP_APPROVAL). */
@Data
public class AppApproval {
    private Long id;
    private String compCd;
    private String aprvNo;
    private String docTyp;
    private Long docId;
    private String docNo;
    private String aprvTitle;
    private String draftUsrId;
    private String draftUsrNm;
    private String draftDt;
    private Integer curStepNo;
    private String aprvSts;
    private String aprvStsNm;
    private String finalAprvDt;
    private List<AppLine> lines = new ArrayList<>();
}
