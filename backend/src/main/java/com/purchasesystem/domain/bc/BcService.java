package com.purchasesystem.domain.bc;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.bc.mapper.BcMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BcService {

    private static final String DOC_TYP = "BC";

    private final BcMapper bcMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;

    public List<BcContract> getList(String keyword, String sts, String vdCd) { return bcMapper.findList(keyword, sts, vdCd); }

    public List<BcContract> getExpiring(int days) { return bcMapper.findExpiring(days); }

    public BcContract getDetail(Long id) {
        BcContract c = bcMapper.findById(id);
        if (c == null) throw new BusinessException("거래계약을 찾을 수 없습니다.");
        c.setTerms(bcMapper.findTerms(id));
        c.setActions(statusService.availableActions(DOC_TYP, c.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction())).toList());
        c.setHistory(statusService.history(DOC_TYP, id));
        c.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return c;
    }

    @Transactional
    public Long create(BcContract c, LoginUser user) {
        c.setCompCd(user.compCd());
        if (c.getChrgUsrId() == null) { c.setChrgUsrId(user.usrId()); c.setChrgUsrNm(user.usrNm()); }
        c.setSts("TMP");
        c.setBcRev(0);
        c.setRegId(user.usrId());
        c.setBcNo(docNoService.generate(DOC_TYP));
        bcMapper.insertContract(c);
        saveTerms(c);
        return c.getId();
    }

    @Transactional
    public void update(Long id, BcContract c, LoginUser user) {
        BcContract cur = bcMapper.findById(id);
        if (cur == null) throw new BusinessException("거래계약을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        c.setId(id);
        c.setModId(user.usrId());
        bcMapper.updateContract(c);
        bcMapper.deleteTerms(id);
        saveTerms(c);
    }

    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        BcContract cur = bcMapper.findById(id);
        if (cur == null) throw new BusinessException("거래계약을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getBcNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        bcMapper.updateStatus(id, toSts, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getBcNo(), cur.getBcTitle(), approvers, user);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        BcContract cur = bcMapper.findById(id);
        if (cur == null) throw new BusinessException("거래계약을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getBcNo(), cur.getSts(), actionCode, reason, user.usrId());
        bcMapper.updateStatus(id, toSts, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);
        return toSts;
    }

    /** 개정: 계약중(ACTIVE) 계약을 다음 차수로 복제하여 작성중 신규본 생성 */
    @Transactional
    public Long revise(Long id, LoginUser user) {
        BcContract cur = bcMapper.findById(id);
        if (cur == null) throw new BusinessException("거래계약을 찾을 수 없습니다.");
        if (!"ACTIVE".equals(cur.getSts())) throw new BusinessException("계약중 상태에서만 개정할 수 있습니다.");
        BcContract rev = new BcContract();
        rev.setCompCd(cur.getCompCd());
        rev.setBcNo(cur.getBcNo());                 // 동일 계약번호
        rev.setBcRev(cur.getBcRev() + 1);           // 차수 +1
        rev.setBcTitle(cur.getBcTitle());
        rev.setBcTyp(cur.getBcTyp());
        rev.setVdCd(cur.getVdCd());
        rev.setVdNm(cur.getVdNm());
        rev.setPurcGrpCd(cur.getPurcGrpCd());
        rev.setChrgUsrId(cur.getChrgUsrId());
        rev.setChrgUsrNm(cur.getChrgUsrNm());
        rev.setValidSd(cur.getValidSd());
        rev.setValidEd(cur.getValidEd());
        rev.setPayCond(cur.getPayCond());
        rev.setDelyCond(cur.getDelyCond());
        rev.setCurrCd(cur.getCurrCd());
        rev.setAutoRenewYn(cur.getAutoRenewYn());
        rev.setNoticeDays(cur.getNoticeDays());
        rev.setPrevId(cur.getId());                 // 이전 차수 연결
        rev.setSts("TMP");
        rev.setRegId(user.usrId());
        rev.setTerms(bcMapper.findTerms(cur.getId()));  // 조항 복제
        bcMapper.insertContract(rev);
        saveTerms(rev);
        return rev.getId();
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        BcContract cur = bcMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        bcMapper.deleteTerms(id);
        bcMapper.deleteContract(id, user.usrId());
    }

    private void saveTerms(BcContract c) {
        if (c.getTerms() == null) return;
        int line = 1;
        for (BcTerm t : c.getTerms()) {
            t.setContractId(c.getId());
            t.setLineNo(line++);
            bcMapper.insertTerm(t);
        }
    }
}
