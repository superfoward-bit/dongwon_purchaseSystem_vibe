package com.purchasesystem.domain.pa.mapper;

import com.purchasesystem.domain.pa.PaAdjust;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface PaMapper {
    List<PaAdjust> findList(@Param("keyword") String keyword, @Param("sts") String sts, @Param("vdCd") String vdCd);
    PaAdjust findById(@Param("id") Long id);
    void insert(PaAdjust a);
    void update(PaAdjust a);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void delete(@Param("id") Long id, @Param("modId") String modId);

    /** 확정된 매입조정 합계 (정산 연계: 협력사+정산월) */
    BigDecimal sumConfirmed(@Param("compCd") String compCd, @Param("vdCd") String vdCd, @Param("adjYm") String adjYm);
}
