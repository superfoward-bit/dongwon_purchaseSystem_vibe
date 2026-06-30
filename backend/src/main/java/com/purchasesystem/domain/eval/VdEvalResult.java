package com.purchasesystem.domain.eval;

import lombok.Data;

import java.math.BigDecimal;

/** 평가 항목별 점수 (VD_EVAL_RESULT). */
@Data
public class VdEvalResult {
    private Long id;
    private Long evalId;
    private Integer sheetItemSeq;
    private String evalItemNm;
    private Integer cateSeq;
    private String cateNm;
    private BigDecimal weight;
    private BigDecimal score;
    private BigDecimal weightedScore;
    private String opinion;
}
