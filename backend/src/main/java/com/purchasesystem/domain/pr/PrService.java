package com.purchasesystem.domain.pr;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.budget.BgBudget;
import com.purchasesystem.domain.budget.mapper.BudgetMapper;
import com.purchasesystem.domain.pr.mapper.PrMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrService {

    private static final String DOC_TYP = "PR";

    private final PrMapper prMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;
    private final BudgetMapper budgetMapper;

    public List<PrRequest> getList(String keyword, String sts) {
        return prMapper.findList(keyword, sts);
    }

    /** 상세: 헤더 + 품목 + 가능한 액션 + 진행이력 */
    public PrRequest getDetail(Long id) {
        PrRequest pr = prMapper.findById(id);
        if (pr == null) throw new BusinessException("구매요청을 찾을 수 없습니다.");
        pr.setItems(prMapper.findItems(id));
        // 결재는 결재함에서 처리하므로 문서 액션에서는 SUBMIT/APPROVE/REJECT 제외(회수·취소만 노출)
        pr.setActions(statusService.availableActions(DOC_TYP, pr.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction()))
                .toList());
        pr.setHistory(statusService.history(DOC_TYP, id));
        pr.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return pr;
    }

    /** 상신: 결재선 지정 → 결재중(REQ) + 결재요청 생성 */
    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        PrRequest cur = prMapper.findById(id);
        if (cur == null) throw new BusinessException("구매요청을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        checkBudget(cur, user);   // 예산 초과 검증
        String toSts = statusService.transition(DOC_TYP, id, cur.getPrNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        prMapper.updateStatus(id, toSts, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getPrNo(), cur.getPrTitle(), approvers, user);
    }

    /** 예산 초과 검증: 예산코드 지정 시 (기집행 + 본건) > 예산액 이면 상신 차단 */
    private void checkBudget(PrRequest pr, LoginUser user) {
        if (pr.getBudgetCd() == null || pr.getBudgetCd().isBlank()) return;
        BgBudget b = budgetMapper.findByCode(user.compCd(), pr.getBudgetCd());
        if (b == null) return;
        BigDecimal committed = budgetMapper.sumCommitted(pr.getBudgetCd(), pr.getId());  // 본건 제외 기집행
        BigDecimal req = pr.getTotAmt() == null ? BigDecimal.ZERO : pr.getTotAmt();
        BigDecimal available = b.getBudgetAmt().subtract(committed);
        if (req.compareTo(available) > 0) {
            throw new BusinessException(String.format(
                "예산 초과: [%s] 예산잔액 %,d원 < 요청액 %,d원 (예산 %,d / 기집행 %,d)",
                b.getBudgetNm(), available.longValue(), req.longValue(), b.getBudgetAmt().longValue(), committed.longValue()));
        }
    }

    @Transactional
    public Long create(PrRequest pr, LoginUser user) {
        pr.setCompCd(user.compCd());
        pr.setReqUsrId(user.usrId());
        pr.setSts("TMP");
        pr.setRegId(user.usrId());
        pr.setPrNo(docNoService.generate(DOC_TYP));
        applyAmount(pr);
        prMapper.insertRequest(pr);          // id 채워짐(useGeneratedKeys)
        saveItems(pr);
        return pr.getId();
    }

    @Transactional
    public void update(Long id, PrRequest pr, LoginUser user) {
        PrRequest cur = prMapper.findById(id);
        if (cur == null) throw new BusinessException("구매요청을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) {
            throw new BusinessException("작성중 상태에서만 수정할 수 있습니다. (현재: " + cur.getStsNm() + ")");
        }
        pr.setId(id);
        pr.setModId(user.usrId());
        applyAmount(pr);
        prMapper.updateRequest(pr);
        prMapper.deleteItems(id);
        saveItems(pr);
    }

    /** 상태전이 처리 (상신/결재/반려/회수/취소). 되돌리기 포함. */
    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        PrRequest cur = prMapper.findById(id);
        if (cur == null) throw new BusinessException("구매요청을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getPrNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        prMapper.updateStatus(id, toSts, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);  // 회수 시 결재 취소
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        PrRequest cur = prMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) {
            throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        }
        prMapper.deleteItems(id);
        prMapper.deleteRequest(id, user.usrId());
    }

    // ---- 내부 ----
    private void applyAmount(PrRequest pr) {
        BigDecimal tot = BigDecimal.ZERO;
        if (pr.getItems() != null) {
            for (PrItem it : pr.getItems()) {
                BigDecimal qty = it.getQty() == null ? BigDecimal.ZERO : it.getQty();
                BigDecimal prc = it.getEstPrc() == null ? BigDecimal.ZERO : it.getEstPrc();
                BigDecimal amt = qty.multiply(prc);
                it.setAmt(amt);
                tot = tot.add(amt);
            }
        }
        pr.setTotAmt(tot);
    }

    private void saveItems(PrRequest pr) {
        int line = 1;
        if (pr.getItems() != null) {
            for (PrItem it : pr.getItems()) {
                it.setRequestId(pr.getId());
                it.setLineNo(line++);
                prMapper.insertItem(it);
            }
        }
    }
}
