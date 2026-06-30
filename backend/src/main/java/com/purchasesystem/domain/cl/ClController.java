package com.purchasesystem.domain.cl;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/cl")
@RequiredArgsConstructor
public class ClController {

    private final ClService clService;

    @GetMapping
    public ApiResponse<List<ClClose>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String sts,
                                           @RequestParam(required = false) String closeYm) {
        return ApiResponse.ok(clService.getList(keyword, sts, closeYm));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClClose> detail(@PathVariable Long id) {
        return ApiResponse.ok(clService.getDetail(id));
    }

    /** 마감대상 입고 미리보기 */
    @GetMapping("/eligible")
    public ApiResponse<List<ClCloseGr>> eligible(@RequestParam String vdCd, @RequestParam String closeYm) {
        return ApiResponse.ok(clService.previewEligible(vdCd, closeYm));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody Map<String, String> body,
                                    @AuthenticationPrincipal LoginUser user) {
        Long id = clService.create(body.get("vdCd"), body.get("vdNm"), body.get("closeYm"), user);
        return ApiResponse.ok("정산마감이 생성되었습니다.", id);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        clService.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.",
                clService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        clService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }

    /** 갭정산 조정 재계산 */
    @PostMapping("/{id}/recalc-gap")
    public ApiResponse<java.math.BigDecimal> recalcGap(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("갭정산이 재계산되었습니다.", clService.recalcGap(id, user));
    }
}
