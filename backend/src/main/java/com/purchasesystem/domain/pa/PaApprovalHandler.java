package com.purchasesystem.domain.pa;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.pa.mapper.PaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 매입조정 결재 핸들러: 결재완료=확정(CFM, 정산 반영 대상), 반려=작성중. */
@Component
@RequiredArgsConstructor
public class PaApprovalHandler implements ApprovableHandler {

    private final PaMapper paMapper;
    private final StatusTransitionService statusService;

    @Override
    public String docTyp() { return "PA"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        PaAdjust a = paMapper.findById(docId);
        if (a == null || !"REQ".equals(a.getSts())) return;
        String toSts = statusService.transition("PA", docId, a.getAdjNo(), a.getSts(), "APPROVE", "결재완료", actorId);
        paMapper.updateStatus(docId, toSts, actorId);
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        PaAdjust a = paMapper.findById(docId);
        if (a == null || !"REQ".equals(a.getSts())) return;
        String toSts = statusService.transition("PA", docId, a.getAdjNo(), a.getSts(), "REJECT", reason, actorId);
        paMapper.updateStatus(docId, toSts, actorId);
    }
}
