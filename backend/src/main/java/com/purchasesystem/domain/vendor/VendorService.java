package com.purchasesystem.domain.vendor;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.vendor.mapper.VendorMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VendorService {

    private static final String DOC_TYP = "VD";

    private final VendorMapper vendorMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;

    public List<VdVendor> getList(String keyword, String vdSts) {
        return vendorMapper.findList(keyword, vdSts);
    }

    public List<VdVendor> getRegList(String keyword, String regSts) {
        return vendorMapper.findRegList(keyword, regSts);
    }

    public VdVendor getPayInfo(String vdCd) {
        return vendorMapper.findPayInfo(vdCd);
    }

    /** 거래중지/재개 — 사유 필수, 이력 기록 */
    @Transactional
    public void changeTrade(Long id, String action, String reason, LoginUser user) {
        VdVendor v = vendorMapper.findById(id);
        if (v == null) throw new BusinessException("협력사를 찾을 수 없습니다.");
        if (reason == null || reason.isBlank()) throw new BusinessException("사유를 입력하세요.");
        boolean stop = "STOP".equals(action);
        String newSts = stop ? "STOP" : "ACTIVE";
        vendorMapper.updateTradeStatus(id, newSts, stop ? java.time.LocalDate.now().toString() : null, reason, user.usrId());
        VdStopHis h = new VdStopHis();
        h.setVendorId(id); h.setVdCd(v.getVdCd()); h.setAction(action); h.setReason(reason); h.setActorId(user.usrId());
        vendorMapper.insertStopHis(h);
    }

    public VdVendor getDetail(Long id) {
        VdVendor v = vendorMapper.findById(id);
        if (v == null) throw new BusinessException("협력사를 찾을 수 없습니다.");
        v.setContacts(vendorMapper.findContacts(id));
        v.setLicenses(vendorMapper.findLicenses(id));
        v.setStopHis(vendorMapper.findStopHis(id));
        v.setActions(statusService.availableActions(DOC_TYP, v.getRegSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction())).toList());
        v.setHistory(statusService.history(DOC_TYP, id));
        v.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return v;
    }

    @Transactional
    public Long create(VdVendor v, LoginUser user) {
        v.setCompCd(user.compCd());
        v.setVdCd(docNoService.generate("VD"));   // VD-00001
        v.setRegSts("DRAFT");                       // 신규등록은 작성중부터
        v.setVdSts("STOP");                          // 심사승인 전까지 거래중지
        v.setRegId(user.usrId());
        vendorMapper.insert(v);
        saveSubs(v);
        return v.getId();
    }

    @Transactional
    public void update(Long id, VdVendor v, LoginUser user) {
        VdVendor cur = vendorMapper.findById(id);
        if (cur == null) throw new BusinessException("협력사를 찾을 수 없습니다.");
        if ("REQ".equals(cur.getRegSts())) throw new BusinessException("심사요청 상태에서는 수정할 수 없습니다. 회수 후 수정하세요.");
        v.setId(id);
        v.setModId(user.usrId());
        vendorMapper.update(v);
        vendorMapper.deleteContacts(id);
        vendorMapper.deleteLicenses(id);
        saveSubs(v);
    }

    /** 등록 심사요청(상신) */
    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        VdVendor cur = vendorMapper.findById(id);
        if (cur == null) throw new BusinessException("협력사를 찾을 수 없습니다.");
        if (!"DRAFT".equals(cur.getRegSts())) throw new BusinessException("작성중 상태에서만 심사요청할 수 있습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getVdCd(), cur.getRegSts(), "SUBMIT", null, user.usrId());
        vendorMapper.updateRegStatus(id, toSts, null, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getVdCd(), "협력사 등록심사 " + cur.getVdNm(), approvers, user);
    }

    /** 상태전이 액션(회수 등) */
    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        VdVendor cur = vendorMapper.findById(id);
        if (cur == null) throw new BusinessException("협력사를 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getVdCd(), cur.getRegSts(), actionCode, reason, user.usrId());
        vendorMapper.updateRegStatus(id, toSts, null, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        VdVendor cur = vendorMapper.findById(id);
        if (cur == null) return;
        if ("REQ".equals(cur.getRegSts())) throw new BusinessException("심사요청 상태에서는 삭제할 수 없습니다.");
        vendorMapper.deleteContacts(id);
        vendorMapper.deleteLicenses(id);
        vendorMapper.deleteVendor(id, user.usrId());
    }

    private void saveSubs(VdVendor v) {
        int line = 1;
        if (v.getContacts() != null) for (VdContact c : v.getContacts()) {
            c.setVendorId(v.getId());
            c.setLineNo(line++);
            vendorMapper.insertContact(c);
        }
        line = 1;
        if (v.getLicenses() != null) for (VdLicense l : v.getLicenses()) {
            l.setVendorId(v.getId());
            l.setLineNo(line++);
            vendorMapper.insertLicense(l);
        }
    }
}
