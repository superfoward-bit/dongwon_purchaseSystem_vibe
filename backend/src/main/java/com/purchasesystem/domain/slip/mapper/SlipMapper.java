package com.purchasesystem.domain.slip.mapper;

import com.purchasesystem.domain.slip.AcSlip;
import com.purchasesystem.domain.slip.AcSlipLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SlipMapper {
    List<AcSlip> findList(@Param("keyword") String keyword, @Param("slipSts") String slipSts);
    AcSlip findById(@Param("id") Long id);
    void insert(AcSlip s);
    void updateStatus(@Param("id") Long id, @Param("slipSts") String slipSts, @Param("modId") String modId);
    void updateSent(@Param("id") Long id, @Param("erpDocNo") String erpDocNo, @Param("modId") String modId);
    void delete(@Param("id") Long id, @Param("modId") String modId);
    int countBySrc(@Param("srcTyp") String srcTyp, @Param("srcId") Long srcId);
    // 전표 명세(차/대변)
    List<AcSlipLine> findLines(@Param("slipId") Long slipId);
    void insertLine(AcSlipLine l);
}
