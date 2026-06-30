package com.purchasesystem.domain.ip;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.ip.mapper.PriceRsvMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 단가 변경예약 결재 핸들러: 결재완료=적용(구간분할 반영), 반려=작성중. */
@Component
@RequiredArgsConstructor
public class PriceRsvApprovalHandler implements ApprovableHandler {

    private final PriceRsvMapper rsvMapper;
    private final PriceApplyService priceApplyService;
    private final StatusTransitionService statusService;

    @Override
    public String docTyp() { return "IP"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        IpRsv r = rsvMapper.findById(docId);
        if (r == null || !"REQ".equals(r.getSts())) return;
        String toSts = statusService.transition("IP", docId, r.getRsvNo(), r.getSts(), "APPROVE", "결재완료", actorId);
        rsvMapper.updateStatus(docId, toSts, actorId);
        // 확정단가 구간 반영 (앞구간당김/뒷구간당김/잔여복원/삭제 + 신규삽입)
        priceApplyService.applyReservation(docId, actorId);
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        IpRsv r = rsvMapper.findById(docId);
        if (r == null || !"REQ".equals(r.getSts())) return;
        String toSts = statusService.transition("IP", docId, r.getRsvNo(), r.getSts(), "REJECT", reason, actorId);
        rsvMapper.updateStatus(docId, toSts, actorId);
    }
}
