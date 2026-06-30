package com.purchasesystem.domain.gr;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/gr")
@RequiredArgsConstructor
public class GrController {

    private final GrService grService;

    @GetMapping
    public ApiResponse<List<GrReceipt>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sts) {
        return ApiResponse.ok(grService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<GrReceipt> detail(@PathVariable Long id) {
        return ApiResponse.ok(grService.getDetail(id));
    }

    /** 입고대상 발주(PC) 목록 */
    @GetMapping("/receivable-po")
    public ApiResponse<List<GrReceipt>> receivablePos(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(grService.getReceivablePos(keyword));
    }

    /** 발주 선택 → 헤더+잔량품목 프리필 */
    @GetMapping("/from-po/{poId}")
    public ApiResponse<GrReceipt> fromPo(@PathVariable Long poId) {
        return ApiResponse.ok(grService.prepareFromPo(poId));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody GrReceipt gr, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", grService.create(gr, user));
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        grService.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.",
                grService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        grService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
