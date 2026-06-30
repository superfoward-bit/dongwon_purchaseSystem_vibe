package com.purchasesystem.domain.slip;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.cl.ClClose;
import com.purchasesystem.domain.cl.mapper.ClMapper;
import com.purchasesystem.domain.iface.IfService;
import com.purchasesystem.domain.slip.mapper.SlipMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlipService {

    private final SlipMapper slipMapper;
    private final ClMapper clMapper;
    private final DocNoService docNoService;
    private final IfService ifService;

    public List<AcSlip> getList(String keyword, String slipSts) { return slipMapper.findList(keyword, slipSts); }
    public AcSlip getDetail(Long id) {
        AcSlip s = slipMapper.findById(id);
        if (s == null) throw new BusinessException("전표를 찾을 수 없습니다.");
        s.setLines(slipMapper.findLines(id));
        return s;
    }

    /** 확정 정산마감에서 매입전표 생성 — 복식부기 다라인
     *  (차) 원재료 공급가 + (차) 부가세대급금 세액 / (대) 외상매입금 총액 */
    @Transactional
    public Long createFromClose(Long closeId, LoginUser user) {
        ClClose c = clMapper.findById(closeId);
        if (c == null) throw new BusinessException("정산마감을 찾을 수 없습니다.");
        if (!"CFM".equals(c.getSts())) throw new BusinessException("마감확정된 정산만 전표를 생성할 수 있습니다.");
        if (slipMapper.countBySrc("CL", closeId) > 0) throw new BusinessException("이미 전표가 생성된 정산입니다.");

        BigDecimal supl = nvl(c.getTotSuplAmt());
        BigDecimal vat = nvl(c.getTotVatAmt());
        BigDecimal payable = c.getNetAmt() != null ? c.getNetAmt() : nvl(c.getTotAmt());

        AcSlip s = new AcSlip();
        s.setCompCd(user.compCd());
        s.setSlipNo(docNoService.generate("SL"));
        s.setSlipTyp("PURCHASE");
        s.setSrcTyp("CL");
        s.setSrcId(closeId);
        s.setSrcNo(c.getCloseNo());
        s.setVdCd(c.getVdCd());
        s.setVdNm(c.getVdNm());
        s.setPostingYmd(LocalDate.now().toString());
        s.setFiscalYm(c.getCloseYm());
        // 헤더 요약(하위호환): 대표 차/대변 + 총액
        s.setDrAcct("원재료(매입)");
        s.setCrAcct("외상매입금");
        s.setAmt(payable);
        s.setRemark("정산 " + c.getCloseNo() + " 매입전표");
        s.setRegId(user.usrId());
        slipMapper.insert(s);

        // 복식부기 명세
        int line = 1;
        addLine(s.getId(), line++, "D", "14900", "원재료", supl, "공급가", user);
        if (vat.signum() != 0) addLine(s.getId(), line++, "D", "13500", "부가세대급금", vat, "매입세액", user);
        // 조정(장려금/수수료 등)으로 지급액이 공급가+세액과 다르면 조정 차변 라인
        BigDecimal adj = payable.subtract(supl.add(vat));
        if (adj.signum() != 0) addLine(s.getId(), line++, "D", "53500", "매입할인·조정", adj, "갭정산 반영", user);
        addLine(s.getId(), line, "C", "25100", "외상매입금", payable, "지급채무", user);
        return s.getId();
    }

    private void addLine(Long slipId, int lineNo, String drCr, String acctCd, String acctNm,
                         BigDecimal amt, String remark, LoginUser user) {
        AcSlipLine l = new AcSlipLine();
        l.setSlipId(slipId); l.setLineNo(lineNo); l.setDrCr(drCr);
        l.setAcctCd(acctCd); l.setAcctNm(acctNm); l.setAmt(amt); l.setRemark(remark);
        l.setRegId(user.usrId());
        slipMapper.insertLine(l);
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    /** 전기(확정) */
    @Transactional
    public void post(Long id, LoginUser user) {
        AcSlip s = slipMapper.findById(id);
        if (s == null) throw new BusinessException("전표를 찾을 수 없습니다.");
        if (!"DRAFT".equals(s.getSlipSts())) throw new BusinessException("작성 상태에서만 전기할 수 있습니다.");
        slipMapper.updateStatus(id, "POSTED", user.usrId());
    }

    /** SAP 전송(IF 시뮬) */
    @Transactional
    public void sendSap(Long id, LoginUser user) {
        AcSlip s = slipMapper.findById(id);
        if (s == null) throw new BusinessException("전표를 찾을 수 없습니다.");
        if (!"POSTED".equals(s.getSlipSts())) throw new BusinessException("전기된 전표만 전송할 수 있습니다.");
        ifService.send("SLIP", "회계전표 전송", "SAP", "SL", id, s.getSlipNo(),
                "차변=" + s.getDrAcct() + ", 대변=" + s.getCrAcct() + ", 금액=" + s.getAmt(), s.getCompCd(), user.usrId());
        // SAP 전표번호 회신(시뮬) 저장 — 역분개·대사 추적용
        String erpDocNo = "51" + String.format("%08d", id);
        slipMapper.updateSent(id, erpDocNo, user.usrId());
    }

    @Transactional
    public void cancel(Long id, LoginUser user) {
        AcSlip s = slipMapper.findById(id);
        if (s == null) return;
        slipMapper.updateStatus(id, "CANCEL", user.usrId());
    }
}
