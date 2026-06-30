package com.purchasesystem.domain.gr;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.gr.mapper.GrMapper;
import com.purchasesystem.domain.stock.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** 입고 결재 핸들러: 결재완료=입고확정(발주 입고누계 반영 + 재고 자동입고), 반려=작성중. */
@Component
@RequiredArgsConstructor
public class GrApprovalHandler implements ApprovableHandler {

    private final GrMapper grMapper;
    private final StatusTransitionService statusService;
    private final StockService stockService;

    @Override
    public String docTyp() { return "GR"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        GrReceipt gr = grMapper.findById(docId);
        if (gr == null || !"REQ".equals(gr.getSts())) return;
        String toSts = statusService.transition("GR", docId, gr.getGrNo(), gr.getSts(), "APPROVE", "결재완료", actorId);
        grMapper.updateStatus(docId, toSts, actorId);
        // 입고확정 → 발주품목 입고누계 가산
        List<GrItem> items = grMapper.findItems(docId);
        for (GrItem it : items) {
            if (it.getPoItemId() == null) continue;
            grMapper.addPoItemGrQty(it.getPoItemId(), it.getGrQty() == null ? BigDecimal.ZERO : it.getGrQty());
        }
        // 입고확정 → 재고 자동입고(합격수량, LOT/유통기한 승계)
        stockService.receiveFromGr(gr, items, actorId);
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        GrReceipt gr = grMapper.findById(docId);
        if (gr == null || !"REQ".equals(gr.getSts())) return;
        String toSts = statusService.transition("GR", docId, gr.getGrNo(), gr.getSts(), "REJECT", reason, actorId);
        grMapper.updateStatus(docId, toSts, actorId);
    }
}
