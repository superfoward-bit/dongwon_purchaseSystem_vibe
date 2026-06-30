package com.purchasesystem.domain.eval;

import lombok.Data;
import java.math.BigDecimal;

/** 평가시트 카테고리(팩터그룹) (VD_EVAL_SHEET_CATE). */
@Data
public class VdEvalCate {
    private Long id;
    private String sheetCd;
    private Integer cateSeq;
    private String cateNm;
    private BigDecimal weight;   // 카테고리 배점(합100)
}
