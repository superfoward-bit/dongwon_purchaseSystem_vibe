package com.purchasesystem.domain.notice;

import lombok.Data;

/** 알림(메일/SMS) 발송이력 (CM_NOTICE). */
@Data
public class CmNotice {
    private Long id;
    private String compCd;
    private String notiTyp;     // EMAIL / SMS
    private String notiTypNm;
    private String toAddr;
    private String toUsrId;
    private String title;
    private String content;
    private String refTyp;
    private Long refId;
    private String eventCd;
    private String sendSts;     // READY / SENT / FAIL
    private String sendStsNm;
    private String sentDt;
    private String errMsg;
    private String regId;
    private String regDt;
}
