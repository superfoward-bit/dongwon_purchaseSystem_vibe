package com.purchasesystem.domain.cl;

import lombok.Data;

import java.math.BigDecimal;

/** 마감대상 입고 (CL_CLOSE_GR). */
@Data
public class ClCloseGr {
    private Long id;
    private Long closeId;
    private Long grId;
    private String grNo;
    private String grYmd;
    private String poNo;
    private String vdNm;
    private BigDecimal suplAmt;
    private BigDecimal vatAmt;
    private BigDecimal amt;
}
