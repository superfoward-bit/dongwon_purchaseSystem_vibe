package com.purchasesystem.domain.vendor;

import lombok.Data;

/** 협력사 면허/인증 (VD_LICENSE). */
@Data
public class VdLicense {
    private Long id;
    private Long vendorId;
    private Integer lineNo;
    private String licTyp;
    private String licTypNm;
    private String licNm;
    private String licNo;
    private String issueOrg;
    private String issueYmd;
    private String expireYmd;
    private String remark;
}
