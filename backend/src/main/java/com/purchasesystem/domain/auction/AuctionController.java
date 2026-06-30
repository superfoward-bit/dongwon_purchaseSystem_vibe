package com.purchasesystem.domain.auction;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/au")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @GetMapping
    public ApiResponse<List<AuAuction>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sts) {
        return ApiResponse.ok(auctionService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<AuAuction> detail(@PathVariable Long id) {
        return ApiResponse.ok(auctionService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody AuAuction a, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", auctionService.create(a, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody AuAuction a,
                                    @AuthenticationPrincipal LoginUser user) {
        auctionService.update(id, a, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", auctionService.action(id, body.get("action"), body.get("reason"), user));
    }

    /** 입찰 */
    @PostMapping("/{id}/bid")
    public ApiResponse<Map<String, Object>> bid(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                                @AuthenticationPrincipal LoginUser user) {
        String vdCd = (String) body.get("vdCd");
        BigDecimal bidAmt = body.get("bidAmt") == null ? null : new BigDecimal(String.valueOf(body.get("bidAmt")));
        return ApiResponse.ok("입찰되었습니다.", auctionService.placeBid(id, vdCd, bidAmt, user));
    }

    /** 낙찰 */
    @PostMapping("/{id}/award")
    public ApiResponse<String> award(@PathVariable Long id, @RequestBody Map<String, String> body,
                                     @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("낙찰되었습니다.", auctionService.award(id, body.get("vdCd"), user));
    }

    @GetMapping("/{id}/po-source")
    public ApiResponse<Map<String, Object>> poSource(@PathVariable Long id) {
        return ApiResponse.ok(auctionService.getPoSource(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        auctionService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
