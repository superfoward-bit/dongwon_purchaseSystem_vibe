package com.purchasesystem.domain.ip;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import com.purchasesystem.domain.approval.AppApproval;
import lombok.Data;
import java.util.List;

/** 단가 변경예약 헤더 */
@Data
public class IpRsv {
    private Long id;
    private String compCd;
    private String rsvNo;
    private String rsvTitle;
    private String vdCd;
    private String vdNm;
    private String chrgUsrId;
    private String applyDt;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String modId;

    private List<IpRsvLine> lines;
    // 상세 부가정보
    private List<CmStatusFlow> actions;
    private List<DocStatusHis> history;
    private AppApproval approval;
}
