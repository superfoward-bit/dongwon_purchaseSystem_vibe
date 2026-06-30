package com.purchasesystem.domain.rx.eval.mapper;

import com.purchasesystem.domain.rx.eval.RxEvalCriteria;
import com.purchasesystem.domain.rx.eval.RxEvalScore;
import com.purchasesystem.domain.rx.eval.RxEvaluator;
import com.purchasesystem.domain.rx.eval.RxEvalDtos;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface RxEvalMapper {

    Map<String, Object> findEvalCfg(@Param("rfxId") Long rfxId);
    void updateEvalCfg(@Param("rfxId") Long rfxId, @Param("evalYn") String evalYn,
                       @Param("priceWeight") BigDecimal priceWeight, @Param("priceMethod") String priceMethod);

    List<RxEvalCriteria> findCriteria(@Param("rfxId") Long rfxId);
    void deleteCriteria(@Param("rfxId") Long rfxId);
    void insertCriteria(RxEvalCriteria c);

    List<RxEvaluator> findEvaluators(@Param("rfxId") Long rfxId);
    void deleteEvaluators(@Param("rfxId") Long rfxId);
    void insertEvaluator(RxEvaluator e);

    List<RxEvalScore> findScores(@Param("rfxId") Long rfxId);
    void deleteScores(@Param("rfxId") Long rfxId);
    void insertScore(RxEvalScore s);

    /** 협력사별 견적총액 (RX_QUOTE_ITEM 합계) */
    List<RxEvalDtos.VendorQuote> findVendorQuotes(@Param("rfxId") Long rfxId);
}
