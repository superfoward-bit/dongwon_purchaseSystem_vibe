package com.purchasesystem.domain.qc;

import lombok.Data;

/** 품질 개선조치 (QC_ACTION). */
@Data
public class QcAction {
    private Long id;
    private Long ncId;
    private Integer lineNo;
    private String actionTyp;
    private String actionTypNm;
    private String rootCause;
    private String actionDesc;
    private String respNm;
    private String dueYmd;
    private String completeYmd;
    private String resultSts;
    private String resultStsNm;
    private String verifyUsr;
    private String verifyYmd;
    private String effectResult;
    private String effectResultNm;
    private String recurPrevent;
    private String remark;
}
