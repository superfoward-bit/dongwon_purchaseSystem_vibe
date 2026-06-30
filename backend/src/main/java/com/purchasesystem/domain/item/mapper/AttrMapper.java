package com.purchasesystem.domain.item.mapper;

import com.purchasesystem.domain.item.ItCateAttr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttrMapper {
    // 분류별 속성 정의
    List<ItCateAttr> findCateAttrs(@Param("cateCd") String cateCd);
    void insertCateAttr(ItCateAttr a);
    void deleteCateAttrs(@Param("cateCd") String cateCd);

    // 품목 속성값
    List<ItCateAttr> findItemAttrs(@Param("itemId") Long itemId);
    void insertItemAttr(@Param("itemId") Long itemId, @Param("attrNm") String attrNm, @Param("attrVal") String attrVal);
    void deleteItemAttrs(@Param("itemId") Long itemId);
}
