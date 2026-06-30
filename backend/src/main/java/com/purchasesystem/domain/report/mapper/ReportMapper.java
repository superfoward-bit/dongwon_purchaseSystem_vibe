package com.purchasesystem.domain.report.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ReportMapper {
    /** 월별 입고실적 */
    List<Map<String, Object>> grMonthly(@Param("year") String year);
    /** 협력사별 입고실적 */
    List<Map<String, Object>> grByVendor(@Param("year") String year);
    /** 품목별 입고실적 */
    List<Map<String, Object>> grByItem(@Param("year") String year);
    /** 월별 발주실적 */
    List<Map<String, Object>> poMonthly(@Param("year") String year);
    /** 단가변동 이력(최근) */
    List<Map<String, Object>> priceHistory(@Param("vdCd") String vdCd, @Param("itemCd") String itemCd);
    /** 정산 조정/장려금 집계 (유형별) */
    List<Map<String, Object>> settleAdj(@Param("closeYm") String closeYm);
    /** 요약 카드(연간 입고/발주 총액, 건수) */
    Map<String, Object> summary(@Param("year") String year);
}
