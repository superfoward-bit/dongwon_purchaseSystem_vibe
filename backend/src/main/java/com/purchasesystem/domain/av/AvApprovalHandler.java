package com.purchasesystem.domain.av;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.av.mapper.AvMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 선급금 결재 핸들러: 결재완료=지급확정(CFM, 잔액=선급금액), 반려=작성중. */
@Component
@RequiredArgsConstructor
public class AvApprovalHandler implements ApprovableHandler {

    private final AvMapper avMapper;
    private final StatusTransitionService statusService;

    @Override
    public String docTyp() { return "AV"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        AvAdvance a = avMapper.findById(docId);
        if (a == null || !"REQ".equals(a.getSts())) return;
        String toSts = statusService.transition("AV", docId, a.getAdvNo(), a.getSts(), "APPROVE", "결재완료", actorId);
        // 지급확정 → 잔액 = 선급금액
        avMapper.updateBalance(docId, a.getAdvAmt(), toSts, actorId);
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        AvAdvance a = avMapper.findById(docId);
        if (a == null || !"REQ".equals(a.getSts())) return;
        String toSts = statusService.transition("AV", docId, a.getAdvNo(), a.getSts(), "REJECT", reason, actorId);
        avMapper.updateStatus(docId, toSts, actorId);
    }
}
