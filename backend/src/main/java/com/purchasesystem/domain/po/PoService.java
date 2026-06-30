package com.purchasesystem.domain.po;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.po.mapper.PoMapper;
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
public class PoService {

    private static final String DOC_TYP = "PO";
    private static final BigDecimal VAT_RATE = new BigDecimal("0.1");

    private final PoMapper poMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;

    public List<PoOrder> getList(String keyword, String sts) {
        return poMapper.findList(keyword, sts);
    }

    public PoOrder getDetail(Long id) {
        PoOrder po = poMapper.findById(id);
        if (po == null) throw new BusinessException("발주를 찾을 수 없습니다.");
        po.setItems(poMapper.findItems(id));
        po.setPayPlans(poMapper.findPayPlans(id));
        po.setDlvSchedules(poMapper.findDlvSchedules(id));
        po.setActions(statusService.availableActions(DOC_TYP, po.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction()))
                .toList());
        po.setHistory(statusService.history(DOC_TYP, id));
        po.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return po;
    }

    /** 상신: 결재선 지정 → 결재중(REQ) + 결재요청 생성 */
    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        PoOrder cur = poMapper.findById(id);
        if (cur == null) throw new BusinessException("발주를 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        checkCredit(cur);   // 여신한도 초과 검증
        String toSts = statusService.transition(DOC_TYP, id, cur.getPoNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        poMapper.updateStatus(id, toSts, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getPoNo(), cur.getPoTitle(), approvers, user);
    }

    @Transactional
    public Long create(PoOrder po, LoginUser user) {
        po.setCompCd(user.compCd());
        po.setChrgUsrId(user.usrId());
        po.setSts("TMP");
        po.setRegId(user.usrId());
        po.setPoNo(docNoService.generate(DOC_TYP));
        applyAmount(po);
        poMapper.insertOrder(po);
        saveItems(po);
        savePayPlans(po);
        saveDlvSchedules(po);
        return po.getId();
    }

    @Transactional
    public void update(Long id, PoOrder po, LoginUser user) {
        PoOrder cur = poMapper.findById(id);
        if (cur == null) throw new BusinessException("발주를 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) {
            throw new BusinessException("작성중 상태에서만 수정할 수 있습니다. (현재: " + cur.getStsNm() + ")");
        }
        po.setId(id);
        po.setModId(user.usrId());
        applyAmount(po);
        poMapper.updateOrder(po);
        poMapper.deleteItems(id);
        saveItems(po);
        poMapper.deletePayPlans(id);
        savePayPlans(po);
        poMapper.deleteDlvSchedules(id);
        saveDlvSchedules(po);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        PoOrder cur = poMapper.findById(id);
        if (cur == null) throw new BusinessException("발주를 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getPoNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        poMapper.updateStatus(id, toSts, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        PoOrder cur = poMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) {
            throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        }
        poMapper.deleteItems(id);
        poMapper.deleteOrder(id, user.usrId());
    }

    /** 여신한도 초과 검증: 한도>0 이고 (미결제 확정발주 + 본건) > 한도 이면 상신 차단 */
    private void checkCredit(PoOrder po) {
        if (po.getVdCd() == null) return;
        BigDecimal limit = poMapper.findCreditLimit(po.getVdCd());
        if (limit == null || limit.signum() <= 0) return;   // 0=무제한
        BigDecimal outstanding = poMapper.sumOutstanding(po.getVdCd(), po.getId());
        BigDecimal thisAmt = po.getTotAmt() == null ? BigDecimal.ZERO : po.getTotAmt();
        if (outstanding.add(thisAmt).compareTo(limit) > 0) {
            throw new BusinessException(String.format(
                "여신한도 초과: 한도 %,d원 < 미결제 %,d + 본건 %,d원. 한도 상향 또는 기존 발주 정산 후 진행하세요.",
                limit.longValue(), outstanding.longValue(), thisAmt.longValue()));
        }
    }

    // ---- 내부: 공급가/부가세(10%)/합계 자동계산 ----
    private void applyAmount(PoOrder po) {
        BigDecimal supl = BigDecimal.ZERO, vat = BigDecimal.ZERO;
        if (po.getItems() != null) {
            for (PoItem it : po.getItems()) {
                BigDecimal qty = it.getQty() == null ? BigDecimal.ZERO : it.getQty();
                BigDecimal prc = it.getPrc() == null ? BigDecimal.ZERO : it.getPrc();
                BigDecimal s = qty.multiply(prc);
                // 과세구분: 과세(TAX)만 부가세 10%, 면세/영세율/불공제는 0
                boolean taxable = it.getTaxTyp() == null || "TAX".equals(it.getTaxTyp());
                BigDecimal v = taxable ? s.multiply(VAT_RATE).setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                it.setSuplAmt(s);
                it.setVatAmt(v);
                it.setAmt(s.add(v));
                supl = supl.add(s);
                vat = vat.add(v);
            }
        }
        po.setSuplAmt(supl);
        po.setVatAmt(vat);
        po.setTotAmt(supl.add(vat));
    }

    /** 분할납품 일정 저장 */
    private void saveDlvSchedules(PoOrder po) {
        if (po.getDlvSchedules() == null || po.getDlvSchedules().isEmpty()) return;
        int line = 1;
        for (PoDlvSchedule s : po.getDlvSchedules()) {
            s.setOrderId(po.getId());
            s.setLineNo(line++);
            poMapper.insertDlvSchedule(s);
        }
    }

    private void saveItems(PoOrder po) {
        int line = 1;
        if (po.getItems() != null) {
            for (PoItem it : po.getItems()) {
                it.setOrderId(po.getId());
                it.setLineNo(line++);
                poMapper.insertItem(it);
            }
        }
    }

    /** 분할결제 계획 저장: 비율 검증(합100) + 금액 자동산출(총액×비율) */
    private void savePayPlans(PoOrder po) {
        if (!"Y".equals(po.getPayPlanYn()) || po.getPayPlans() == null || po.getPayPlans().isEmpty()) return;
        BigDecimal rateSum = po.getPayPlans().stream()
                .map(p -> p.getRate() == null ? BigDecimal.ZERO : p.getRate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (rateSum.compareTo(new BigDecimal("100")) != 0)
            throw new BusinessException("선금/기성/잔금 비율 합계가 100%여야 합니다. (현재 " + rateSum + "%)");
        BigDecimal tot = po.getTotAmt() == null ? BigDecimal.ZERO : po.getTotAmt();
        int line = 1;
        for (PoPayPlan p : po.getPayPlans()) {
            p.setOrderId(po.getId());
            p.setLineNo(line++);
            BigDecimal rate = p.getRate() == null ? BigDecimal.ZERO : p.getRate();
            p.setAmt(tot.multiply(rate).divide(new BigDecimal("100"), 0, RoundingMode.HALF_UP));
            poMapper.insertPayPlan(p);
        }
    }
}
