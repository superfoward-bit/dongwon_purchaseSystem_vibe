package com.purchasesystem.domain.stock.mapper;

import com.purchasesystem.domain.stock.StCount;
import com.purchasesystem.domain.stock.StCountItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CountMapper {
    List<StCount> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    StCount findById(@Param("id") Long id);
    List<StCountItem> findItems(@Param("countId") Long countId);
    void insert(StCount c);
    void updateHeader(StCount c);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void deleteItems(@Param("countId") Long countId);
    void insertItem(StCountItem it);
}
