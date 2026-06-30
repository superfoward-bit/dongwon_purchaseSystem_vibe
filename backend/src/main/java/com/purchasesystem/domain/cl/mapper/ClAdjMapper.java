package com.purchasesystem.domain.cl.mapper;

import com.purchasesystem.domain.cl.ClAdjRate;
import com.purchasesystem.domain.cl.ClCloseAdj;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ClAdjMapper {

    // ----- 조정율 마스터 -----
    List<ClAdjRate> findRateList(@Param("vdCd") String vdCd, @Param("adjTyp") String adjTyp);
    List<ClAdjRate> findActiveRates(@Param("compCd") String compCd, @Param("vdCd") String vdCd, @Param("ymd") String ymd);
    ClAdjRate findRateById(@Param("id") Long id);
    void insertRate(ClAdjRate r);
    void updateRate(ClAdjRate r);
    void deleteRate(@Param("id") Long id, @Param("modId") String modId);

    // ----- 정산 조정명세 -----
    List<ClCloseAdj> findAdjs(@Param("closeId") Long closeId);
    void insertAdj(ClCloseAdj a);
    void deleteAdjs(@Param("closeId") Long closeId);
    void updateCloseAmounts(@Param("closeId") Long closeId, @Param("adjAmt") BigDecimal adjAmt,
                            @Param("netAmt") BigDecimal netAmt, @Param("modId") String modId);
}
