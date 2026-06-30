package com.purchasesystem.domain.segment;

import lombok.Data;

/** 협력사 세그먼트 (VD_SEGMENT). */
@Data
public class VdSegment {
    private Long id;
    private String compCd;
    private String segCd;
    private String segNm;
    private String description;
    private String defaultSheetCd;
    private String defaultSheetNm;
    private Integer sortNo;
    private String useYn;
    private Integer vendorCnt;   // 소속 협력사 수(표시용)
    private String regId;
}
