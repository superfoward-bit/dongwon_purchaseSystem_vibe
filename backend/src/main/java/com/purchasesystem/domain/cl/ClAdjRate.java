package com.purchasesystem.domain.cl;

import lombok.Data;
import java.math.BigDecimal;

/** 정산 조정율 마스터 */
@Data
public class ClAdjRate {
    private Long id;
    private String compCd;
    private String vdCd;
    private String vdNm;
    private String adjTyp;
    private String adjTypNm;
    private String calcBase;
    private BigDecimal rate;
    private BigDecimal unitAmt;
    private String signTyp;
    private String validSd;
    private String validEd;
    private String remark;
    private String useYn;
    private String regId;
}
