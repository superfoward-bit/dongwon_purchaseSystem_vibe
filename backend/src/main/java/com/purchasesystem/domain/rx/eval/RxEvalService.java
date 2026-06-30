package com.purchasesystem.domain.rx.eval;

import com.purchasesystem.domain.rx.eval.mapper.RxEvalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * RFP 평가입찰: 비가격 가중평가 + 가격점수(MR 최저가비율 / LN 선형) 자동계산으로 종합점수·순위 산출.
 */
@Service
@RequiredArgsConstructor
public class RxEvalService {

    private final RxEvalMapper evalMapper;

    public RxEvalDtos.Setup getSetup(Long rfxId) {
        RxEvalDtos.Setup s = new RxEvalDtos.Setup();
        Map<String, Object> cfg = evalMapper.findEvalCfg(rfxId);
        if (cfg != null) {
            s.setEvalYn(str(cfg.get("evalYn")));
            s.setPriceWeight(num(cfg.get("priceWeight")));
            s.setPriceMethod(str(cfg.get("priceMethod")));
        }
        s.setCriteria(evalMapper.findCriteria(rfxId));
        s.setEvaluators(evalMapper.findEvaluators(rfxId));
        return s;
    }

    @Transactional
    public void saveSetup(Long rfxId, RxEvalDtos.Setup s) {
        evalMapper.updateEvalCfg(rfxId, s.getEvalYn(), s.getPriceWeight(), s.getPriceMethod());
        evalMapper.deleteCriteria(rfxId);
        int line = 1;
        if (s.getCriteria() != null) for (RxEvalCriteria c : s.getCriteria()) {
            c.setRfxId(rfxId);
            c.setLineNo(line++);
            evalMapper.insertCriteria(c);
        }
        evalMapper.deleteEvaluators(rfxId);
        if (s.getEvaluators() != null) for (RxEvaluator e : s.getEvaluators()) {
            e.setRfxId(rfxId);
            evalMapper.insertEvaluator(e);
        }
    }

    public List<RxEvalScore> getScores(Long rfxId) { return evalMapper.findScores(rfxId); }

    @Transactional
    public void saveScores(Long rfxId, List<RxEvalScore> scores) {
        evalMapper.deleteScores(rfxId);
        if (scores != null) for (RxEvalScore sc : scores) {
            if (sc.getScore() == null) continue;
            sc.setRfxId(rfxId);
            evalMapper.insertScore(sc);
        }
    }

    /** 종합점수/순위 자동계산 */
    public List<RxEvalDtos.Result> computeResult(Long rfxId) {
        Map<String, Object> cfg = evalMapper.findEvalCfg(rfxId);
        BigDecimal priceWeight = cfg == null ? BigDecimal.ZERO : num(cfg.get("priceWeight"));
        String method = cfg == null ? "MR" : str(cfg.get("priceMethod"));
        if (method == null) method = "MR";

        List<RxEvalCriteria> criteria = evalMapper.findCriteria(rfxId);
        List<RxEvalScore> scores = evalMapper.findScores(rfxId);
        List<RxEvalDtos.VendorQuote> quotes = evalMapper.findVendorQuotes(rfxId);

        // 비가격 항목별 점수 합계/건수 인덱스: key = vdCd|criteriaId
        Map<String, BigDecimal[]> sum = new HashMap<>();   // [합계, 건수]
        for (RxEvalScore s : scores) {
            String k = s.getVdCd() + "|" + s.getCriteriaId();
            BigDecimal[] v = sum.computeIfAbsent(k, x -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            v[0] = v[0].add(s.getScore() == null ? BigDecimal.ZERO : s.getScore());
            v[1] = v[1].add(BigDecimal.ONE);
        }

        // 가격 min/max (응찰=quoteAmt>0)
        BigDecimal minAmt = null, maxAmt = null;
        for (RxEvalDtos.VendorQuote q : quotes) {
            BigDecimal a = q.getQuoteAmt();
            if (a == null || a.signum() <= 0) continue;
            if (minAmt == null || a.compareTo(minAmt) < 0) minAmt = a;
            if (maxAmt == null || a.compareTo(maxAmt) > 0) maxAmt = a;
        }

        List<RxEvalDtos.Result> results = new ArrayList<>();
        for (RxEvalDtos.VendorQuote q : quotes) {
            RxEvalDtos.Result r = new RxEvalDtos.Result();
            r.setVdCd(q.getVdCd());
            r.setVdNm(q.getVdNm());
            r.setQuoteAmt(q.getQuoteAmt());

            // 비가격 가중점수
            BigDecimal nonPrice = BigDecimal.ZERO;
            for (RxEvalCriteria c : criteria) {
                BigDecimal[] v = sum.get(q.getVdCd() + "|" + c.getId());
                BigDecimal avg = (v == null || v[1].signum() == 0) ? BigDecimal.ZERO
                        : v[0].divide(v[1], 4, RoundingMode.HALF_UP);  // 0~100
                BigDecimal w = c.getWeight() == null ? BigDecimal.ZERO : c.getWeight();
                nonPrice = nonPrice.add(avg.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP).multiply(w));
            }
            r.setNonPriceScore(nonPrice.setScale(2, RoundingMode.HALF_UP));

            // 가격점수
            BigDecimal price = BigDecimal.ZERO;
            BigDecimal a = q.getQuoteAmt();
            if (a != null && a.signum() > 0 && minAmt != null && priceWeight.signum() > 0) {
                if ("LN".equals(method)) {
                    if (maxAmt.compareTo(minAmt) == 0) price = priceWeight;
                    else price = maxAmt.subtract(a).divide(maxAmt.subtract(minAmt), 6, RoundingMode.HALF_UP).multiply(priceWeight);
                } else { // MR
                    price = minAmt.divide(a, 6, RoundingMode.HALF_UP).multiply(priceWeight);
                }
            }
            r.setPriceScore(price.setScale(2, RoundingMode.HALF_UP));
            r.setTotalScore(r.getNonPriceScore().add(r.getPriceScore()).setScale(2, RoundingMode.HALF_UP));
            results.add(r);
        }

        results.sort((x, y) -> y.getTotalScore().compareTo(x.getTotalScore()));
        int rank = 1;
        for (RxEvalDtos.Result r : results) r.setRankNo(rank++);
        return results;
    }

    private String str(Object o) { return o == null ? null : o.toString(); }
    private BigDecimal num(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal b) return b;
        return new BigDecimal(o.toString());
    }
}
