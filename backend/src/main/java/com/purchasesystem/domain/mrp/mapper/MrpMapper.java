package com.purchasesystem.domain.mrp.mapper;

import com.purchasesystem.domain.mrp.MrSuggest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MrpMapper {
    /** 발주정책이 설정된 품목 + 현재고 + 입고예정(미입고 발주잔량) */
    List<MrSuggest> findReorderItems(@Param("compCd") String compCd);
    void clearSuggests(@Param("compCd") String compCd);
    void insertSuggest(MrSuggest s);
    List<MrSuggest> findSuggests(@Param("sts") String sts);
    MrSuggest findById(@Param("id") Long id);
    void updateOrdered(@Param("id") Long id, @Param("prNo") String prNo, @Param("modId") String modId);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
}
