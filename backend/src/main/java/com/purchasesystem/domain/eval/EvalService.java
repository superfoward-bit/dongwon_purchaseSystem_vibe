package com.purchasesystem.domain.eval;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.eval.mapper.EvalMapper;
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
public class EvalService {

    private static final String DOC_TYP = "EV";
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final EvalMapper evalMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;

    // ===== 평가시트 =====
    public List<VdEvalSheet> getSheets() { return evalMapper.findSheets(); }

    public VdEvalSheet getSheetDetail(String sheetCd) {
        VdEvalSheet s = evalMapper.findSheetByCd(sheetCd);
        if (s == null) throw new BusinessException("평가시트를 찾을 수 없습니다.");
        s.setItems(evalMapper.findSheetItems(sheetCd));
        s.setCategories(evalMapper.findSheetCates(sheetCd));
        return s;
    }

    public List<VdEvalGrade> getGrades() { return evalMapper.findGrades(); }

    @Transactional
    public void createSheet(VdEvalSheet s, LoginUser user) {
        if (evalMapper.countSheet(s.getSheetCd()) > 0) throw new BusinessException("이미 존재하는 시트코드입니다: " + s.getSheetCd());
        validateWeight(s);
        s.setRegId(user.usrId());
        evalMapper.insertSheet(s);
        saveSheetCatesItems(s);
    }

    @Transactional
    public void updateSheet(Long id, VdEvalSheet s, LoginUser user) {
        validateWeight(s);
        s.setId(id);
        s.setModId(user.usrId());
        evalMapper.updateSheet(s);
        evalMapper.deleteSheetItems(s.getSheetCd());
        evalMapper.deleteSheetCates(s.getSheetCd());
        saveSheetCatesItems(s);
    }

    @Transactional
    public void deleteSheet(Long id, String sheetCd, LoginUser user) {
        if (sheetCd != null) { evalMapper.deleteSheetItems(sheetCd); evalMapper.deleteSheetCates(sheetCd); }
        evalMapper.deleteSheet(id, user.usrId());
    }

    // ===== 평가 실행 =====
    public List<VdEval> getEvalList(String keyword, String sts) { return evalMapper.findEvalList(keyword, sts); }

    public VdEval getEvalDetail(Long id) {
        VdEval e = evalMapper.findEvalById(id);
        if (e == null) throw new BusinessException("평가를 찾을 수 없습니다.");
        e.setResults(evalMapper.findResults(id));
        e.setActions(statusService.availableActions(DOC_TYP, e.getSts()));
        e.setHistory(statusService.history(DOC_TYP, id));
        return e;
    }

    /** 평가 시작: 협력사+시트 → 항목별 결과행 생성(점수 0) */
    @Transactional
    public Long create(VdEval e, LoginUser user) {
        if (e.getVdCd() == null) throw new BusinessException("협력사를 선택하세요.");
        if (e.getSheetCd() == null) throw new BusinessException("평가시트를 선택하세요.");
        VdEvalSheet sheet = evalMapper.findSheetByCd(e.getSheetCd());
        if (sheet == null) throw new BusinessException("평가시트를 찾을 수 없습니다.");
        List<VdEvalSheetItem> items = evalMapper.findSheetItems(e.getSheetCd());
        if (items.isEmpty()) throw new BusinessException("평가시트에 항목이 없습니다.");

        e.setCompCd(user.compCd());
        e.setSheetNm(sheet.getSheetNm());
        e.setEvaluatorId(user.usrId());
        // 협력사 세그먼트 자동 배정(등급행렬 적용 기준)
        e.setSegCd(evalMapper.findVendorSeg(e.getVdCd()));
        e.setSegNm(evalMapper.findVendorSegNm(e.getVdCd()));
        e.setSts("ING");
        e.setRegId(user.usrId());
        e.setEvalNo(docNoService.generate(DOC_TYP));
        evalMapper.insertEval(e);

        for (VdEvalSheetItem it : items) {
            VdEvalResult r = new VdEvalResult();
            r.setEvalId(e.getId());
            r.setSheetItemSeq(it.getItemSeq());
            r.setEvalItemNm(it.getEvalItemNm());
            r.setCateSeq(it.getCateSeq());
            r.setCateNm(it.getCateNm());
            r.setWeight(it.getWeight());
            r.setScore(BigDecimal.ZERO);
            r.setWeightedScore(BigDecimal.ZERO);
            evalMapper.insertResult(r);
        }
        return e.getId();
    }

