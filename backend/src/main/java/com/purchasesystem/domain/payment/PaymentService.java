package com.purchasesystem.domain.payment;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.cl.ClClose;
import com.purchasesystem.domain.cl.mapper.ClMapper;
import com.purchasesystem.domain.payment.mapper.PaymentMapper;
import com.purchasesystem.domain.vendor.VdVendor;
import com.purchasesystem.domain.vendor.mapper.VendorMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final ClMapper clMapper;
    private final VendorMapper vendorMapper;
    private final DocNoService docNoService;

    public List<PyPayment> getList(String keyword, String paySts) { return paymentMapper.findList(keyword, paySts); }

    public PyPayment getDetail(Long id) {
        PyPayment p = paymentMapper.findById(id);
        if (p == null) throw new BusinessException("지급내역을 찾을 수 없습니다.");
        return p;
    }

    /** 확정 정산마감에서 지급 생성 — 지급예정일/지급방법/계좌 스냅샷 */
    @Transactional
    public Long createFromClose(Long closeId, LoginUser user) {
        ClClose c = clMapper.findById(closeId);
        if (c == null) throw new BusinessException("정산마감을 찾을 수 없습니다.");
        if (!"CFM".equals(c.getSts())) throw new BusinessException("마감확정된 정산만 지급 생성할 수 있습니다.");
        if (paymentMapper.countBySrc("CL", closeId) > 0) throw new BusinessException("이미 지급이 생성된 정산입니다.");

        PyPayment p = new PyPayment();
        p.setCompCd(user.compCd());
        p.setPayNo(docNoService.generate("PY"));
        p.setSrcTyp("CL");
        p.setSrcId(closeId);
        p.setSrcNo(c.getCloseNo());
        p.setVdCd(c.getVdCd());
        p.setVdNm(c.getVdNm());
        p.setPayDueYmd(c.getPayDueYmd());                                  // 정산 지급예정일 스냅샷
        p.setPayAmt(c.getNetAmt() != null ? c.getNetAmt() : c.getTotAmt()); // 최종정산액
        p.setPayMethodCd(c.getPayMethodCd());
        // 협력사 계좌정보 스냅샷
        VdVendor v = vendorMapper.findPayInfo(c.getVdCd());
        if (v != null) {
            p.setBankCd(v.getBankCd()); p.setBankNm(v.getBankNm());
            p.setAcctNo(v.getAcctNo()); p.setAcctHolder(v.getAcctHolder());
            if (p.getPayMethodCd() == null) p.setPayMethodCd(v.getPayMethodCd());
        }
        p.setRegId(user.usrId());
        paymentMapper.insert(p);
        return p.getId();
    }

    /** 지급 실행 — 실지급일/어음정보 기록 */
    @Transactional
    public void pay(Long id, String payYmd, String noteNo, String noteDueYmd, LoginUser user) {
        PyPayment p = paymentMapper.findById(id);
        if (p == null) throw new BusinessException("지급내역을 찾을 수 없습니다.");
        if (!"PLAN".equals(p.getPaySts())) throw new BusinessException("지급예정 상태에서만 지급 실행할 수 있습니다.");
        if (payYmd == null || payYmd.isBlank()) throw new BusinessException("실지급일을 입력하세요.");
        paymentMapper.updatePaid(id, payYmd, noteNo, noteDueYmd, user.usrId());
    }

    @Transactional
    public void cancel(Long id, LoginUser user) {
        PyPayment p = paymentMapper.findById(id);
        if (p == null) return;
        paymentMapper.updateStatus(id, "CANCEL", user.usrId());
    }
}
