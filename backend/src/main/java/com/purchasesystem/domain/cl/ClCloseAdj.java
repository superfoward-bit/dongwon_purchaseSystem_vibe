package com.purchasesystem.domain.cl;

import lombok.Data;
import java.math.BigDecimal;

/** 정산 조정명세 (갭정산 자동계산 결과) */
@Data
public class ClCloseAdj {
    private Long id;
    private Long closeId;
    private Integer lineNo;
    private String adjTyp;
    private String adjTypNm;
    private String calcBase;
    private BigDecimal baseAmt;
    private BigDecimal rate;
    private BigDecimal unitAmt;
    private String signTyp;
    private BigDecimal adjAmt;
    private String remark;
}
