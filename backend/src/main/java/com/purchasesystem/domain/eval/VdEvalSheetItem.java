package com.purchasesystem.domain.eval;

import lombok.Data;

import java.math.BigDecimal;

/** 평가항목 (VD_EVAL_SHEET_ITEM). */
@Data
public class VdEvalSheetItem {
    private Long id;
    private String sheetCd;
    private Integer itemSeq;
    private String evalItemNm;
    private String evalCls;
    private BigDecimal weight;
    private Integer cateSeq;
    private String cateNm;
}
