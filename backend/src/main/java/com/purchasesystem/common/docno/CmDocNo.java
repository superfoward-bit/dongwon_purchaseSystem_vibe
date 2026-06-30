package com.purchasesystem.common.docno;

import lombok.Data;

/** 문서번호 채번 규칙 (CM_DOC_NO). */
@Data
public class CmDocNo {
    private Long id;
    private String docTyp;
    private String prefix;
    private String dateFmt;
    private Integer seqLen;
    private String lastYmd;
    private Long lastSeq;
}
