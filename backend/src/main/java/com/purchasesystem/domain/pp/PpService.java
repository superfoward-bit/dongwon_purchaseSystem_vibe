package com.purchasesystem.domain.pp;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.pp.mapper.PpMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PpService {

    private static final String DOC_TYP = "PP";

    private final PpMapper ppMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;

    public List<PpPlan> getList(String keyword, String sts, String planYear) { return ppMapper.findList(keyword, sts, planYear); }

    public PpPlan getDetail(Long id) {
        PpPlan p = ppMapper.findById(id);
        if (p == null) throw new BusinessException("구매계획을 찾을 수 없습니다.");
        List<PpPlanItem> items = ppMapper.findItems(id, p.getPlanYear());
        p.setItems(items);
        p.setTotActualAmt(items.stream().map(i -> i.getActualAmt() == null ? BigDecimal.ZERO : i.getActualAmt())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        p.setActions(statusService.availableActions(DOC_TYP, p.getSts()));
        p.setHistory(statusService.history(DOC_TYP, id));
        return p;
    }

    @Transactional
    public Long create(PpPlan p, LoginUser user) {
        if (p.getPlanYear() == null || p.getPlanYear().length() != 4) throw new BusinessException("계획연도(YYYY)를 입력하세요.");
        p.setCompCd(user.compCd());
        p.setSts("DRAFT");
        p.setRegId(user.usrId());
        p.setPlanNo(docNoService.generate(DOC_TYP));
        applyTot(p);
        ppMapper.insertPlan(p);
        saveItems(p);
        return p.getId();
    }

    @Transactional
    public void update(Long id, PpPlan p, LoginUser user) {
        PpPlan cur = ppMapper.findById(id);
        if (cur == null) throw new BusinessException("구매계획을 찾을 수 없습니다.");
        if (!"DRAFT".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        p.setId(id);
        p.setModId(user.usrId());
        applyTot(p);
        ppMapper.updatePlan(p);
        ppMapper.deleteItems(id);
        saveItems(p);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        PpPlan cur = ppMapper.findById(id);
        if (cur == null) throw new BusinessException("구매계획을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getPlanNo(), cur.getSts(), actionCode, reason, user.usrId());
        ppMapper.updateStatus(id, toSts, user.usrId());
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        PpPlan cur = ppMapper.findById(id);
        if (cur == null) return;
        if (!"DRAFT".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        ppMapper.deleteItems(id);
        ppMapper.deletePlan(id, user.usrId());
    }

    private void applyTot(PpPlan p) {
        BigDecimal tot = BigDecimal.ZERO;
        if (p.getItems() != null)
            for (PpPlanItem it : p.getItems()) tot = tot.add(it.getPlanAmt() == null ? BigDecimal.ZERO : it.getPlanAmt());
        p.setTotPlanAmt(tot);
    }

    private void saveItems(PpPlan p) {
        if (p.getItems() == null) return;
        int line = 1;
        for (PpPlanItem it : p.getItems()) {
            it.setPlanId(p.getId());
            it.setLineNo(line++);
            ppMapper.insertItem(it);
        }
    }
}
