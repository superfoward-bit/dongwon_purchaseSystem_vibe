package com.purchasesystem.domain.item.mapper;

import com.purchasesystem.domain.item.ItCategory;
import com.purchasesystem.domain.item.ItItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {

    // ----- 분류 -----
    List<ItCategory> findCategories();
    int countByCateCd(@Param("cateCd") String cateCd);
    int insertCategory(ItCategory c);
    int updateCategory(ItCategory c);
    int deleteCategory(@Param("id") Long id, @Param("modId") String modId);
    int countChildCate(@Param("cateCd") String cateCd);
    int countItemByCate(@Param("cateCd") String cateCd);

    // ----- 품목 -----
    List<ItItem> findList(@Param("keyword") String keyword, @Param("cateCd") String cateCd,
                          @Param("itemSts") String itemSts);
    ItItem findById(@Param("id") Long id);
    int insertItem(ItItem it);
    int updateItem(ItItem it);
    int deleteItem(@Param("id") Long id, @Param("modId") String modId);
}
