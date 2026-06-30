package com.purchasesystem.domain.eval;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 평가시트 (VD_EVAL_SHEET). */
@Data
public class VdEvalSheet {
    private Long id;
    private String sheetCd;
    private String sheetNm;
    private String evalTyp;
    private String description;
    private String useCateYn;
    private String useYn;
    private String regId;
    private String modId;
    private List<VdEvalSheetItem> items = new ArrayList<>();
    private List<VdEvalCate> categories = new ArrayList<>();
}
