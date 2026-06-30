package com.purchasesystem.domain.rx.eval;

import lombok.Data;

/** RFP 평가위원 */
@Data
public class RxEvaluator {
    private Long id;
    private Long rfxId;
    private String usrId;
    private String usrNm;
}
