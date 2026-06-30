package com.purchasesystem.domain.mrp;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.mrp.mapper.MrpMapper;
import com.purchasesystem.domain.pr.PrItem;
import com.purchasesystem.domain.pr.PrRequest;
import com.purchasesystem.domain.pr.PrService;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MrpService {

    private final MrpMapper mrpMapper;
    private final PrService prService;

    public List<MrSuggest> getSuggests(String sts) { return mrpMapper.findSuggests(sts); }

    /** MRP 실행: 발주정책 품목별 (현재고+입고예정) vs 발주점 비교 → 부족분 발주제안 생성 */
    @Transactional
    public int runMrp(LoginUser user) {
        mrpMapper.clearSuggests(user.compCd());
        String today = LocalDate.now().toString();
        int cnt = 0;
        for (MrSuggest m : mrpMapper.findReorderItems(user.compCd())) {
            BigDecimal cur = nvl(m.getCurQty());
            BigDecimal onOrder = nvl(m.getOnOrderQty());
            BigDecimal avail = cur.add(onOrder);
            BigDecimal rop = nvl(m.getReorderPoint());
            if (avail.compareTo(rop) > 0) continue;   // 충분 → 발주 불필요

            BigDecimal safety = nvl(m.getSafetyStock());
            BigDecimal orderQty = nvl(m.getOrderQty());
            BigDecimal moq = nvl(m.getMoq());
            // 제안량: 고정발주량(있으면) 또는 (발주점+안전재고-가용). 최소 MOQ.
            BigDecimal suggest = orderQty.signum() > 0 ? orderQty : rop.add(safety).subtract(avail);
            if (suggest.compareTo(moq) < 0) suggest = moq;
            if (suggest.signum() <= 0) continue;

            MrSuggest s = new MrSuggest();
            s.setCompCd(user.compCd());
            s.setRunYmd(today);
            s.setItemCd(m.getItemCd()); s.setItemNm(m.getItemNm()); s.setUnitCd(m.getUnitCd());
            s.setCurQty(cur); s.setOnOrderQty(onOrder); s.setAvailQty(avail);
            s.setSafetyStock(safety); s.setReorderPoint(rop);
            s.setShortageQty(rop.subtract(avail).max(BigDecimal.ZERO));
            s.setSuggestQty(suggest);
            s.setRepVdCd(m.getRepVdCd()); s.setRepVdNm(m.getRepVdNm());
            s.setRegId(user.usrId());
            mrpMapper.insertSuggest(s);
            cnt++;
        }
        return cnt;
    }

    /** 선택한 발주제안 → 구매요청(PR) 1건 자동생성 */
    @Transactional
    public String createPr(List<Long> suggestIds, LoginUser user) {
        if (suggestIds == null || suggestIds.isEmpty()) throw new BusinessException("발주제안을 선택하세요.");
        List<MrSuggest> picked = new ArrayList<>();
        for (Long id : suggestIds) {
            MrSuggest s = mrpMapper.findById(id);
            if (s != null && "SUGGEST".equals(s.getSts())) picked.add(s);
        }
        if (picked.isEmpty()) throw new BusinessException("처리 가능한 제안이 없습니다.");

        PrRequest pr = new PrRequest();
        pr.setPrTitle("[MRP 자동발주] " + LocalDate.now());
        pr.setPrTyp("PRODUCT");
        pr.setReqYmd(LocalDate.now().toString());
        pr.setRemark("MRP 발주제안 자동생성 (" + picked.size() + "건)");
        List<PrItem> items = new ArrayList<>();
        for (MrSuggest s : picked) {
            PrItem it = new PrItem();
            it.setItemCd(s.getItemCd()); it.setItemNm(s.getItemNm()); it.setUnitCd(s.getUnitCd());
            it.setQty(s.getSuggestQty()); it.setEstPrc(BigDecimal.ZERO);
            it.setRemark(s.getRepVdNm() != null ? "대표협력사: " + s.getRepVdNm() : null);
            items.add(it);
        }
        pr.setItems(items);
        Long prId = prService.create(pr, user);
        String prNo = prService.getDetail(prId).getPrNo();
        for (MrSuggest s : picked) mrpMapper.updateOrdered(s.getId(), prNo, user.usrId());
        return prNo;
    }

    @Transactional
    public void ignore(Long id, LoginUser user) { mrpMapper.updateStatus(id, "IGNORE", user.usrId()); }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
