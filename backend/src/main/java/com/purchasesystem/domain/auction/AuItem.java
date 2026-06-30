package com.purchasesystem.domain.auction;

import lombok.Data;

import java.math.BigDecimal;

/** 역경매 품목 (AU_AUCTION_ITEM). */
@Data
public class AuItem {
    private Long id;
    private Long auctionId;
    private Integer lineNo;
    private Long prItemId;
    private String itemCd;
    private String itemNm;
    private String spec;
    private String unitCd;
    private BigDecimal qty;
    private BigDecimal startPrc;
}
