package com.purchasesystem.domain.auction;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.auction.mapper.AuctionMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private static final String DOC_TYP = "AC";

    private final AuctionMapper auctionMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;

    public List<AuAuction> getList(String keyword, String sts) { return auctionMapper.findList(keyword, sts); }

    public AuAuction getDetail(Long id) {
        AuAuction a = auctionMapper.findById(id);
        if (a == null) throw new BusinessException("역경매를 찾을 수 없습니다.");
        a.setItems(auctionMapper.findItems(id));
        List<AuVendor> vendors = auctionMapper.findVendors(id);
        int rank = 1;
        for (AuVendor v : vendors) if (v.getLastBidAmt() != null) v.setRankNo(rank++);
        a.setVendors(vendors);
        a.setBids(auctionMapper.findBids(id));
        a.setActions(statusService.availableActions(DOC_TYP, a.getSts()));
        a.setHistory(statusService.history(DOC_TYP, id));
        return a;
    }

    @Transactional
    public Long create(AuAuction a, LoginUser user) {
        if (a.getItems() == null || a.getItems().isEmpty()) throw new BusinessException("품목을 입력하세요.");
        if (a.getVendors() == null || a.getVendors().isEmpty()) throw new BusinessException("참여 협력사를 1개 이상 선택하세요.");
        a.setStartPrc(totalStart(a.getItems()));
        a.setCompCd(user.compCd());
        a.setChrgUsrId(user.usrId());
        a.setSts("TMP");
        a.setRegId(user.usrId());
        a.setAuctionNo(docNoService.generate(DOC_TYP));
        auctionMapper.insertAuction(a);
        saveItemsVendors(a);
        return a.getId();
    }

    @Transactional
    public void update(Long id, AuAuction a, LoginUser user) {
        AuAuction cur = auctionMapper.findById(id);
        if (cur == null) throw new BusinessException("역경매를 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        a.setId(id);
        a.setModId(user.usrId());
        a.setStartPrc(totalStart(a.getItems()));
        auctionMapper.updateAuction(a);
        auctionMapper.deleteItems(id);
        auctionMapper.deleteVendors(id);
        saveItemsVendors(a);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        AuAuction cur = auctionMapper.findById(id);
        if (cur == null) throw new BusinessException("역경매를 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getAuctionNo(), cur.getSts(),
                actionCode, reason, user.usrId());
        auctionMapper.updateStatus(id, toSts, user.usrId());
        if ("CANCEL_AWARD".equals(actionCode)) {
            auctionMapper.clearAwardYn(id);
            auctionMapper.updateAward(id, null, null, null, user.usrId());
        }
        return toSts;
    }

    /** 입찰 (총액 하향) */
    @Transactional
    public Map<String, Object> placeBid(Long auctionId, String vdCd, BigDecimal bidAmt, LoginUser user) {
        AuAuction a = auctionMapper.findById(auctionId);
        if (a == null) throw new BusinessException("역경매를 찾을 수 없습니다.");
        if (!"OPEN".equals(a.getSts())) throw new BusinessException("진행중 상태에서만 입찰할 수 있습니다.");
        if (bidAmt == null || bidAmt.signum() <= 0) throw new BusinessException("입찰가를 입력하세요.");
        AuVendor v = auctionMapper.findVendor(auctionId, vdCd);
        if (v == null) throw new BusinessException("참여 협력사가 아닙니다.");
        if (bidAmt.compareTo(a.getStartPrc()) > 0) throw new BusinessException("입찰가는 시작가(" + a.getStartPrc() + ") 이하여야 합니다.");
        if (v.getLastBidAmt() != null && bidAmt.compareTo(v.getLastBidAmt()) >= 0) {
            throw new BusinessException("이전 입찰가(" + v.getLastBidAmt() + ")보다 낮아야 합니다.");
        }

        auctionMapper.updateVendorBid(auctionId, vdCd, bidAmt);
        // 순위 = 나보다 낮은 가격을 가진 협력사 수 + 1
        int rank = 1;
        for (AuVendor o : auctionMapper.findVendors(auctionId)) {
            if (!o.getVdCd().equals(vdCd) && o.getLastBidAmt() != null && o.getLastBidAmt().compareTo(bidAmt) < 0) rank++;
        }
        AuBid bid = new AuBid();
        bid.setAuctionId(auctionId);
        bid.setVdCd(vdCd);
        bid.setVdNm(v.getVdNm());
        bid.setBidAmt(bidAmt);
        bid.setRankNo(rank);
        auctionMapper.insertBid(bid);

        // 자동연장(스나이핑 방지): 마감 임박 입찰 시 마감시각 연장
        boolean extended = auctionMapper.tryAutoExtend(auctionId) > 0;
        AuAuction after = auctionMapper.findById(auctionId);

        Map<String, Object> r = new HashMap<>();
        r.put("rank", rank);
        r.put("bidAmt", bidAmt);
        r.put("extended", extended);
        r.put("endDt", after.getEndDt());
        r.put("extCnt", after.getExtCnt());
        return r;
    }

    /** 낙찰 (진행중 + 입찰한 협력사) */
    @Transactional
    public String award(Long auctionId, String vdCd, LoginUser user) {
        AuAuction a = auctionMapper.findById(auctionId);
        if (a == null) throw new BusinessException("역경매를 찾을 수 없습니다.");
        AuVendor v = auctionMapper.findVendor(auctionId, vdCd);
        if (v == null || v.getLastBidAmt() == null) throw new BusinessException("입찰하지 않은 협력사는 낙찰할 수 없습니다.");
        auctionMapper.clearAwardYn(auctionId);
        auctionMapper.setAwardYn(auctionId, vdCd);
        auctionMapper.updateAward(auctionId, vdCd, v.getVdNm(), v.getLastBidAmt(), user.usrId());
        String toSts = statusService.transition(DOC_TYP, auctionId, a.getAuctionNo(), a.getSts(),
                "AWARD", null, user.usrId());
        auctionMapper.updateStatus(auctionId, toSts, user.usrId());
        return toSts;
    }

    /** 낙찰결과 → 발주 소스 (낙찰가 비율로 품목단가 환산) */
    public Map<String, Object> getPoSource(Long auctionId) {
        AuAuction a = auctionMapper.findById(auctionId);
        if (a == null || a.getAwardVdCd() == null) throw new BusinessException("낙찰된 역경매가 아닙니다.");
        BigDecimal ratio = a.getStartPrc().signum() == 0 ? BigDecimal.ONE
                : a.getAwardAmt().divide(a.getStartPrc(), 6, RoundingMode.HALF_UP);
        List<Map<String, Object>> items = auctionMapper.findItems(auctionId).stream().map(it -> {
            BigDecimal prc = it.getStartPrc().multiply(ratio).setScale(0, RoundingMode.HALF_UP);
            Map<String, Object> m = new HashMap<>();
            m.put("prItemId", it.getPrItemId());
            m.put("itemCd", it.getItemCd());
            m.put("itemNm", it.getItemNm());
            m.put("spec", it.getSpec());
            m.put("unitCd", it.getUnitCd());
            m.put("qty", it.getQty());
            m.put("prc", prc);
            return m;
        }).toList();
        Map<String, Object> src = new HashMap<>();
        src.put("vdCd", a.getAwardVdCd());
        src.put("vdNm", a.getAwardVdNm());
        src.put("poTitle", a.getAuctionTitle());
        src.put("srcTyp", "AUCTION");
        src.put("srcId", a.getId());
        src.put("items", items);
        return src;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        AuAuction cur = auctionMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        auctionMapper.deleteItems(id);
        auctionMapper.deleteVendors(id);
        auctionMapper.deleteAuction(id, user.usrId());
    }

    private BigDecimal totalStart(List<AuItem> items) {
        BigDecimal tot = BigDecimal.ZERO;
        if (items != null) for (AuItem it : items) {
            BigDecimal qty = it.getQty() == null ? BigDecimal.ZERO : it.getQty();
            BigDecimal prc = it.getStartPrc() == null ? BigDecimal.ZERO : it.getStartPrc();
            tot = tot.add(qty.multiply(prc));
        }
        return tot;
    }

    private void saveItemsVendors(AuAuction a) {
        int line = 1;
        for (AuItem it : a.getItems()) { it.setAuctionId(a.getId()); it.setLineNo(line++); auctionMapper.insertItem(it); }
        for (AuVendor v : a.getVendors()) { v.setAuctionId(a.getId()); auctionMapper.insertVendor(v); }
    }
}
