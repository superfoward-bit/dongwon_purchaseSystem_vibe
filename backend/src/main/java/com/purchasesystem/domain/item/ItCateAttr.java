package com.purchasesystem.domain.item;

import lombok.Data;

/** 분류별 속성 정의 (IT_CATE_ATTR) / 품목·요청 속성값 공용 표현. */
@Data
public class ItCateAttr {
    private Long id;
    private String cateCd;
    private Integer attrSeq;
    private String attrNm;
    private String attrTyp;
    private String attrTypNm;
    private String optionVals;
    private String requiredYn;
    private Integer sortNo;
    private String useYn;
    // 값(품목/요청 속성값 표현용)
    private String attrVal;
}
