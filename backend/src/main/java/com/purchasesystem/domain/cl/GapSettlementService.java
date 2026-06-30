package com.purchasesystem.domain.cl;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.cl.mapper.ClAdjMapper;
import com.purchasesystem.domain.cl.mapper.ClMapper;
import com.purchasesystem.domain.pa.mapper.PaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 갭정산: 정산마감 기초금액에 물류수수료·피킹료·장려금 등 조정율을 자동집계.
 * 최종 정산액(NET_AMT) = 기초정산액(TOT_AMT) + 조정합계(ADJ_AMT).
 */
@Service
@RequiredArgsConstructor
public class GapSettlementService {

    private final ClMapper clMapper;
    private final ClAdjMapper adjMapper;
    private final PaMapper paMapper;

    public List<ClCloseAdj> getAdjs(Long closeId) { return adjMapper.findAdjs(closeId); }

    /** 정산마감의 조정명세를 재계산(자동집계)하고 헤더 금액을 갱신. */
    @Transactional
    public BigDecimal recalc(Long closeId, String actorId) {
        ClClose close = clMapper.findById(closeId);
        if (close == null) throw new BusinessException("정산마감을 찾을 수 없습니다.");
        if ("CFM".equals(close.getSts())) throw new BusinessException("마감확정된 정산은 재계산할 수 없습니다.");

        String firstDay = firstDayOf(close.getCloseYm());
        List<ClAdjRate> rates = adjMapper.findActiveRates(close.getCompCd(), close.getVdCd(), firstDay);

        adjMapper.deleteAdjs(closeId);

        BigDecimal supl = nvl(close.getTotSuplAmt());
        BigDecimal cnt = BigDecimal.valueOf(close.getGrCnt() == null ? 0 : close.getGrCnt());
        BigDecimal adjTot = BigDecimal.ZERO;
        int line = 1;

        for (ClAdjRate r : rates) {
            BigDecimal base;
            BigDecimal amt;
            if ("CNT".equals(r.getCalcBase())) {
                base = cnt;
                amt = cnt.multiply(nvl(r.getUnitAmt()));
            } else if ("FIXED".equals(r.getCalcBase())) {
                base = BigDecimal.ONE;
                amt = nvl(r.getUnitAmt());
            } else { // SUPL
                base = supl;
                amt = supl.multiply(nvl(r.getRate()));
            }
            amt = amt.setScale(0, RoundingMode.HALF_UP);
            BigDecimal signed = "-".equals(r.getSignTyp()) ? amt.negate() : amt;

            ClCloseAdj a = new ClCloseAdj();
            a.setCloseId(closeId);
            a.setLineNo(line++);
            a.setAdjTyp(r.getAdjTyp());
            a.setCalcBase(r.getCalcBase());
            a.setBaseAmt(base);
            a.setRate(nvl(r.getRate()));
            a.setUnitAmt(nvl(r.getUnitAmt()));
            a.setSignTyp(r.getSignTyp());
            a.setAdjAmt(signed);
            a.setRemark(r.getRemark());
            adjMapper.insertAdj(a);

            adjTot = adjTot.add(signed);
        }

        // 확정 매입조정(PA) 반영 — 협력사+정산월 기준 합계(±)를 조정 라인으로 추가
        BigDecimal paAmt = nvl(paMapper.sumConfirmed(close.getCompCd(), close.getVdCd(), close.getCloseYm()));
        if (paAmt.signum() != 0) {
            ClCloseAdj pa = new ClCloseAdj();
            pa.setCloseId(closeId);
            pa.setLineNo(line++);
            pa.setAdjTyp("PURC_ADJ");
            pa.setCalcBase("FIXED");
            pa.setBaseAmt(paAmt.abs());
            pa.setRate(BigDecimal.ZERO);
            pa.setUnitAmt(paAmt.abs());
            pa.setSignTyp(paAmt.signum() < 0 ? "-" : "+");
            pa.setAdjAmt(paAmt);
            pa.setRemark("확정 매입조정 반영");
            adjMapper.insertAdj(pa);
            adjTot = adjTot.add(paAmt);
        }

        BigDecimal netAmt = nvl(close.getTotAmt()).add(adjTot);
        adjMapper.updateCloseAmounts(closeId, adjTot, netAmt, actorId);
        return netAmt;
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private String firstDayOf(String ym) {
        // YYYYMM -> YYYY-MM-01
        if (ym == null || ym.length() != 6) throw new BusinessException("마감년월(YYYYMM)이 올바르지 않습니다.");
        return ym.substring(0, 4) + "-" + ym.substring(4, 6) + "-01";
    }
}
