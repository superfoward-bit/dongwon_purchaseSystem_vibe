package com.purchasesystem.domain.approval;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/** 결재선 템플릿 (금액조건별) */
@Data
public class AppLineTpl {
    private Long id;
    private String compCd;
    private String docTyp;
    private String tplNm;
    private BigDecimal amtFrom;
    private BigDecimal amtTo;
    private Integer sortNo;
    private String useYn;
    private String regId;
    private List<AppLineTplStep> steps;
}