    /** 점수 입력/저장 → 가중점수·총점·등급 즉시 산출 (1단계 평탄 / 2단계 카테고리 가중) */
    @Transactional
    public void saveScores(Long evalId, List<Map<String, Object>> scores, LoginUser user) {
        VdEval e = evalMapper.findEvalById(evalId);
        if (e == null) throw new BusinessException("평가를 찾을 수 없습니다.");
        if (!"ING".equals(e.getSts())) throw new BusinessException("평가중 상태에서만 점수를 입력할 수 있습니다.");

        List<VdEvalResult> results = evalMapper.findResults(evalId);
        // 입력 점수/의견 적용
        for (VdEvalResult r : results) {
            Map<String, Object> in = scores.stream()
                    .filter(m -> String.valueOf(m.get("sheetItemSeq")).equals(String.valueOf(r.getSheetItemSeq())))
                    .findFirst().orElse(null);
            r.setScore(in != null && in.get("score") != null ? new BigDecimal(String.valueOf(in.get("score"))) : BigDecimal.ZERO);
            r.setOpinion(in != null ? (String) in.get("opinion") : r.getOpinion());
            if (r.getWeight() == null) r.setWeight(BigDecimal.ZERO);
        }

        BigDecimal tot = BigDecimal.ZERO;
        boolean twoLevel = "Y".equals(e.getUseCateYn());
        if (twoLevel) {
            // 카테고리별 가중평균 → 카테고리배점 적용
            Map<Integer, BigDecimal> cateWeight = new java.util.HashMap<>();
            for (VdEvalCate c : evalMapper.findSheetCates(e.getSheetCd()))
                cateWeight.put(c.getCateSeq(), c.getWeight() == null ? BigDecimal.ZERO : c.getWeight());
            Map<Integer, List<VdEvalResult>> byCate = results.stream()
                    .collect(java.util.stream.Collectors.groupingBy(r -> r.getCateSeq() == null ? 0 : r.getCateSeq()));
            for (Map.Entry<Integer, List<VdEvalResult>> en : byCate.entrySet()) {
                List<VdEvalResult> rs = en.getValue();
                BigDecimal sumW = rs.stream().map(VdEvalResult::getWeight).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal cw = cateWeight.getOrDefault(en.getKey(), BigDecimal.ZERO);
                for (VdEvalResult r : rs) {
                    // 총점 기여도 = 점수 × (항목배점/카테고리내합) × (카테고리배점/100)
                    BigDecimal contrib = sumW.signum() == 0 ? BigDecimal.ZERO
                            : r.getScore().multiply(r.getWeight()).divide(sumW, 6, RoundingMode.HALF_UP)
                              .multiply(cw).divide(HUNDRED, 2, RoundingMode.HALF_UP);
                    r.setWeightedScore(contrib);
                    tot = tot.add(contrib);
                }
            }
        } else {
            for (VdEvalResult r : results) {
                BigDecimal weighted = r.getScore().multiply(r.getWeight()).divide(HUNDRED, 2, RoundingMode.HALF_UP);
                r.setWeightedScore(weighted);
                tot = tot.add(weighted);
            }
        }
        for (VdEvalResult r : results)
            evalMapper.updateResult(r.getId(), r.getScore(), r.getWeightedScore(), r.getOpinion());

        // 세그먼트별 등급행렬 우선 적용
        VdEvalGrade grade = evalMapper.findGradeBySegScore(e.getSegCd(), tot);
        evalMapper.updateEvalScore(evalId, tot,
                grade != null ? grade.getGradeCd() : null, grade != null ? grade.getGradeNm() : null);
    }

    /** 상태전이: 평가완료(등급 협력사 반영)/재평가/취소 */
    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        VdEval cur = evalMapper.findEvalById(id);
        if (cur == null) throw new BusinessException("평가를 찾을 수 없습니다.");
        if ("COMPLETE".equals(actionCode) && (cur.getGradeCd() == null)) {
            throw new BusinessException("점수를 입력/저장하여 등급이 산출된 후 완료할 수 있습니다.");
        }
        String toSts = statusService.transition(DOC_TYP, id, cur.getEvalNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        evalMapper.updateStatus(id, toSts, user.usrId());
        if ("COMPLETE".equals(actionCode)) {
            evalMapper.updateVendorGrade(cur.getVdCd(), cur.getGradeCd(), cur.getGradeNm());
        }
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        VdEval cur = evalMapper.findEvalById(id);
        if (cur == null) return;
        if (!"ING".equals(cur.getSts())) throw new BusinessException("평가중 상태에서만 삭제할 수 있습니다.");
        evalMapper.deleteResults(id);
        evalMapper.deleteEval(id, user.usrId());
    }

    // ----- 내부 -----
    private void validateWeight(VdEvalSheet s) {
        if (s.getItems() == null || s.getItems().isEmpty()) throw new BusinessException("평가항목을 1개 이상 입력하세요.");
        if ("Y".equals(s.getUseCateYn())) {
            // 2단계: 카테고리 배점 합100 + 카테고리별 항목 배점 합100
            if (s.getCategories() == null || s.getCategories().isEmpty()) throw new BusinessException("카테고리를 1개 이상 입력하세요.");
            BigDecimal cateSum = s.getCategories().stream().map(c -> nvl(c.getWeight())).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (cateSum.compareTo(HUNDRED) != 0) throw new BusinessException("카테고리 배점 합계가 100이어야 합니다. (현재 " + cateSum + ")");
            for (int seq = 1; seq <= s.getCategories().size(); seq++) {
                final int cs = seq;
                BigDecimal itemSum = s.getItems().stream().filter(i -> i.getCateSeq() != null && i.getCateSeq() == cs)
                        .map(i -> nvl(i.getWeight())).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (itemSum.signum() == 0) throw new BusinessException(cs + "번 카테고리에 항목이 없습니다.");
                if (itemSum.compareTo(HUNDRED) != 0)
                    throw new BusinessException(cs + "번 카테고리 내 항목 배점 합계가 100이어야 합니다. (현재 " + itemSum + ")");
            }
        } else {
            BigDecimal sum = s.getItems().stream().map(i -> nvl(i.getWeight())).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.compareTo(HUNDRED) != 0) throw new BusinessException("배점 합계가 100이어야 합니다. (현재 " + sum + ")");
        }
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private void saveSheetCatesItems(VdEvalSheet s) {
        if ("Y".equals(s.getUseCateYn()) && s.getCategories() != null) {
            int cseq = 1;
            for (VdEvalCate c : s.getCategories()) {
                c.setSheetCd(s.getSheetCd());
                c.setCateSeq(cseq++);
                evalMapper.insertSheetCate(c);
            }
        }
        int seq = 1;
        for (VdEvalSheetItem it : s.getItems()) {
            it.setSheetCd(s.getSheetCd());
            it.setItemSeq(seq++);
            if (!"Y".equals(s.getUseCateYn())) it.setCateSeq(null);
            evalMapper.insertSheetItem(it);
        }
    }
}
