package com.purchasesystem.domain.auction;

import lombok.Data;

import java.math.BigDecimal;

/** 역경매 입찰내역 (AU_BID). */
@Data
public class AuBid {
    private Long id;
    private Long auctionId;
    private String vdCd;
    private String vdNm;
    private BigDecimal bidAmt;
    private String bidDt;
    private Integer rankNo;
}
