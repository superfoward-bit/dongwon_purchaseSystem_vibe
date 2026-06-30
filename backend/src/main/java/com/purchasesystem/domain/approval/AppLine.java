package com.purchasesystem.domain.approval;

import lombok.Data;

/** 결재선 (AP_APPROVAL_LINE). */
@Data
public class AppLine {
    private Long id;
    private Long aprvId;
    private Integer stepNo;
    private String aprvUsrId;
    private String aprvUsrNm;
    private String lineTyp;       // APRV결재/AGRE합의/REFR참조
    private String lineTypNm;
    private String finalYn;       // 전결권 보유
    private String actAsUsrId;    // 대결 처리자
    private String lineSts;
    private String lineStsNm;
    private String aprvDt;
    private String opinion;
    // 결재함 표시용
    private String aprvNo;
    private String docTyp;
    private Long docId;
    private String docNo;
    private String aprvTitle;
    private String draftUsrNm;
}
