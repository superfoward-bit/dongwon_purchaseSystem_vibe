package com.purchasesystem.domain.price;

import lombok.Data;
import java.math.BigDecimal;

/** 단가 자동적용 조회 결과 */
@Data
public class PriceLookup {
    private String vdCd;
    private String itemCd;
    private String ymd;
    private BigDecimal unitPrc;
    private String srcTyp;     // CT(단가계약) / IP(품목단가) / NONE
    private String srcTypNm;   // 계약단가 / 품목단가 / 없음
    private String applySd;
    private String applyEd;
}
