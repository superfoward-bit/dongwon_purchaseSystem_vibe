package com.purchasesystem.domain.bc;

import lombok.Data;

/** 표준거래계약 거래조건 조항 (BC_CONTRACT_TERM). */
@Data
public class BcTerm {
    private Long id;
    private Long contractId;
    private Integer lineNo;
    private String termNm;
    private String termContent;
    private String remark;
}
