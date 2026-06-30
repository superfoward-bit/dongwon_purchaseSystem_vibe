package com.purchasesystem.domain.ct;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.ct.mapper.ContractMapper;
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
public class ContractService {

    private static final String DOC_TYP = "CT";
    private static final BigDecimal VAT_RATE = new BigDecimal("0.1");

    private final ContractMapper contractMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;

    public List<CtContract> getList(String keyword, String sts) { return contractMapper.findList(keyword, sts); }

    public CtContract getDetail(Long id) {
        CtContract c = contractMapper.findById(id);
        if (c == null) throw new BusinessException("계약을 찾을 수 없습니다.");
        c.setItems(contractMapper.findItems(id));
        c.setActions(statusService.availableActions(DOC_TYP, c.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction())).toList());
        c.setHistory(statusService.history(DOC_TYP, id));
        c.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return c;
    }

    @Transactional
    public Long create(CtContract c, LoginUser user) {
        if (c.getItems() == null || c.getItems().isEmpty()) throw new BusinessException("계약 품목을 입력하세요.");
        c.setCompCd(user.compCd());
        c.setChrgUsrId(user.usrId());
        c.setSts("TMP");
        c.setRegId(user.usrId());
        c.setCtNo(docNoService.generate(DOC_TYP));
        applyAmount(c);
        contractMapper.insertContract(c);
        saveItems(c);
        return c.getId();
    }

    @Transactional
    public void update(Long id, CtContract c, LoginUser user) {
        CtContract cur = contractMapper.findById(id);
        if (cur == null) throw new BusinessException("계약을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        c.setId(id);
        c.setModId(user.usrId());
        applyAmount(c);
        contractMapper.updateContract(c);
        contractMapper.deleteItems(id);
        saveItems(c);
    }

    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        CtContract cur = contractMapper.findById(id);
        if (cur == null) throw new BusinessException("계약을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getCtNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        contractMapper.updateStatus(id, toSts, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getCtNo(), cur.getCtTitle(), approvers, user);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        CtContract cur = contractMapper.findById(id);
        if (cur == null) throw new BusinessException("계약을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getCtNo(), cur.getSts(), actionCode, reason, user.usrId());
        contractMapper.updateStatus(id, toSts, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        CtContract cur = contractMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        contractMapper.deleteItems(id);
        contractMapper.deleteContract(id, user.usrId());
    }

    /** 유효 계약단가 조회 (단가 자동적용용) */
    public CtItem findActivePrice(String vdCd, String itemCd, String ymd) {
        return contractMapper.findActivePrice(vdCd, itemCd, ymd);
    }

    // ---- 내부 ----
    private void applyAmount(CtContract c) {
        BigDecimal supl = BigDecimal.ZERO;
        if (c.getItems() != null) {
            for (CtItem it : c.getItems()) {
                // 적용기간 미지정 시 계약 유효기간으로
                if (it.getApplySd() == null || it.getApplySd().isBlank()) it.setApplySd(c.getValidSd());
                if (it.getApplyEd() == null || it.getApplyEd().isBlank()) it.setApplyEd(c.getValidEd());
                BigDecimal prc = it.getCtPrc() == null ? BigDecimal.ZERO : it.getCtPrc();
                supl = supl.add(prc);
            }
        }
        BigDecimal vat = supl.multiply(VAT_RATE).setScale(0, RoundingMode.HALF_UP);
        c.setSuplAmt(supl);
        c.setVatAmt(vat);
        c.setTotAmt(supl.add(vat));
    }

    private void saveItems(CtContract c) {
        int line = 1;
        for (CtItem it : c.getItems()) {
            it.setContractId(c.getId());
            it.setLineNo(line++);
            contractMapper.insertItem(it);
        }
    }
}
