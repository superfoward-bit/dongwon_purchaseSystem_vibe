package com.purchasesystem.domain.pa;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/pa")
@RequiredArgsConstructor
public class PaController {

    private final PaService paService;

    @GetMapping
    public ApiResponse<List<PaAdjust>> list(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String sts,
                                            @RequestParam(required = false) String vdCd) {
        return ApiResponse.ok(paService.getList(keyword, sts, vdCd));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaAdjust> detail(@PathVariable Long id) {
        return ApiResponse.ok(paService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody PaAdjust a, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", paService.create(a, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PaAdjust a, @AuthenticationPrincipal LoginUser user) {
        paService.update(id, a, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        paService.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", paService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        paService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
