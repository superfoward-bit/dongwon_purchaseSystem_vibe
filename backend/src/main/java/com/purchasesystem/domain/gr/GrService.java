package com.purchasesystem.domain.gr;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.gr.mapper.GrMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GrService {

    private static final String DOC_TYP = "GR";

    private final GrMapper grMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;

    public List<GrReceipt> getList(String keyword, String sts) {
        return grMapper.findList(keyword, sts);
    }

    public GrReceipt getDetail(Long id) {
        GrReceipt gr = grMapper.findById(id);
        if (gr == null) throw new BusinessException("입고를 찾을 수 없습니다.");
        gr.setItems(grMapper.findItems(id));
        gr.setActions(statusService.availableActions(DOC_TYP, gr.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction()))
                .toList());
        gr.setHistory(statusService.history(DOC_TYP, id));
        gr.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return gr;
    }

    /** 상신: 결재선 지정 → 결재중(REQ) + 결재요청 생성 */
    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        GrReceipt cur = grMapper.findById(id);
        if (cur == null) throw new BusinessException("입고를 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getGrNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        grMapper.updateStatus(id, toSts, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getGrNo(), "입고 " + cur.getGrNo(), approvers, user);
    }

    /** 발주확정(PC) 상태의 입고대상 발주 목록 */
    public List<GrReceipt> getReceivablePos(String keyword) {
        return grMapper.findConfirmedPos(keyword);
    }

    /** 발주 선택 시: 헤더(협력사) + 미입고 잔량 품목 프리필 */
    public GrReceipt prepareFromPo(Long poId) {
        GrReceipt header = grMapper.findPoHeader(poId);
        if (header == null) throw new BusinessException("발주를 찾을 수 없습니다.");
        header.setItems(grMapper.findPoItemsForGr(poId));
        return header;
    }

    @Transactional
    public Long create(GrReceipt gr, LoginUser user) {
        if (gr.getPoId() == null) throw new BusinessException("발주를 선택하세요.");
        gr.setCompCd(user.compCd());
        gr.setInspUsrId(user.usrId());
        gr.setSts("TMP");
        gr.setRegId(user.usrId());
        gr.setGrNo(docNoService.generate(DOC_TYP));
        applyAmount(gr);
        grMapper.insertReceipt(gr);
        saveItems(gr);
        return gr.getId();
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        GrReceipt cur = getDetail(id);
        String toSts = statusService.transition(DOC_TYP, id, cur.getGrNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        grMapper.updateStatus(id, toSts, user.usrId());

        // 입고취소(확정후) → 발주 입고누계 차감 / 회수 → 결재취소
        if ("CANCEL".equals(actionCode) && "CFM".equals(cur.getSts())) {
            adjustPoGrQty(cur.getItems(), -1);
        } else if ("RECALL".equals(actionCode)) {
            approvalService.cancelByDoc(DOC_TYP, id);
        }
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        GrReceipt cur = grMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        grMapper.deleteItems(id);
        grMapper.deleteReceipt(id, user.usrId());
    }

    // ---- 내부 ----
    private void adjustPoGrQty(List<GrItem> items, int sign) {
        if (items == null) return;
        for (GrItem it : items) {
            if (it.getPoItemId() == null) continue;
            BigDecimal qty = it.getGrQty() == null ? BigDecimal.ZERO : it.getGrQty();
            if (sign < 0) qty = qty.negate();
            grMapper.addPoItemGrQty(it.getPoItemId(), qty);
        }
    }

    private void applyAmount(GrReceipt gr) {
        BigDecimal tot = BigDecimal.ZERO;
        if (gr.getItems() != null) {
            for (GrItem it : gr.getItems()) {
                BigDecimal qty = it.getGrQty() == null ? BigDecimal.ZERO : it.getGrQty();
                BigDecimal prc = it.getPrc() == null ? BigDecimal.ZERO : it.getPrc();
                BigDecimal amt = qty.multiply(prc);
                it.setAmt(amt);
                tot = tot.add(amt);
            }
        }
        gr.setTotAmt(tot);
    }

    private void saveItems(GrReceipt gr) {
        int line = 1;
        if (gr.getItems() != null) {
            for (GrItem it : gr.getItems()) {
                it.setReceiptId(gr.getId());
                it.setLineNo(line++);
                grMapper.insertItem(it);
            }
        }
    }
}
