package com.purchasesystem.domain.pr;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.pr.mapper.PrMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 구매요청 결재 핸들러: 결재 완료/반려 시 PR 상태 갱신. */
@Component
@RequiredArgsConstructor
public class PrApprovalHandler implements ApprovableHandler {

    private final PrMapper prMapper;
    private final StatusTransitionService statusService;

    @Override
    public String docTyp() { return "PR"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        PrRequest pr = prMapper.findById(docId);
        if (pr == null || !"REQ".equals(pr.getSts())) return;
        String toSts = statusService.transition("PR", docId, pr.getPrNo(), pr.getSts(), "APPROVE", "결재완료", actorId);
        prMapper.updateStatus(docId, toSts, actorId);
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        PrRequest pr = prMapper.findById(docId);
        if (pr == null || !"REQ".equals(pr.getSts())) return;
        String toSts = statusService.transition("PR", docId, pr.getPrNo(), pr.getSts(), "REJECT", reason, actorId);
        prMapper.updateStatus(docId, toSts, actorId);
    }
}
