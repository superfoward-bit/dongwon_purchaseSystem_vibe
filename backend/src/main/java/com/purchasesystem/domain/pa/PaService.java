package com.purchasesystem.domain.pa;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.pa.mapper.PaMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaService {

    private static final String DOC_TYP = "PA";
    private static final BigDecimal VAT_RATE = new BigDecimal("0.1");

    private final PaMapper paMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;

    public List<PaAdjust> getList(String keyword, String sts, String vdCd) { return paMapper.findList(keyword, sts, vdCd); }

    public PaAdjust getDetail(Long id) {
        PaAdjust a = paMapper.findById(id);
        if (a == null) throw new BusinessException("매입조정을 찾을 수 없습니다.");
        a.setActions(statusService.availableActions(DOC_TYP, a.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction())).toList());
        a.setHistory(statusService.history(DOC_TYP, id));
        a.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return a;
    }

    @Transactional
    public Long create(PaAdjust a, LoginUser user) {
        a.setCompCd(user.compCd());
        a.setSts("TMP");
        a.setRegId(user.usrId());
        a.setAdjNo(docNoService.generate(DOC_TYP));
        applyVat(a);
        paMapper.insert(a);
        return a.getId();
    }

    @Transactional
    public void update(Long id, PaAdjust a, LoginUser user) {
        PaAdjust cur = paMapper.findById(id);
        if (cur == null) throw new BusinessException("매입조정을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        a.setId(id);
        a.setModId(user.usrId());
        applyVat(a);
        paMapper.update(a);
    }

    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        PaAdjust cur = paMapper.findById(id);
        if (cur == null) throw new BusinessException("매입조정을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getAdjNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        paMapper.updateStatus(id, toSts, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getAdjNo(), "매입조정 " + cur.getAdjNo(), approvers, user);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        PaAdjust cur = paMapper.findById(id);
        if (cur == null) throw new BusinessException("매입조정을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getAdjNo(), cur.getSts(), actionCode, reason, user.usrId());
        paMapper.updateStatus(id, toSts, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        PaAdjust cur = paMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        paMapper.delete(id, user.usrId());
    }

    /** 공급가 조정금액 기준 부가세·합계 자동산출 (±부호 유지). 과세(TAX)만 10%, 면세/영세율/불공제는 0 */
    private void applyVat(PaAdjust a) {
        BigDecimal supl = a.getSuplAdjAmt() == null ? BigDecimal.ZERO : a.getSuplAdjAmt();
        boolean taxable = a.getTaxTyp() == null || "TAX".equals(a.getTaxTyp());
        BigDecimal vat = taxable ? supl.multiply(VAT_RATE).setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        a.setSuplAdjAmt(supl);
        a.setVatAdjAmt(vat);
        a.setTotAdjAmt(supl.add(vat));
    }
}
