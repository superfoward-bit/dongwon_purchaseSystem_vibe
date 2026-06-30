package com.purchasesystem.domain.dashboard.mapper;

import com.purchasesystem.domain.dashboard.dto.MonthAmt;
import com.purchasesystem.domain.dashboard.dto.RecentDoc;
import com.purchasesystem.domain.dashboard.dto.StatCount;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface DashboardMapper {

    int countPrOpen();
    int countPoDone();
    int countGrConfirmed();
    int countRfxOpen();
    BigDecimal poAmtThisMonth();

    List<StatCount> prByStatus();
    List<StatCount> poByStatus();
    List<StatCount> grByStatus();
    List<StatCount> vendorByGrade();

    List<MonthAmt> poMonthly();

    List<RecentDoc> recentPo();
    List<RecentDoc> recentGr();
}
