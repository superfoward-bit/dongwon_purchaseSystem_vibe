package com.purchasesystem.common.status;

import lombok.Data;

/** 상태전이 규칙 (CM_STATUS_FLOW). */
@Data
public class CmStatusFlow {
    private String docTyp;
    private String fromSts;
    private String action;
    private String toSts;
    private String actionNm;
    private String direction;   // FWD/BWD/END
    private String rsnReqYn;
    private Integer sortNo;
}
