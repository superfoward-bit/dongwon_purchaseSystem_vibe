package com.purchasesystem.domain.av;

import lombok.Data;
import java.math.BigDecimal;

/** 선급금 반제 내역 (AV_REPAY). */
@Data
public class AvRepay {
    private Long id;
    private Long advanceId;
    private Integer lineNo;
    private BigDecimal repayAmt;
    private String repayYmd;
    private String closeNo;
    private String remark;
}
