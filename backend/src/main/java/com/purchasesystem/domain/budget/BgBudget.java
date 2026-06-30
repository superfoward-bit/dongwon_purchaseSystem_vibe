package com.purchasesystem.domain.budget;

import lombok.Data;
import java.math.BigDecimal;

/** 예산 마스터 (BG_BUDGET). */
@Data
public class BgBudget {
    private Long id;
    private String compCd;
    private String budgetCd;
    private String budgetNm;
    private String fiscalYear;
    private String deptCd;
    private String acctCd;
    private BigDecimal budgetAmt;
    private BigDecimal usedAmt;       // 집행(약정)액 — 동적 산출
    private BigDecimal availableAmt;  // 잔액 — 동적 산출
    private String useYn;
    private String regId;
    private String modId;
}
