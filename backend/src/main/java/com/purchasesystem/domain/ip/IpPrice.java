package com.purchasesystem.domain.ip;

import lombok.Data;
import java.math.BigDecimal;

/** 품목 확정단가 구간 */
@Data
public class IpPrice {
    private Long id;
    private String compCd;
    private String vdCd;
    private String vdNm;
    private String itemCd;
    private String itemNm;
    private String unitCd;
    private String currCd;
    private String applySd;
    private String applyEd;
    private BigDecimal unitPrc;
    private String srcTyp;
    private Long srcId;
    private String remark;
    private String regId;
    private String regDt;
}
