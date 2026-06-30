package com.purchasesystem.domain.rx.eval;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/** RFP 평가입찰 DTO 모음 */
public class RxEvalDtos {

    /** 평가 설정(헤더 + 항목 + 위원) */
    @Data
    public static class Setup {
        private String evalYn;
        private BigDecimal priceWeight;
        private String priceMethod;     // MR / LN
        private List<RxEvalCriteria> criteria;
        private List<RxEvaluator> evaluators;
    }

    /** 협력사 견적총액 */
    @Data
    public static class VendorQuote {
        private String vdCd;
        private String vdNm;
        private BigDecimal quoteAmt;
    }

    /** 평가 결과(협력사별 종합점수) */
    @Data
    public static class Result {
        private String vdCd;
        private String vdNm;
        private BigDecimal quoteAmt;
        private BigDecimal nonPriceScore;
        private BigDecimal priceScore;
        private BigDecimal totalScore;
        private Integer rankNo;
    }
}
