package com.purchasesystem.domain.claim;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 협력사 클레임 (CM_CLAIM). */
@Data
public class CmClaim {
    private Long id;
    private String compCd;
    private String clmNo;
    private String vdCd;
    private String vdNm;
    private String clmTyp;
    private String clmTypNm;
    private String srcTyp;
    private String refNo;
    private String itemCd;
    private String itemNm;
    private String clmYmd;
    private String lotNo;
    private String mfgYmd;
    private String compTyp;
    private String compTypNm;
    private BigDecimal claimQty;
    private BigDecimal clmAmt;
    private BigDecimal recoverAmt;
    private String clmDesc;
    private String dueYmd;
    private String resolveDesc;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
