package com.purchasesystem.domain.pp;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 구매계획 헤더 (PP_PLAN). */
@Data
public class PpPlan {
    private Long id;
    private String compCd;
    private String planNo;
    private String planYear;
    private String planNm;
    private String purcGrpCd;
    private BigDecimal totPlanAmt;
    private BigDecimal totActualAmt;   // 실적합(표시용)
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<PpPlanItem> items = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
