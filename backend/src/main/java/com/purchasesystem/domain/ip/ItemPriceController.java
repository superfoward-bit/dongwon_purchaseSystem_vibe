package com.purchasesystem.domain.ip;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/ip")
@RequiredArgsConstructor
public class ItemPriceController {

    private final ItemPriceService service;

    // ===== 확정단가 구간 =====
    @GetMapping("/price")
    public ApiResponse<List<IpPrice>> priceList(@RequestParam(required = false) String vdCd,
                                                @RequestParam(required = false) String itemCd,
                                                @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(service.getPriceList(vdCd, itemCd, keyword));
    }

    @GetMapping("/price/history")
    public ApiResponse<List<IpPriceHis>> history(@RequestParam(required = false) String vdCd,
                                                 @RequestParam(required = false) String itemCd) {
        return ApiResponse.ok(service.getHistory(vdCd, itemCd));
    }

    @GetMapping("/price/effective")
    public ApiResponse<IpPrice> effective(@RequestParam String vdCd, @RequestParam String itemCd,
                                          @RequestParam(required = false) String ymd,
                                          @AuthenticationPrincipal LoginUser user) {
        String d = (ymd == null || ymd.isBlank()) ? java.time.LocalDate.now().toString() : ymd;
        return ApiResponse.ok(service.findEffectivePrice(user.compCd(), vdCd, itemCd, d));
    }

    // ===== 변경예약 =====
    @GetMapping("/rsv")
    public ApiResponse<List<IpRsv>> rsvList(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String sts) {
        return ApiResponse.ok(service.getRsvList(keyword, sts));
    }

    @GetMapping("/rsv/{id}")
    public ApiResponse<IpRsv> rsvDetail(@PathVariable Long id) {
        return ApiResponse.ok(service.getRsvDetail(id));
    }

    @PostMapping("/rsv")
    public ApiResponse<Long> create(@RequestBody IpRsv r, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", service.createRsv(r, user));
    }

    @PutMapping("/rsv/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody IpRsv r, @AuthenticationPrincipal LoginUser user) {
        service.updateRsv(id, r, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/rsv/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        service.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    @PostMapping("/rsv/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", service.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/rsv/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        service.deleteRsv(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
