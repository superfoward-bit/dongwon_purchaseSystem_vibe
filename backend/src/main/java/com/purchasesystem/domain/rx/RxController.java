package com.purchasesystem.domain.rx;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/rx")
@RequiredArgsConstructor
public class RxController {

    private final RxService rxService;

    @GetMapping
    public ApiResponse<List<RxRfx>> list(@RequestParam(required = false) String keyword,
                                         @RequestParam(required = false) String sts) {
        return ApiResponse.ok(rxService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<RxRfx> detail(@PathVariable Long id) {
        return ApiResponse.ok(rxService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody RxRfx r, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", rxService.create(r, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody RxRfx r,
                                    @AuthenticationPrincipal LoginUser user) {
        rxService.update(id, r, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", rxService.action(id, body.get("action"), body.get("reason"), user));
    }

    /** 재공고: 유찰/취소 견적 복제 */
    @PostMapping("/{id}/reannounce")
    public ApiResponse<Long> reannounce(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("재공고 견적이 생성되었습니다.", rxService.reannounce(id, user));
    }

    /** 협력사 견적 응답 입력 */
    @PostMapping("/{id}/quote")
    public ApiResponse<Void> saveQuote(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                       @AuthenticationPrincipal LoginUser user) {
        String vdCd = (String) body.get("vdCd");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("items");
        List<RxQuoteItem> items = raw.stream().map(m -> {
            RxQuoteItem q = new RxQuoteItem();
            q.setRfxItemId(Long.valueOf(String.valueOf(m.get("rfxItemId"))));
            Object prc = m.get("offerPrc");
            q.setOfferPrc(prc == null ? null : new java.math.BigDecimal(String.valueOf(prc)));
            return q;
        }).toList();
        rxService.saveQuote(id, vdCd, items, user);
        return ApiResponse.ok("견적이 저장되었습니다.", null);
    }

    /** 협력사 선정 */
    @PostMapping("/{id}/select")
    public ApiResponse<String> select(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("선정되었습니다.", rxService.selectVendor(id, body.get("vdCd"), user));
    }

    /** 선정결과 → 발주 소스 */
    @GetMapping("/{id}/po-source")
    public ApiResponse<Map<String, Object>> poSource(@PathVariable Long id) {
        return ApiResponse.ok(rxService.getPoSource(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        rxService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
