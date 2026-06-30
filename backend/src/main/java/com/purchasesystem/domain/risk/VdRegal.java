package com.purchasesystem.domain.risk;

import lombok.Data;

/** 규제/법령 (VD_REGAL). */
@Data
public class VdRegal {
    private Long id;
    private String regalNo;
    private String regalNm;
    private String regalTyp;
    private String regalTypNm;
    private String planYmd;
    private String notifyYmd;
    private String enforceYmd;
    private String targetDesc;
    private String alarmYn;
    private Integer alarmBeforeDay;
    private String sts;
    private String stsNm;
    private String remark;
    private Integer daysToEnforce;   // 시행일까지 남은 일수(조회용)
    private String useYn;
    private String regId;
    private String modId;
}
