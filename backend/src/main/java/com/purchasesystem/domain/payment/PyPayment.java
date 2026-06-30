package com.purchasesystem.domain.payment;

import lombok.Data;
import java.math.BigDecimal;

/** 지급 (PY_PAYMENT). */
@Data
public class PyPayment {
    private Long id;
    private String compCd;
    private String payNo;
    private String srcTyp;
    private Long srcId;
    private String srcNo;
    private String vdCd;
    private String vdNm;
    private String payDueYmd;
    private BigDecimal payAmt;
    private String payMethodCd;
    private String payMethodNm;
    private String bankCd;
    private String bankNm;
    private String acctNo;
    private String acctHolder;
    private String noteNo;
    private String noteDueYmd;
    private String payYmd;
    private String paySts;
    private String payStsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;
}
