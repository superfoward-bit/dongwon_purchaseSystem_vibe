package com.purchasesystem.domain.auction.mapper;

import com.purchasesystem.domain.auction.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AuctionMapper {

    List<AuAuction> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    List<AuAuction> findByVendor(@Param("vdCd") String vdCd);
    AuAuction findById(@Param("id") Long id);
    List<AuItem> findItems(@Param("auctionId") Long auctionId);
    List<AuVendor> findVendors(@Param("auctionId") Long auctionId);
    List<AuBid> findBids(@Param("auctionId") Long auctionId);

    int insertAuction(AuAuction a);
    int updateAuction(AuAuction a);
    int updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    int updateAward(@Param("id") Long id, @Param("vdCd") String vdCd, @Param("vdNm") String vdNm,
                    @Param("awardAmt") BigDecimal awardAmt, @Param("modId") String modId);
    int deleteAuction(@Param("id") Long id, @Param("modId") String modId);

    int insertItem(AuItem it);
    int deleteItems(@Param("auctionId") Long auctionId);
    int insertVendor(AuVendor v);
    int deleteVendors(@Param("auctionId") Long auctionId);

    // 입찰
    AuVendor findVendor(@Param("auctionId") Long auctionId, @Param("vdCd") String vdCd);
    int insertBid(AuBid b);
    int updateVendorBid(@Param("auctionId") Long auctionId, @Param("vdCd") String vdCd, @Param("bidAmt") BigDecimal bidAmt);
    int clearAwardYn(@Param("auctionId") Long auctionId);
    int setAwardYn(@Param("auctionId") Long auctionId, @Param("vdCd") String vdCd);

    /** 자동연장 시도 (1=연장됨) */
    int tryAutoExtend(@Param("id") Long id);
}
