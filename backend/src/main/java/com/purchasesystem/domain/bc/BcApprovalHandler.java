package com.purchasesystem.domain.bc;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.bc.mapper.BcMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 표준거래계약 결재 핸들러: 결재완료=계약발효(ACTIVE), 반려=작성중. 개정본 발효 시 이전 차수는 만료처리. */
@Component
@RequiredArgsConstructor
public class BcApprovalHandler implements ApprovableHandler {

    private final BcMapper bcMapper;
    private final StatusTransitionService statusService;

    @Override
    public String docTyp() { return "BC"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        BcContract c = bcMapper.findById(docId);
        if (c == null || !"REQ".equals(c.getSts())) return;
        String toSts = statusService.transition("BC", docId, c.getBcNo(), c.getSts(), "APPROVE", "결재완료", actorId);
        bcMapper.updateStatus(docId, toSts, actorId);
        // 개정본이 발효되면 이전 차수 계약을 만료(supersede)
        if (c.getPrevId() != null) {
            bcMapper.updateStatus(c.getPrevId(), "EXPIRE", actorId);
        }
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        BcContract c = bcMapper.findById(docId);
        if (c == null || !"REQ".equals(c.getSts())) return;
        String toSts = statusService.transition("BC", docId, c.getBcNo(), c.getSts(), "REJECT", reason, actorId);
        bcMapper.updateStatus(docId, toSts, actorId);
    }
}
