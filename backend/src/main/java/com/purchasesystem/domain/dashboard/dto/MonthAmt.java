package com.purchasesystem.domain.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 월별 금액 집계. */
@Data
public class MonthAmt {
    private String ym;
    private BigDecimal amt;
}
