package com.purchasesystem.domain.iface;

import lombok.Data;

/** 대외 인터페이스 송수신 이력 (IF_INTERFACE). */
@Data
public class IfInterface {
    private Long id;
    private String compCd;
    private String ifNo;
    private String ifTyp;
    private String ifTypNm;
    private String ifNm;
    private String direction;
    private String sysCd;
    private String refTyp;
    private Long refId;
    private String refNo;
    private String payload;
    private String ifStatus;
    private String ifStatusNm;
    private String sentDt;
    private String resultMsg;
    private Integer retryCnt;
    private String regId;
    private String regDt;
}
