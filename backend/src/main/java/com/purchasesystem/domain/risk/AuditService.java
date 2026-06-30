package com.purchasesystem.domain.risk;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.risk.mapper.RiskMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String DOC_TYP = "AD";

    private final RiskMapper riskMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;

    public List<VdAudit> getList(String keyword, String sts) { return riskMapper.findAuditList(keyword, sts); }

    public VdAudit getDetail(Long id) {
        VdAudit a = riskMapper.findAuditById(id);
        if (a == null) throw new BusinessException("감시를 찾을 수 없습니다.");
        a.setResults(riskMapper.findAuditResults(id));
        a.setActions(statusService.availableActions(DOC_TYP, a.getSts()));
        a.setHistory(statusService.history(DOC_TYP, id));
        return a;
    }

    @Transactional
    public Long create(VdAudit a, LoginUser user) {
        if (a.getVdCd() == null) throw new BusinessException("협력사를 선택하세요.");
        a.setCompCd(user.compCd());
        a.setAuditorId(user.usrId());
        a.setRegId(user.usrId());
        a.setAuditNo(docNoService.generate(DOC_TYP));
        riskMapper.insertAudit(a);
        if (a.getResults() != null && !a.getResults().isEmpty()) saveResultsInternal(a.getId(), a.getResults());
        return a.getId();
    }

    /** 점검 항목 저장 → 지적건수/종합등급 자동 산출 */
    @Transactional
    public void saveResults(Long auditId, List<VdAuditResult> results, LoginUser user) {
        VdAudit cur = riskMapper.findAuditById(auditId);
        if (cur == null) throw new BusinessException("감시를 찾을 수 없습니다.");
        if (!"ING".equals(cur.getSts())) throw new BusinessException("점검중 상태에서만 입력할 수 있습니다.");
        saveResultsInternal(auditId, results);
    }

    private void saveResultsInternal(Long auditId, List<VdAuditResult> results) {
        riskMapper.deleteAuditResults(auditId);
        int seq = 1, findingCnt = 0, worst = 0;  // 0 SAFE, 1 WATCH, 2 RISK
        for (VdAuditResult r : results) {
            r.setAuditId(auditId);
            r.setItemSeq(seq++);
            if (r.getResult() == null) r.setResult("SAFE");
            riskMapper.insertAuditResult(r);
            int sev = switch (r.getResult()) { case "RISK" -> 2; case "WATCH" -> 1; default -> 0; };
            if (sev > 0) findingCnt++;
            if (sev > worst) worst = sev;
        }
        String grade = switch (worst) { case 2 -> "RISK"; case 1 -> "WATCH"; default -> "SAFE"; };
        riskMapper.updateAuditSummary(auditId, grade, findingCnt, findingCnt > 0 ? "Y" : "N");
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        VdAudit cur = riskMapper.findAuditById(id);
        if (cur == null) throw new BusinessException("감시를 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getAuditNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        riskMapper.updateAuditStatus(id, toSts, user.usrId());
        // 감시완료(DONE) 시 리스크등급을 협력사 마스터로 환류
        if ("DONE".equals(toSts) && cur.getVdCd() != null && cur.getResultGrade() != null) {
            riskMapper.updateVendorRisk(cur.getVdCd(), cur.getResultGrade());
        }
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        VdAudit cur = riskMapper.findAuditById(id);
        if (cur == null) return;
        if (!"ING".equals(cur.getSts())) throw new BusinessException("점검중 상태에서만 삭제할 수 있습니다.");
        riskMapper.deleteAuditResults(id);
        riskMapper.deleteAudit(id, user.usrId());
    }
}
