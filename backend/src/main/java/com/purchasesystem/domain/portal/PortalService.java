package com.purchasesystem.domain.portal;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.auction.AuAuction;
import com.purchasesystem.domain.auction.AuBid;
import com.purchasesystem.domain.auction.AuVendor;
import com.purchasesystem.domain.auction.AuctionService;
import com.purchasesystem.domain.auction.mapper.AuctionMapper;
import com.purchasesystem.domain.rx.RxQuoteItem;
import com.purchasesystem.domain.rx.RxRfx;
import com.purchasesystem.domain.rx.RxVendor;
import com.purchasesystem.domain.rx.RxService;
import com.purchasesystem.domain.rx.mapper.RxMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/** 협력사 포털 서비스 — 로그인한 협력사(vdCd) 범위로만 노출. */
@Service
@RequiredArgsConstructor
public class PortalService {

    private final RxMapper rxMapper;
    private final RxService rxService;
    private final AuctionMapper auctionMapper;
    private final AuctionService auctionService;

    private String vd(LoginUser u) {
        if (u.vdCd() == null || u.vdCd().isBlank()) throw new BusinessException("협력사 계정이 아닙니다.");
        return u.vdCd();
    }

    // ===== 견적 =====
    public List<RxRfx> getMyRfxList(LoginUser u) {
        return rxMapper.findByVendor(vd(u));
    }

    public RxRfx getMyRfxDetail(Long rfxId, LoginUser u) {
        String vdCd = vd(u);
        RxRfx r = rxMapper.findById(rfxId);
        if (r == null) throw new BusinessException("견적을 찾을 수 없습니다.");
        boolean invited = rxMapper.findVendors(rfxId).stream().anyMatch(v -> vdCd.equals(v.getVdCd()));
        if (!invited) throw new BusinessException("초대받지 않은 견적입니다.");
        r.setItems(rxMapper.findItems(rfxId));
        r.setQuoteItems(rxMapper.findQuoteItems(rfxId).stream().filter(q -> vdCd.equals(q.getVdCd())).toList());
        r.setVendors(rxMapper.findVendors(rfxId).stream().filter(v -> vdCd.equals(v.getVdCd())).toList());
        return r;
    }

    public void submitMyQuote(Long rfxId, List<RxQuoteItem> items, LoginUser u) {
        rxService.saveQuote(rfxId, vd(u), items, u);
    }

    // ===== 역경매 =====
    public List<AuAuction> getMyAuctionList(LoginUser u) {
        return auctionMapper.findByVendor(vd(u));
    }

    public AuAuction getMyAuctionDetail(Long auctionId, LoginUser u) {
        String vdCd = vd(u);
        AuAuction a = auctionMapper.findById(auctionId);
        if (a == null) throw new BusinessException("역경매를 찾을 수 없습니다.");
        AuVendor me = auctionMapper.findVendor(auctionId, vdCd);
        if (me == null) throw new BusinessException("참여 협력사가 아닙니다.");
        // 내 순위 계산 (다른 업체 입찰가는 비노출)
        int rank = 1;
        if (me.getLastBidAmt() != null) {
            for (AuVendor o : auctionMapper.findVendors(auctionId)) {
                if (!vdCd.equals(o.getVdCd()) && o.getLastBidAmt() != null
                        && o.getLastBidAmt().compareTo(me.getLastBidAmt()) < 0) rank++;
            }
            me.setRankNo(rank);
        }
        a.setItems(auctionMapper.findItems(auctionId));
        a.setVendors(List.of(me));
        a.setBids(auctionMapper.findBids(auctionId).stream().filter(b -> vdCd.equals(b.getVdCd())).toList());
        return a;
    }

    public java.util.Map<String, Object> placeMyBid(Long auctionId, BigDecimal bidAmt, LoginUser u) {
        return auctionService.placeBid(auctionId, vd(u), bidAmt, u);
    }
}
