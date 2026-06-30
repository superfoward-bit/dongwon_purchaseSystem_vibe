package com.purchasesystem.domain.vendor;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/base/vendor")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;

    @GetMapping
    public ApiResponse<List<VdVendor>> list(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String vdSts) {
        return ApiResponse.ok(vendorService.getList(keyword, vdSts));
    }

    /** 등록심사 대상 목록 */
    @GetMapping("/reg")
    public ApiResponse<List<VdVendor>> regList(@RequestParam(required = false) String keyword,
                                               @RequestParam(required = false) String regSts) {
        return ApiResponse.ok(vendorService.getRegList(keyword, regSts));
    }

    @GetMapping("/{id}")
    public ApiResponse<VdVendor> detail(@PathVariable Long id) {
        return ApiResponse.ok(vendorService.getDetail(id));
    }

    /** 결제조건 스냅샷 조회 (발주 등에서 협력사 선택 시 프리필용) */
    @GetMapping("/pay-info/{vdCd}")
    public ApiResponse<VdVendor> payInfo(@PathVariable String vdCd) {
        return ApiResponse.ok(vendorService.getPayInfo(vdCd));
    }

    /** 등록 심사요청(상신) */
    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        vendorService.submit(id, approvers, user);
        return ApiResponse.ok("심사요청되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", vendorService.action(id, body.get("action"), body.get("reason"), user));
    }

    /** 거래중지/재개 (action: STOP/RESUME, reason 필수) */
    @PostMapping("/{id}/trade")
    public ApiResponse<Void> trade(@PathVariable Long id, @RequestBody Map<String, String> body,
                                   @AuthenticationPrincipal LoginUser user) {
        vendorService.changeTrade(id, body.get("action"), body.get("reason"), user);
        return ApiResponse.ok("STOP".equals(body.get("action")) ? "거래중지 되었습니다." : "거래재개 되었습니다.", null);
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody VdVendor v, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", vendorService.create(v, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody VdVendor v,
                                    @AuthenticationPrincipal LoginUser user) {
        vendorService.update(id, v, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        vendorService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
