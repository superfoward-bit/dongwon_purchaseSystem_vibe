package com.purchasesystem.domain.portal;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.auction.AuAuction;
import com.purchasesystem.domain.rx.RxQuoteItem;
import com.purchasesystem.domain.rx.RxRfx;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    // ----- 견적 응찰 -----
    @GetMapping("/rfx")
    public ApiResponse<List<RxRfx>> rfxList(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(portalService.getMyRfxList(user));
    }

    @GetMapping("/rfx/{id}")
    public ApiResponse<RxRfx> rfxDetail(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(portalService.getMyRfxDetail(id, user));
    }

    @PostMapping("/rfx/{id}/quote")
    public ApiResponse<Void> submitQuote(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("items");
        List<RxQuoteItem> items = raw.stream().map(m -> {
            RxQuoteItem q = new RxQuoteItem();
            q.setRfxItemId(Long.valueOf(String.valueOf(m.get("rfxItemId"))));
            Object prc = m.get("offerPrc");
            q.setOfferPrc(prc == null ? null : new BigDecimal(String.valueOf(prc)));
            return q;
        }).toList();
        portalService.submitMyQuote(id, items, user);
        return ApiResponse.ok("견적이 제출되었습니다.", null);
    }

    // ----- 역경매 입찰 -----
    @GetMapping("/au")
    public ApiResponse<List<AuAuction>> auList(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(portalService.getMyAuctionList(user));
    }

    @GetMapping("/au/{id}")
    public ApiResponse<AuAuction> auDetail(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(portalService.getMyAuctionDetail(id, user));
    }

    @PostMapping("/au/{id}/bid")
    public ApiResponse<Map<String, Object>> bid(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                @AuthenticationPrincipal LoginUser user) {
        BigDecimal bidAmt = body.get("bidAmt") == null ? null : new BigDecimal(String.valueOf(body.get("bidAmt")));
        return ApiResponse.ok("입찰되었습니다.", portalService.placeMyBid(id, bidAmt, user));
    }
}
