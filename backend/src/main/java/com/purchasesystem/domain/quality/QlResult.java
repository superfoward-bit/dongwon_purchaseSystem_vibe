package com.purchasesystem.domain.quality;

import lombok.Data;
import java.math.BigDecimal;

/** 입고검사 항목결과 (QL_INSPECTION_RESULT). */
@Data
public class QlResult {
    private Long id;
    private Long inspId;
    private Integer itemSeq;
    private String inspItemNm;
    private String itemTyp;        // 시험분류
    private String itemTypNm;
    private String specVal;        // 규격(텍스트 표기)
    private BigDecimal specLower;  // 규격 하한
    private BigDecimal specUpper;  // 규격 상한
    private String measureVal;     // 측정값(텍스트)
    private BigDecimal measureNum; // 측정값(수치, 자동판정)
    private String testMethod;
    private String unit;
    private String passYn;
    private String qmCode;
    private String remark;
}
