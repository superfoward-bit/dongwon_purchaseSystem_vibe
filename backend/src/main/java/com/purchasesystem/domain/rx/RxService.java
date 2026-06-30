package com.purchasesystem.domain.rx;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.rx.mapper.RxMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RxService {

    private static final String DOC_TYP = "RX";

    private final RxMapper rxMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;

    public List<RxRfx> getList(String keyword, String sts) {
        return rxMapper.findList(keyword, sts);
    }

    public RxRfx getDetail(Long id) {
        RxRfx r = rxMapper.findById(id);
        if (r == null) throw new BusinessException("견적을 찾을 수 없습니다.");
        r.setItems(rxMapper.findItems(id));
        r.setVendors(rxMapper.findVendors(id));
        r.setQuoteItems(rxMapper.findQuoteItems(id));
        r.setActions(statusService.availableActions(DOC_TYP, r.getSts()));
        r.setHistory(statusService.history(DOC_TYP, id));
        return r;
    }

    @Transactional
    public Long create(RxRfx r, LoginUser user) {
        if (r.getItems() == null || r.getItems().isEmpty()) throw new BusinessException("견적 품목을 입력하세요.");
        if (r.getVendors() == null || r.getVendors().isEmpty()) throw new BusinessException("초대 협력사를 1개 이상 선택하세요.");
        r.setCompCd(user.compCd());
        r.setChrgUsrId(user.usrId());
        r.setSts("TMP");
        r.setRegId(user.usrId());
        r.setRfxNo(docNoService.generate(DOC_TYP));
        rxMapper.insertRfx(r);
        saveItemsAndVendors(r);
        return r.getId();
    }

    @Transactional
    public void update(Long id, RxRfx r, LoginUser user) {
        RxRfx cur = rxMapper.findById(id);
        if (cur == null) throw new BusinessException("견적을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        r.setId(id);
        r.setModId(user.usrId());
        rxMapper.updateRfx(r);
        rxMapper.deleteItems(id);
        rxMapper.deleteVendors(id);
        saveItemsAndVendors(r);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        RxRfx cur = rxMapper.findById(id);
        if (cur == null) throw new BusinessException("견적을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getRfxNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        rxMapper.updateStatus(id, toSts, user.usrId());
        if ("CANCEL_SEL".equals(actionCode)) {   // 선정취소 → 선정정보 초기화
            rxMapper.clearVendorSel(id);
            rxMapper.updateSelectedVendor(id, null, null, user.usrId());
        }
        return toSts;
    }

    /** 재공고: 유찰/취소된 견적을 신규 견적으로 복제(품목·초대협력사 승계, TMP) */
    @Transactional
    public Long reannounce(Long failedId, LoginUser user) {
        RxRfx src = rxMapper.findById(failedId);
        if (src == null) throw new BusinessException("견적을 찾을 수 없습니다.");
        if (!List.of("FAIL", "CANCEL").contains(src.getSts()))
            throw new BusinessException("유찰 또는 취소된 견적만 재공고할 수 있습니다.");
        src.setItems(rxMapper.findItems(failedId));
        src.setVendors(rxMapper.findVendors(failedId));
        src.setId(null);
        src.setSts("TMP");
        src.setRfxTitle("[재공고] " + src.getRfxTitle());
        src.setSelVdCd(null);
        src.setSelVdNm(null);
        src.setChrgUsrId(user.usrId());
        src.setRegId(user.usrId());
        src.setRfxNo(docNoService.generate(DOC_TYP));
        rxMapper.insertRfx(src);
        saveItemsAndVendors(src);
        return src.getId();
    }

    /** 협력사 견적 응답 입력 (구매담당이 대행 입력) */
    @Transactional
    public void saveQuote(Long rfxId, String vdCd, List<RxQuoteItem> quoteItems, LoginUser user) {
        RxRfx cur = rxMapper.findById(rfxId);
        if (cur == null) throw new BusinessException("견적을 찾을 수 없습니다.");
        if (!"OPEN".equals(cur.getSts())) throw new BusinessException("공고중 상태에서만 견적을 입력할 수 있습니다.");

        Map<Long, BigDecimal> qtyMap = new HashMap<>();
        for (RxItem it : rxMapper.findItems(rfxId)) {
            qtyMap.put(it.getId(), it.getQty() == null ? BigDecimal.ZERO : it.getQty());
        }

        rxMapper.deleteQuoteItems(rfxId, vdCd);
        BigDecimal tot = BigDecimal.ZERO;
        for (RxQuoteItem q : quoteItems) {
            BigDecimal prc = q.getOfferPrc() == null ? BigDecimal.ZERO : q.getOfferPrc();
            BigDecimal qty = qtyMap.getOrDefault(q.getRfxItemId(), BigDecimal.ZERO);
            BigDecimal amt = prc.multiply(qty);
            q.setRfxId(rfxId);
            q.setVdCd(vdCd);
            q.setOfferAmt(amt);
            rxMapper.insertQuoteItem(q);
            tot = tot.add(amt);
        }
        rxMapper.updateVendorResp(rfxId, vdCd, tot);
    }

    /** 협력사 선정 (공고중 + 응답건만) → 상태 SEL */
    @Transactional
    public String selectVendor(Long rfxId, String vdCd, LoginUser user) {
        RxRfx cur = rxMapper.findById(rfxId);
        if (cur == null) throw new BusinessException("견적을 찾을 수 없습니다.");
        RxVendor target = rxMapper.findVendors(rfxId).stream()
                .filter(v -> v.getVdCd().equals(vdCd)).findFirst()
                .orElseThrow(() -> new BusinessException("초대 협력사가 아닙니다."));
        if (!"Y".equals(target.getRespYn())) throw new BusinessException("견적 미응답 협력사는 선정할 수 없습니다.");

        rxMapper.clearVendorSel(rfxId);
        rxMapper.setVendorSel(rfxId, vdCd);
        rxMapper.updateSelectedVendor(rfxId, vdCd, target.getVdNm(), user.usrId());
        String toSts = statusService.transition(DOC_TYP, rfxId, cur.getRfxNo(), cur.getSts(),
                "SELECT", null, user.usrId());
        rxMapper.updateStatus(rfxId, toSts, user.usrId());
        return toSts;
    }

    /** 선정결과 → 발주 생성용 소스(선정협력사 + 견적단가) */
    public Map<String, Object> getPoSource(Long rfxId) {
        RxRfx r = rxMapper.findById(rfxId);
        if (r == null || r.getSelVdCd() == null) throw new BusinessException("선정된 견적이 아닙니다.");
        Map<Long, RxQuoteItem> offerMap = new HashMap<>();
        for (RxQuoteItem q : rxMapper.findQuoteItems(rfxId)) {
            if (q.getVdCd().equals(r.getSelVdCd())) offerMap.put(q.getRfxItemId(), q);
        }
        List<Map<String, Object>> items = rxMapper.findItems(rfxId).stream().map(it -> {
            Map<String, Object> m = new HashMap<>();
            RxQuoteItem off = offerMap.get(it.getId());
            m.put("prItemId", it.getPrItemId());
            m.put("itemCd", it.getItemCd());
            m.put("itemNm", it.getItemNm());
            m.put("spec", it.getSpec());
            m.put("unitCd", it.getUnitCd());
            m.put("qty", it.getQty());
            m.put("prc", off != null ? off.getOfferPrc() : it.getBasePrc());
            return m;
        }).toList();
        Map<String, Object> src = new HashMap<>();
        src.put("vdCd", r.getSelVdCd());
        src.put("vdNm", r.getSelVdNm());
        src.put("poTitle", r.getRfxTitle());
        src.put("srcTyp", "RFX");
        src.put("srcId", r.getId());
        src.put("items", items);
        return src;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        RxRfx cur = rxMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        rxMapper.deleteItems(id);
        rxMapper.deleteVendors(id);
        rxMapper.deleteRfx(id, user.usrId());
    }

    private void saveItemsAndVendors(RxRfx r) {
        int line = 1;
        for (RxItem it : r.getItems()) {
            it.setRfxId(r.getId());
            it.setLineNo(line++);
            rxMapper.insertItem(it);
        }
        for (RxVendor v : r.getVendors()) {
            v.setRfxId(r.getId());
            rxMapper.insertVendor(v);
        }
    }
}
