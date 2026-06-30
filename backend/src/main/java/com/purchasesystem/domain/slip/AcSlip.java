package com.purchasesystem.domain.slip;

import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 회계전표 (AC_SLIP). */
@Data
public class AcSlip {
    private Long id;
    private String compCd;
    private String slipNo;
    private String slipTyp;
    private String slipTypNm;
    private String srcTyp;
    private Long srcId;
    private String srcNo;
    private String vdCd;
    private String vdNm;
    private String postingYmd;
    private String drAcct;
    private String crAcct;
    private BigDecimal amt;
    private String slipSts;
    private String slipStsNm;
    private String fiscalYm;
    private String erpDocNo;
    private String remark;
    private String regId;
    private String regDt;

    private List<AcSlipLine> lines = new ArrayList<>();
}
