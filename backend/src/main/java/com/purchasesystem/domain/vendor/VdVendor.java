package com.purchasesystem.domain.vendor;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** 협력사 (VD_VENDOR). */
@Data
public class VdVendor {
    private Long id;
    private String compCd;
    private String vdCd;
    private String erpVdCd;
    private String vdNm;
    private String vdNmEn;
    private String bizNo;
    private String corpNo;
    private String ceoNm;
    private String vdTyp;
    private String vdTypNm;
    private String bizCond;
    private String bizItem;
    private String addr;
    private String tel;
    private String fax;
    private String email;
    private String payCond;
    private String payTermCd;
    private String payTermNm;
    private String payMethodCd;
    private String payMethodNm;
    private String vdSts;
    private String vdStsNm;
    private String gradeCd;
    private String gradeNm;
    private String lastEvalYmd;
    private String segCd;
    private String segNm;
    // 확장 상세정보
    private String zipCd;
    private String addrEn;
    private String mobile;
    private String homepage;
    private String indSectCd;
    private String indSectNm;
    private String tradFormCd;
    private String tradFormNm;
    private String vdSizeCd;
    private String vdSizeNm;
    private String listedYn;
    private String fndDate;
    private String currCd;
    private String bankCd;
    private String bankNm;
    private String acctNo;
    private String acctHolder;
    private java.math.BigDecimal creditLimit;
    private String taxBillTyp;
    private String taxBillTypNm;
    private String invVeriYn;
    private String purcChrgId;
    private String purcChrgNm;
    private String tradStartYmd;
    private String stopYmd;
    private String stopReason;
    private String riskGrade;
    private String nextEvalYmd;
    private String regSts;
    private String regStsNm;
    private String remark;
    private String useYn;
    private String regId;
    private String modId;

    private List<VdContact> contacts = new ArrayList<>();
    private List<VdLicense> licenses = new ArrayList<>();
    private List<VdStopHis> stopHis = new ArrayList<>();
    // 등록심사 워크플로우
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private com.purchasesystem.domain.approval.AppApproval approval;
}
