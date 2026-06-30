package com.purchasesystem.domain.rx.eval;

import lombok.Data;
import java.math.BigDecimal;

/** RFP 평가 점수 (위원×협력사×항목) */
@Data
public class RxEvalScore {
    private Long id;
    private Long rfxId;
    private String vdCd;
    private Long criteriaId;
    private Long evaluatorId;
    private BigDecimal score;    // 0~100
}
