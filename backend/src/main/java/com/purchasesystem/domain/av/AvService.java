package com.purchasesystem.domain.av;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.av.mapper.AvMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AvService {

    private static final String DOC_TYP = "AV";

    private final AvMapper avMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;

    public List<AvAdvance> getList(String keyword, String sts, String vdCd) { return avMapper.findList(keyword, sts, vdCd); }

    public AvAdvance getDetail(Long id) {
        AvAdvance a = avMapper.findById(id);
        if (a == null) throw new BusinessException("선급금을 찾을 수 없습니다.");
        a.setRepays(avMapper.findRepays(id));
        a.setActions(statusService.availableActions(DOC_TYP, a.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction())).toList());
        a.setHistory(statusService.history(DOC_TYP, id));
        a.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return a;
    }

    @Transactional
    public Long create(AvAdvance a, LoginUser user) {
        if (a.getAdvAmt() == null || a.getAdvAmt().signum() <= 0) throw new BusinessException("선급금액을 입력하세요.");
        a.setCompCd(user.compCd());
        a.setSts("TMP");
        a.setRegId(user.usrId());
        a.setAdvNo(docNoService.generate(DOC_TYP));
        avMapper.insert(a);
        return a.getId();
    }

    @Transactional
    public void update(Long id, AvAdvance a, LoginUser user) {
        AvAdvance cur = avMapper.findById(id);
        if (cur == null) throw new BusinessException("선급금을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        a.setId(id);
        a.setModId(user.usrId());
        avMapper.update(a);
    }

    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        AvAdvance cur = avMapper.findById(id);
        if (cur == null) throw new BusinessException("선급금을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getAdvNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        avMapper.updateStatus(id, toSts, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getAdvNo(), "선급금 " + cur.getAdvNo(), approvers, user);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        AvAdvance cur = avMapper.findById(id);
        if (cur == null) throw new BusinessException("선급금을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getAdvNo(), cur.getSts(), actionCode, reason, user.usrId());
        avMapper.updateStatus(id, toSts, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        AvAdvance cur = avMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        avMapper.deleteRepays(id);
        avMapper.delete(id, user.usrId());
    }

    /** 반제(상계) 추가 → 잔액 차감, 잔액 0이면 반제완료(CLOSED) */
    @Transactional
    public void addRepay(Long id, AvRepay repay, LoginUser user) {
        AvAdvance cur = avMapper.findById(id);
        if (cur == null) throw new BusinessException("선급금을 찾을 수 없습니다.");
        if (!"CFM".equals(cur.getSts()) && !"CLOSED".equals(cur.getSts()))
            throw new BusinessException("지급확정된 선급금만 반제할 수 있습니다.");
        if (repay.getRepayAmt() == null || repay.getRepayAmt().signum() <= 0) throw new BusinessException("반제금액을 입력하세요.");
        BigDecimal bal = cur.getBalance() == null ? BigDecimal.ZERO : cur.getBalance();
        if (repay.getRepayAmt().compareTo(bal) > 0)
            throw new BusinessException("반제금액이 잔액(" + bal + ")을 초과합니다.");
        repay.setAdvanceId(id);
        repay.setLineNo(avMapper.nextRepayLine(id));
        avMapper.insertRepay(repay);
        BigDecimal newBal = cur.getAdvAmt().subtract(avMapper.sumRepay(id));
        String sts = newBal.signum() <= 0 ? "CLOSED" : "CFM";
        avMapper.updateBalance(id, newBal, sts, user.usrId());
    }
}
