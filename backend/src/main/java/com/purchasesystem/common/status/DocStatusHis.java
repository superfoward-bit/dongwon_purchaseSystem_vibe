package com.purchasesystem.common.status;

import lombok.Data;

/** 문서 상태이력 (LOG_DOC_STATUS_HIS). */
@Data
public class DocStatusHis {
    private Long id;
    private String docTyp;
    private Long docId;
    private String docNo;
    private Integer seq;
    private String fromSts;
    private String toSts;
    private String action;
    private String actionNm;
    private String actorId;
    private String actionDt;
    private String rsn;
}
