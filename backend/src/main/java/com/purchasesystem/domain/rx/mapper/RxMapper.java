package com.purchasesystem.domain.rx.mapper;

import com.purchasesystem.domain.rx.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RxMapper {

    List<RxRfx> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    List<RxRfx> findByVendor(@Param("vdCd") String vdCd);
    RxRfx findById(@Param("id") Long id);
    List<RxItem> findItems(@Param("rfxId") Long rfxId);
    List<RxVendor> findVendors(@Param("rfxId") Long rfxId);
    List<RxQuoteItem> findQuoteItems(@Param("rfxId") Long rfxId);

    int insertRfx(RxRfx r);
    int updateRfx(RxRfx r);
    int updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    int updateSelectedVendor(@Param("id") Long id, @Param("vdCd") String vdCd, @Param("vdNm") String vdNm,
                             @Param("modId") String modId);
    int deleteRfx(@Param("id") Long id, @Param("modId") String modId);

    int insertItem(RxItem it);
    int deleteItems(@Param("rfxId") Long rfxId);
    int insertVendor(RxVendor v);
    int deleteVendors(@Param("rfxId") Long rfxId);

    // 견적 응답
    int deleteQuoteItems(@Param("rfxId") Long rfxId, @Param("vdCd") String vdCd);
    int insertQuoteItem(RxQuoteItem q);
    int updateVendorResp(@Param("rfxId") Long rfxId, @Param("vdCd") String vdCd,
                         @Param("totAmt") BigDecimal totAmt);
    int clearVendorSel(@Param("rfxId") Long rfxId);
    int setVendorSel(@Param("rfxId") Long rfxId, @Param("vdCd") String vdCd);
}
