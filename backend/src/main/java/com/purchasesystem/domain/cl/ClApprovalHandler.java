package com.purchasesystem.domain.cl;

import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovableHandler;
import com.purchasesystem.domain.cl.mapper.ClMapper;
import com.purchasesystem.domain.iface.IfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 정산 결재 핸들러: 결재완료=마감확정(잠금), 반려=작성중. 확정 시 SAP 전송(연계). */
@Component
@RequiredArgsConstructor
public class ClApprovalHandler implements ApprovableHandler {

    private final ClMapper clMapper;
    private final StatusTransitionService statusService;
    private final IfService ifService;

    @Override
    public String docTyp() { return "CL"; }

    @Override
    public void onApprovalCompleted(Long docId, String actorId) {
        ClClose c = clMapper.findById(docId);
        if (c == null || !"REQ".equals(c.getSts())) return;
        String toSts = statusService.transition("CL", docId, c.getCloseNo(), c.getSts(), "APPROVE", "결재완료", actorId);
        clMapper.updateStatus(docId, toSts, "Y", actorId);   // 마감확정 → 잠금
        // 정산 확정 → SAP 전송(연계)
        try {
            ifService.send("CL_SEND", "정산 전송", "SAP", "CL", docId, c.getCloseNo(),
                    "협력사=" + c.getVdNm() + ", 마감월=" + c.getCloseYm() + ", 최종정산액=" + c.getNetAmt(), c.getCompCd(), actorId);
        } catch (Exception ignore) { }
    }

    @Override
    public void onApprovalRejected(Long docId, String actorId, String reason) {
        ClClose c = clMapper.findById(docId);
        if (c == null || !"REQ".equals(c.getSts())) return;
        String toSts = statusService.transition("CL", docId, c.getCloseNo(), c.getSts(), "REJECT", reason, actorId);
        clMapper.updateStatus(docId, toSts, "N", actorId);
    }
}
