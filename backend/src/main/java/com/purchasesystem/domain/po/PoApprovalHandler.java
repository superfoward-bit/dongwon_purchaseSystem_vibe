package com.purchasesystem.domain.po;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.iface.IfService;
import com.purchasesystem.domain.po.mapper.PoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 발주 결재 핸들러: 결재 완료/반려 시 발주 상태 갱신. 확정 시 SAP 전송(연계). */
@Component
@RequiredArgsConstructor
public class PoApprovalHandler implements ApprovableHandler {

    private final PoMapper poMapper;
    private final StatusTransitionService statusService;
    private final IfService ifService;

    @Override
    public String docTyp() { return "PO"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        PoOrder po = poMapper.findById(docId);
        if (po == null || !"REQ".equals(po.getSts())) return;
        String toSts = statusService.transition("PO", docId, po.getPoNo(), po.getSts(), "APPROVE", "결재완료", actorId);
        poMapper.updateStatus(docId, toSts, actorId);
        // 발주 확정 → SAP 전송(연계)
        try {
            ifService.send("PO_SEND", "발주 전송", "SAP", "PO", docId, po.getPoNo(),
                    "협력사=" + po.getVdNm() + ", 총액=" + po.getTotAmt(), po.getCompCd(), actorId);
        } catch (Exception ignore) { }
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        PoOrder po = poMapper.findById(docId);
        if (po == null || !"REQ".equals(po.getSts())) return;
        String toSts = statusService.transition("PO", docId, po.getPoNo(), po.getSts(), "REJECT", reason, actorId);
        poMapper.updateStatus(docId, toSts, actorId);
    }
}
