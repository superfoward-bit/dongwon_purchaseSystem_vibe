package com.purchasesystem.domain.ip;

import lombok.Data;
import java.math.BigDecimal;

/** 단가 변경예약 명세 */
@Data
public class IpRsvLine {
    private Long id;
    private Long rsvId;
    private Integer lineNo;
    private String itemCd;
    private String itemNm;
    private String unitCd;
    private String applySd;
    private String applyEd;
    private BigDecimal unitPrc;
    private String remark;
}
