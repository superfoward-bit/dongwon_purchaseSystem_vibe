package com.purchasesystem.domain.vendor;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.vendor.mapper.VendorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 협력사 등록심사 결재 핸들러: 심사승인=거래개시(ACTIVE), 반려=작성중. */
@Component
@RequiredArgsConstructor
public class VendorApprovalHandler implements ApprovableHandler {

    private final VendorMapper vendorMapper;
    private final StatusTransitionService statusService;

    @Override
    public String docTyp() { return "VD"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        VdVendor v = vendorMapper.findById(docId);
        if (v == null || !"REQ".equals(v.getRegSts())) return;
        String toSts = statusService.transition("VD", docId, v.getVdCd(), v.getRegSts(), "APPROVE", "심사승인", actorId);
        // 심사승인 → 등록상태 APPROVED + 거래상태 ACTIVE(거래개시)
        vendorMapper.updateRegStatus(docId, toSts, "ACTIVE", actorId);
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        VdVendor v = vendorMapper.findById(docId);
        if (v == null || !"REQ".equals(v.getRegSts())) return;
        String toSts = statusService.transition("VD", docId, v.getVdCd(), v.getRegSts(), "REJECT", reason, actorId);
        vendorMapper.updateRegStatus(docId, toSts, null, actorId);
    }
}
