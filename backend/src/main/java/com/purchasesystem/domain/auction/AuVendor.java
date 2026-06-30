package com.purchasesystem.domain.auction;

import lombok.Data;

import java.math.BigDecimal;

/** 역경매 참여협력사 (AU_AUCTION_VENDOR). */
@Data
public class AuVendor {
    private Long id;
    private Long auctionId;
    private String vdCd;
    private String vdNm;
    private String joinYn;
    private String awardYn;
    private BigDecimal lastBidAmt;
    private Integer bidCnt;
    private Integer rankNo;   // 전송용: 현재 순위
}
