package com.purchasesystem.domain.stock.mapper;

import com.purchasesystem.domain.stock.StLedger;
import com.purchasesystem.domain.stock.StStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface StockMapper {
    // 재고 현황
    List<StStock> findStockList(@Param("keyword") String keyword, @Param("whCd") String whCd, @Param("zeroYn") String zeroYn);
    StStock findStockRow(@Param("compCd") String compCd, @Param("whCd") String whCd,
                         @Param("itemCd") String itemCd, @Param("lotNo") String lotNo);
    List<StStock> findStockByItemWh(@Param("whCd") String whCd, @Param("itemCd") String itemCd);
    void insertStock(StStock s);
    void updateStockQty(@Param("id") Long id, @Param("qty") BigDecimal qty,
                        @Param("inYmd") String inYmd, @Param("outYmd") String outYmd, @Param("modId") String modId);
    // 수불부
    List<StLedger> findLedger(@Param("keyword") String keyword, @Param("itemCd") String itemCd,
                              @Param("whCd") String whCd, @Param("transTyp") String transTyp);
    void insertLedger(StLedger l);
}
