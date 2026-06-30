package com.purchasesystem.domain.tax;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.cl.ClClose;
import com.purchasesystem.domain.cl.mapper.ClMapper;
import com.purchasesystem.domain.iface.IfService;
import com.purchasesystem.domain.tax.mapper.TaxMapper;
import com.purchasesystem.domain.tax.TxTaxbillItem;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxService {

    private final TaxMapper taxMapper;
    private final ClMapper clMapper;
    private final DocNoService docNoService;
    private final IfService ifService;

    public List<TxTaxbill> getList(String keyword, String billSts) { return taxMapper.findList(keyword, billSts); }
    public TxTaxbill getDetail(Long id) {
        TxTaxbill t = taxMapper.findById(id);
        if (t == null) throw new BusinessException("세금계산서를 찾을 수 없습니다.");
        t.setItems(taxMapper.findItems(id));
        return t;
    }

    /** 확정 정산마감에서 세금계산서 발행대상 생성 */
    @Transactional
    public Long createFromClose(Long closeId, LoginUser user) {
        ClClose c = clMapper.findById(closeId);
        if (c == null) throw new BusinessException("정산마감을 찾을 수 없습니다.");
        if (!"CFM".equals(c.getSts())) throw new BusinessException("마감확정된 정산만 세금계산서를 발행할 수 있습니다.");
        if (taxMapper.countByClose(closeId) > 0) throw new BusinessException("이미 세금계산서가 생성된 정산입니다.");
        TxTaxbill t = new TxTaxbill();
        t.setCompCd(user.compCd());
        t.setBillNo(docNoService.generate("TB"));
        t.setCloseId(closeId);
        t.setCloseNo(c.getCloseNo());
        t.setVdCd(c.getVdCd());
        t.setVdNm(c.getVdNm());
        t.setBillYmd(LocalDate.now().toString());
        t.setWriteYmd(LocalDate.now().toString());     // 작성일자
        // 면세만 있는 정산이면 면세 세금계산서, 아니면 과세
        t.setBillTyp(c.getTotVatAmt() != null && c.getTotVatAmt().signum() > 0 ? "TAX" : "FREE");
        t.setBillKind("NORMAL");
        t.setSupplyAmt(c.getTotSuplAmt());
        t.setVatAmt(c.getTotVatAmt());
        t.setTotAmt(c.getTotAmt());
        // 공급자(협력사)·공급받는자(자사) 사업자정보 스냅샷
        TxTaxbill sup = taxMapper.findSupplier(c.getVdCd());
        if (sup != null) { t.setSupBizNo(sup.getSupBizNo()); t.setSupNm(sup.getSupNm()); t.setSupCeo(sup.getSupCeo()); t.setSupAddr(sup.getSupAddr()); }
        TxTaxbill buy = taxMapper.findBuyer(user.compCd());
        if (buy != null) { t.setBuyBizNo(buy.getBuyBizNo()); t.setBuyNm(buy.getBuyNm()); t.setBuyCeo(buy.getBuyCeo()); t.setBuyAddr(buy.getBuyAddr()); }
        t.setRegId(user.usrId());
        taxMapper.insert(t);
        // 품목명세 생성(정산 GR 품목 집계)
        int line = 1;
        for (TxTaxbillItem it : taxMapper.findCloseItemLines(closeId)) {
            it.setBillId(t.getId());
            it.setLineNo(line++);
            it.setRegId(user.usrId());
            taxMapper.insertItem(it);
        }
        return t.getId();
    }

    /** 발행 → 국세청 전송(IF 시뮬) */
    @Transactional
    public void issue(Long id, LoginUser user) {
        TxTaxbill t = taxMapper.findById(id);
        if (t == null) throw new BusinessException("세금계산서를 찾을 수 없습니다.");
        if (!"DRAFT".equals(t.getBillSts())) throw new BusinessException("작성 상태에서만 발행할 수 있습니다.");
        ifService.send("TAXBILL", "전자세금계산서 발행", "NTS", "TB", id, t.getBillNo(),
                "협력사=" + t.getVdNm() + ", 공급가=" + t.getSupplyAmt() + ", 세액=" + t.getVatAmt(), t.getCompCd(), user.usrId());
        // 국세청 승인번호(24자리) 회신(시뮬) + 발행일자
        String approvalNo = LocalDate.now().toString().replace("-", "") + String.format("%016d", id);  // 24자리
        taxMapper.updateIssued(id, "국세청 승인(시뮬)", approvalNo, LocalDate.now().toString(), user.usrId());
    }

    @Transactional
    public void cancel(Long id, LoginUser user) {
        TxTaxbill t = taxMapper.findById(id);
        if (t == null) return;
        taxMapper.updateStatus(id, "CANCEL", null, user.usrId());
    }
}
