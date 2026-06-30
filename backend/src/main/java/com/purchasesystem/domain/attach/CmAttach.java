package com.purchasesystem.domain.attach;

import lombok.Data;

/** 공통 첨부파일 (CM_ATTACH). */
@Data
public class CmAttach {
    private Long id;
    private String compCd;
    private String refTyp;
    private Long refId;
    private String fileNm;
    private String storNm;
    private String filePath;
    private Long fileSize;
    private String contentTyp;
    private String remark;
    private String regId;
    private String regDt;
}
