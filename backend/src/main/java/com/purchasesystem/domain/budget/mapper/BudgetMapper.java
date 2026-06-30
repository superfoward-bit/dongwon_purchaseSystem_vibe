package com.purchasesystem.domain.budget.mapper;

import com.purchasesystem.domain.budget.BgBudget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BudgetMapper {
    List<BgBudget> findList(@Param("keyword") String keyword, @Param("fiscalYear") String fiscalYear);
    BgBudget findById(@Param("id") Long id);
    BgBudget findByCode(@Param("compCd") String compCd, @Param("budgetCd") String budgetCd);
    /** 약정(집행)액 = 상신·승인된 PR 합계. excludePrId 제외(자기 자신 재검증용) */
    BigDecimal sumCommitted(@Param("budgetCd") String budgetCd, @Param("excludePrId") Long excludePrId);
    void insert(BgBudget b);
    void update(BgBudget b);
    void delete(@Param("id") Long id, @Param("modId") String modId);
}
