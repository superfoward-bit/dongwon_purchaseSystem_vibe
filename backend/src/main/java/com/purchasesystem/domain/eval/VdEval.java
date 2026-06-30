package com.purchasesystem.domain.eval;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 협력사 평가 실행 (VD_EVAL). */
@Data
public class VdEval {
    private Long id;
    private String compCd;
    private String evalNo;
    private String evalTyp;
    private String evalTypNm;
    private String sheetCd;
    private String sheetNm;
    private String vdCd;
    private String vdNm;
    private String segCd;
    private String segNm;
    private String useCateYn;
    private String evalPeriod;
    private String evaluatorId;
    private BigDecimal totScore;
    private String gradeCd;
    private String gradeNm;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<VdEvalResult> results = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
