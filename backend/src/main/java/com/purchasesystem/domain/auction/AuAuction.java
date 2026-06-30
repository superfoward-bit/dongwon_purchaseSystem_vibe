package com.purchasesystem.domain.auction;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 역경매 헤더 (AU_AUCTION). */
@Data
public class AuAuction {
    private Long id;
    private String compCd;
    private String auctionNo;
    private String auctionTitle;
    private String purcGrpCd;
    private String chrgUsrId;
    private String startYmd;
    private String endYmd;
    private BigDecimal startPrc;
    private BigDecimal minDownPrc;
    private String currCd;
    private String srcTyp;
    private Long srcId;
    private String awardVdCd;
    private String awardVdNm;
    private BigDecimal awardAmt;
    private String sts;
    private String stsNm;
    private String remark;
    // 자동연장(스나이핑 방지)
    private String autoExtYn;
    private String endDt;
    private Integer extTriggerMin;
    private Integer extMin;
    private Integer maxExtCnt;
    private Integer extCnt;
    private String regId;
    private String regDt;
    private String modId;

    private List<AuItem> items = new ArrayList<>();
    private List<AuVendor> vendors = new ArrayList<>();
    private List<AuBid> bids = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
