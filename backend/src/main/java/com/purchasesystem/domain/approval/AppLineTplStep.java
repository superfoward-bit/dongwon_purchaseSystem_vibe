package com.purchasesystem.domain.approval;

import lombok.Data;

/** 결재선 템플릿 단계 */
@Data
public class AppLineTplStep {
    private Long id;
    private Long tplId;
    private Integer stepNo;
    private String lineTyp;
    private String lineTypNm;
    private String aprvUsrId;
    private String aprvUsrNm;
    private String finalYn;
}
