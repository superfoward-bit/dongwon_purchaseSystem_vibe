package com.purchasesystem.domain.pp.mapper;

import com.purchasesystem.domain.pp.PpPlan;
import com.purchasesystem.domain.pp.PpPlanItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PpMapper {
    List<PpPlan> findList(@Param("keyword") String keyword, @Param("sts") String sts, @Param("planYear") String planYear);
    PpPlan findById(@Param("id") Long id);
    /** 품목 + 입고실적(계획연도) */
    List<PpPlanItem> findItems(@Param("planId") Long planId, @Param("year") String year);
    void insertPlan(PpPlan p);
    void updatePlan(PpPlan p);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void deletePlan(@Param("id") Long id, @Param("modId") String modId);
    void insertItem(PpPlanItem it);
    void deleteItems(@Param("planId") Long planId);
}
