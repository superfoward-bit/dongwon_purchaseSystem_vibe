package com.purchasesystem.domain.stock;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/** 재고 출고 (ST_ISSUE). */
@Data
public class StIssue {
    private Long id;
    private String compCd;
    private String issueNo;
    private String issueTyp;
    private String issueTypNm;
    private String whCd;
    private String whNm;
    private String reqDept;
    private String issueYmd;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<StIssueItem> items = new ArrayList<>();
}
