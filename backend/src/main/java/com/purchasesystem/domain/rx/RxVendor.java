package com.purchasesystem.domain.rx;

import lombok.Data;

import java.math.BigDecimal;

/** 견적 초대협력사/응답요약 (RX_RFX_VENDOR). */
@Data
public class RxVendor {
    private Long id;
    private Long rfxId;
    private String vdCd;
    private String vdNm;
    private String respYn;
    private String selYn;
    private BigDecimal quoteTotAmt;
    private String quoteYmd;
}
