package com.purchasesystem.domain.vendor;

import lombok.Data;

/** 협력사 거래중지/재개 이력 (VD_STOP_HIS). */
@Data
public class VdStopHis {
    private Long id;
    private Long vendorId;
    private String vdCd;
    private String action;     // STOP / RESUME
    private String reason;
    private String actorId;
    private String actionDt;
}
