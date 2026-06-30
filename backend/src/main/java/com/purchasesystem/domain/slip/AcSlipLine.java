package com.purchasesystem.domain.slip;

import lombok.Data;
import java.math.BigDecimal;

/** 회계전표 명세 (AC_SLIP_LINE) - 차/대변 복식부기. */
@Data
public class AcSlipLine {
    private Long id;
    private Long slipId;
    private Integer lineNo;
    private String drCr;       // D차변 / C대변
    private String acctCd;
    private String acctNm;
    private BigDecimal amt;
    private String remark;
    private String regId;
}
