package com.purchasesystem.domain.vendor;

import lombok.Data;

/** 협력사 담당자 (VD_CONTACT). */
@Data
public class VdContact {
    private Long id;
    private Long vendorId;
    private Integer lineNo;
    private String contactNm;
    private String position;
    private String dept;
    private String tel;
    private String mobile;
    private String email;
    private String repYn;
    private String remark;
}
