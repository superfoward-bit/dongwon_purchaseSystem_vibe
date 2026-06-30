package com.purchasesystem.domain.quality;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 입고검사 헤더 (QL_INSPECTION). */
@Data
public class QlInspection {
    private Long id;
    private String compCd;
    private String inspNo;
    private Long grId;
    private String grNo;
    private String poNo;
    private String vdCd;
    private String vdNm;
    private String operOrgCd;
    private String inspTyp;
    private String qmLotNo;
    private String lotNo;
    private String mfgYmd;
    private String expYmd;
    private BigDecimal inspQty;
    private String inspResult;
    private String inspResultNm;
    private String inspYmd;
    private String inspUsrId;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<QlResult> results = new ArrayList<>();
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
}
