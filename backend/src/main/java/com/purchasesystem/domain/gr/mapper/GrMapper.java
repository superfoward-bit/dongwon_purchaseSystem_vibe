package com.purchasesystem.domain.gr.mapper;

import com.purchasesystem.domain.gr.GrItem;
import com.purchasesystem.domain.gr.GrReceipt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GrMapper {

    List<GrReceipt> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    GrReceipt findById(@Param("id") Long id);
    List<GrItem> findItems(@Param("receiptId") Long receiptId);

    int insertReceipt(GrReceipt gr);
    int updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    int deleteReceipt(@Param("id") Long id, @Param("modId") String modId);
    int insertItem(GrItem item);
    int deleteItems(@Param("receiptId") Long receiptId);

    // ----- 발주(PO) 연계 -----
    List<GrReceipt> findConfirmedPos(@Param("keyword") String keyword);
    GrReceipt findPoHeader(@Param("poId") Long poId);
    List<GrItem> findPoItemsForGr(@Param("poId") Long poId);

    /** 발주품목 입고누계 가감 (입고확정 +, 입고취소 -) */
    int addPoItemGrQty(@Param("poItemId") Long poItemId, @Param("qty") java.math.BigDecimal qty);
}
