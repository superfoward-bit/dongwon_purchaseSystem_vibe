package com.purchasesystem.domain.rx.eval;

import lombok.Data;
import java.math.BigDecimal;

/** RFP 비가격 평가항목 */
@Data
public class RxEvalCriteria {
    private Long id;
    private Long rfxId;
    private Integer lineNo;
    private String criteriaNm;
    private BigDecimal weight;   // 배점
    private String remark;
}
