package com.purchasesystem.domain.ct;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.ct.mapper.ContractMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 단가계약 결재 핸들러: 결재완료=계약중(ACTIVE), 반려=작성중. */
@Component
@RequiredArgsConstructor
public class ContractApprovalHandler implements ApprovableHandler {

    private final ContractMapper contractMapper;
    private final StatusTransitionService statusService;

    @Override
    public String docTyp() { return "CT"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        CtContract c = contractMapper.findById(docId);
        if (c == null || !"REQ".equals(c.getSts())) return;
        String toSts = statusService.transition("CT", docId, c.getCtNo(), c.getSts(), "APPROVE", "결재완료", actorId);
        contractMapper.updateStatus(docId, toSts, actorId);
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        CtContract c = contractMapper.findById(docId);
        if (c == null || !"REQ".equals(c.getSts())) return;
        String toSts = statusService.transition("CT", docId, c.getCtNo(), c.getSts(), "REJECT", reason, actorId);
        contractMapper.updateStatus(docId, toSts, actorId);
    }
}
