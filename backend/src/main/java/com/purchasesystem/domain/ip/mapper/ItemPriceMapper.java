package com.purchasesystem.domain.ip.mapper;

import com.purchasesystem.domain.ip.IpPrice;
import com.purchasesystem.domain.ip.IpPriceHis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemPriceMapper {

    /** 확정단가 구간 목록 (조건 검색) */
    List<IpPrice> findList(@Param("vdCd") String vdCd, @Param("itemCd") String itemCd, @Param("keyword") String keyword);

    /** 특정 기간과 겹치는 확정구간 (분할용) */
    List<IpPrice> findOverlapping(@Param("compCd") String compCd, @Param("vdCd") String vdCd,
                                  @Param("itemCd") String itemCd, @Param("sd") String sd, @Param("ed") String ed);

    /** 유효일자 기준 단가 1건 (단가 자동적용) */
    IpPrice findEffectivePrice(@Param("compCd") String compCd, @Param("vdCd") String vdCd,
                               @Param("itemCd") String itemCd, @Param("ymd") String ymd);

    void insertPrice(IpPrice p);

    void updateApplyEd(@Param("id") Long id, @Param("applyEd") String applyEd, @Param("modId") String modId);

    void updateApplySd(@Param("id") Long id, @Param("applySd") String applySd, @Param("modId") String modId);

    void softDelete(@Param("id") Long id, @Param("modId") String modId);

    void insertHis(IpPriceHis h);

    List<IpPriceHis> findHis(@Param("vdCd") String vdCd, @Param("itemCd") String itemCd);
}
