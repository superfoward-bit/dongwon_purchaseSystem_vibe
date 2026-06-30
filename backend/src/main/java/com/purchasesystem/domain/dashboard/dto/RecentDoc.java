package com.purchasesystem.domain.dashboard.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 최근 문서. */
@Data
public class RecentDoc {
    private Long id;
    private String docNo;
    private String title;
    private String vdNm;
    private BigDecimal amt;
    private String stsNm;
    private String regDt;
}
