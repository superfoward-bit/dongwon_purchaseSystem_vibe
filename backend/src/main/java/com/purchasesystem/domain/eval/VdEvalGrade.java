package com.purchasesystem.domain.eval;

import lombok.Data;

import java.math.BigDecimal;

/** 평가등급 (VD_EVAL_GRADE). */
@Data
public class VdEvalGrade {
    private Long id;
    private String gradeCd;
    private String gradeNm;
    private BigDecimal minScore;
    private BigDecimal maxScore;
    private String actionDesc;
    private Integer sortNo;
    private String segCd;
}
