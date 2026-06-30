package com.purchasesystem.domain.ip;

import lombok.Data;
import java.math.BigDecimal;

/** 단가 구간분할 변경이력 */
@Data
public class IpPriceHis {
    private Long id;
    private String compCd;
    private String vdCd;
    private String itemCd;
    private String chgTyp;      // PULL/INSERT/RESTORE/TAIL/DELETE
    private String chgTypNm;
    private String oldSd;
    private String oldEd;
    private BigDecimal oldPrc;
    private String newSd;
    private String newEd;
    private BigDecimal newPrc;
    private String srcTyp;
    private Long srcId;
    private String regId;
    private String regDt;
}
