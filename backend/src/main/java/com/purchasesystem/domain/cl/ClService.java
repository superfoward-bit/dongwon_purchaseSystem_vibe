package com.purchasesystem.domain.cl;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.cl.mapper.ClMapper;
import com.purchasesystem.domain.vendor.VdVendor;
import com.purchasesystem.domain.vendor.mapper.VendorMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClService {

    private static final String DOC_TYP = "CL";

    private final ClMapper clMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;
    private final GapSettlementService gapService;
    private final VendorMapper vendorMapper;

    public List<ClClose> getList(String keyword, String sts, String closeYm) {
        return clMapper.findList(keyword, sts, closeYm);
    }

    public ClClose getDetail(Long id) {
        ClClose c = clMapper.findById(id);
        if (c == null) throw new BusinessException("정산마감을 찾을 수 없습니다.");
        c.setGrs(clMapper.findDetailGrs(id));
        c.setAdjs(gapService.getAdjs(id));
        c.setActions(statusService.availableActions(DOC_TYP, c.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction()))
                .toList());
        c.setHistory(statusService.history(DOC_TYP, id));
        c.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return c;
    }

    /** 상신: 결재선 지정 → 결재중(REQ) + 결재요청 생성 */
    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        ClClose cur = clMapper.findById(id);
        if (cur == null) throw new BusinessException("정산마감을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getCloseNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        clMapper.updateStatus(id, toSts, "N", user.usrId());
        approvalService.create(DOC_TYP, id, cur.getCloseNo(), "정산마감 " + cur.getCloseNo(), approvers, user);
    }

    /** 마감대상 입고 미리보기 (VAT 계산 포함) */
    public List<ClCloseGr> previewEligible(String vdCd, String closeYm) {
        List<ClCloseGr> grs = clMapper.findEligibleGrs(vdCd, closeYm);
        for (ClCloseGr g : grs) applyVat(g);
        return grs;
    }

    /** 정산마감 생성: 대상 입고를 모아 합계 산출 후 저장 */
    @Transactional
    public Long create(String vdCd, String vdNm, String closeYm, LoginUser user) {
        if (vdCd == null || vdCd.isBlank()) throw new BusinessException("협력사를 선택하세요.");
        if (closeYm == null || closeYm.length() != 6) throw new BusinessException("마감년월(YYYYMM)을 확인하세요.");

        List<ClCloseGr> grs = clMapper.findEligibleGrs(vdCd, closeYm);
        if (grs.isEmpty()) throw new BusinessException("마감대상 입고확정 건이 없습니다.");

        BigDecimal supl = BigDecimal.ZERO, vat = BigDecimal.ZERO, tot = BigDecimal.ZERO;
        for (ClCloseGr g : grs) {
            applyVat(g);
            supl = supl.add(g.getSuplAmt());
            vat = vat.add(g.getVatAmt());
            tot = tot.add(g.getAmt());
        }

        ClClose c = new ClClose();
        c.setCompCd(user.compCd());
        c.setCloseNo(docNoService.generate(DOC_TYP));
        c.setCloseYm(closeYm);
        c.setVdCd(vdCd);
        c.setVdNm(vdNm != null ? vdNm : (grs.get(0).getVdNm()));
        c.setTotSuplAmt(supl);
        c.setTotVatAmt(vat);
        c.setTotAmt(tot);
        c.setGrCnt(grs.size());
        // 결제조건 스냅샷(거래시점 박제) + 지급예정일 자동산출
        VdVendor pay = vendorMapper.findPayInfo(vdCd);
        if (pay != null) {
            c.setPayTermCd(pay.getPayTermCd());
            c.setPayMethodCd(pay.getPayMethodCd());
            c.setPayDueYmd(calcPayDueYmd(closeYm, pay.getPayTermCd()));
        }
        c.setRegId(user.usrId());
        clMapper.insertClose(c);

        for (ClCloseGr g : grs) {
            g.setCloseId(c.getId());
            clMapper.insertCloseGr(g);
        }
        // 갭정산: 물류수수료/피킹료/장려금 등 조정 자동집계
        gapService.recalc(c.getId(), user.usrId());
        return c.getId();
    }

    /** 갭정산 조정 재계산 */
    @Transactional
    public java.math.BigDecimal recalcGap(Long id, LoginUser user) {
        ClClose cur = clMapper.findById(id);
        if (cur == null) throw new BusinessException("정산마감을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 재계산할 수 있습니다.");
        return gapService.recalc(id, user.usrId());
    }

    /** 상태전이: 마감확정(잠금)/마감해제(되돌리기)/취소 */
    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        ClClose cur = clMapper.findById(id);
        if (cur == null) throw new BusinessException("정산마감을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getCloseNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        String lockYn = "CFM".equals(toSts) ? "Y" : "N";
        clMapper.updateStatus(id, toSts, lockYn, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        ClClose cur = clMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        clMapper.deleteCloseGrs(id);
        clMapper.deleteClose(id, user.usrId());
    }

    /** 결제조건 기반 지급예정일 산출. 기준 = 마감월(closeYm) 말일. */
    private String calcPayDueYmd(String closeYm, String payTermCd) {
        if (payTermCd == null || closeYm == null || closeYm.length() != 6) return null;
        int y = Integer.parseInt(closeYm.substring(0, 4));
        int m = Integer.parseInt(closeYm.substring(4, 6));
        LocalDate closeEnd = YearMonth.of(y, m).atEndOfMonth();   // 마감월 말일
        LocalDate due = switch (payTermCd) {
            case "IMMED" -> closeEnd;
            case "NET15" -> closeEnd.plusDays(15);
            case "NET30" -> closeEnd.plusDays(30);
            case "NET60" -> closeEnd.plusDays(60);
            case "EOM_NEXT" -> YearMonth.of(y, m).plusMonths(1).atEndOfMonth();   // 익월 말일
            default -> closeEnd;
        };
        return due.toString();
    }

    /** 합계 산출: 공급가/부가세는 입고 품목 과세구분별로 매퍼에서 이미 집계됨(면세/영세율 = 부가세 0). 여기선 합계만. */
    private void applyVat(ClCloseGr g) {
        BigDecimal supl = g.getSuplAmt() == null ? BigDecimal.ZERO : g.getSuplAmt();
        BigDecimal vat = g.getVatAmt() == null ? BigDecimal.ZERO : g.getVatAmt();
        g.setSuplAmt(supl);
        g.setVatAmt(vat);
        g.setAmt(supl.add(vat));
    }
}
