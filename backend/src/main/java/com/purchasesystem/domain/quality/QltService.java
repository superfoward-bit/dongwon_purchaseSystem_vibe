package com.purchasesystem.domain.quality;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.quality.mapper.QltMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QltService {

    private static final String DOC_TYP = "QI";

    private final QltMapper qltMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;

    public List<QlInspection> getList(String keyword, String sts) { return qltMapper.findList(keyword, sts); }

    public QlInspection getDetail(Long id) {
        QlInspection q = qltMapper.findById(id);
        if (q == null) throw new BusinessException("입고검사를 찾을 수 없습니다.");
        q.setResults(qltMapper.findResults(id));
        q.setActions(statusService.availableActions(DOC_TYP, q.getSts()));
        q.setHistory(statusService.history(DOC_TYP, id));
        return q;
    }

    public List<QlInspection> getInspectableGrs(String keyword) { return qltMapper.findInspectableGrs(keyword); }

    public QlInspection prepareFromGr(Long grId) {
        QlInspection h = qltMapper.findGrHeader(grId);
        if (h == null) throw new BusinessException("입고건을 찾을 수 없습니다.");
        return h;
    }

    @Transactional
    public Long create(QlInspection q, LoginUser user) {
        if (q.getGrId() == null) throw new BusinessException("입고건을 선택하세요.");
        q.setCompCd(user.compCd());
        q.setInspUsrId(user.usrId());
        q.setRegId(user.usrId());
        q.setInspNo(docNoService.generate(DOC_TYP));
        qltMapper.insertInspection(q);
        if (q.getResults() != null && !q.getResults().isEmpty()) saveResultsInternal(q.getId(), q.getResults());
        return q.getId();
    }

    /** 검사항목 저장 → 합부 자동 판정 (전부 합격→PASS, 전부 불합격→FAIL, 혼합→PARTIAL) */
    @Transactional
    public void saveResults(Long inspId, List<QlResult> results, LoginUser user) {
        QlInspection cur = qltMapper.findById(inspId);
        if (cur == null) throw new BusinessException("입고검사를 찾을 수 없습니다.");
        if (!"ING".equals(cur.getSts())) throw new BusinessException("검사중 상태에서만 입력할 수 있습니다.");
        saveResultsInternal(inspId, results);
    }

    private void saveResultsInternal(Long inspId, List<QlResult> results) {
        qltMapper.deleteResults(inspId);
        int seq = 1, pass = 0, fail = 0;
        for (QlResult r : results) {
            r.setInspId(inspId);
            r.setItemSeq(seq++);
            // 규격 상·하한 + 측정값(수치)이 있으면 자동 합부판정(범위 내=합격)
            if (r.getMeasureNum() != null && (r.getSpecLower() != null || r.getSpecUpper() != null)) {
                boolean ok = (r.getSpecLower() == null || r.getMeasureNum().compareTo(r.getSpecLower()) >= 0)
                          && (r.getSpecUpper() == null || r.getMeasureNum().compareTo(r.getSpecUpper()) <= 0);
                r.setPassYn(ok ? "Y" : "N");
            } else if (r.getPassYn() == null) {
                r.setPassYn("Y");
            }
            qltMapper.insertResult(r);
            if ("Y".equals(r.getPassYn())) pass++; else fail++;
        }
        String result = fail == 0 ? "PASS" : (pass == 0 ? "FAIL" : "PARTIAL");
        qltMapper.updateResultSummary(inspId, result);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        QlInspection cur = qltMapper.findById(id);
        if (cur == null) throw new BusinessException("입고검사를 찾을 수 없습니다.");
        if ("COMPLETE".equals(actionCode) && cur.getInspResult() == null) {
            throw new BusinessException("검사항목을 입력/저장하여 합부 판정 후 완료할 수 있습니다.");
        }
        String toSts = statusService.transition(DOC_TYP, id, cur.getInspNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        qltMapper.updateStatus(id, toSts, user.usrId());
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        QlInspection cur = qltMapper.findById(id);
        if (cur == null) return;
        if (!"ING".equals(cur.getSts())) throw new BusinessException("검사중 상태에서만 삭제할 수 있습니다.");
        qltMapper.deleteResults(id);
        qltMapper.deleteInspection(id, user.usrId());
    }
}
