package com.purchasesystem.domain.av;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/av")
@RequiredArgsConstructor
public class AvController {

    private final AvService avService;

    @GetMapping
    public ApiResponse<List<AvAdvance>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sts,
                                             @RequestParam(required = false) String vdCd) {
        return ApiResponse.ok(avService.getList(keyword, sts, vdCd));
    }

    @GetMapping("/{id}")
    public ApiResponse<AvAdvance> detail(@PathVariable Long id) {
        return ApiResponse.ok(avService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody AvAdvance a, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", avService.create(a, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody AvAdvance a, @AuthenticationPrincipal LoginUser user) {
        avService.update(id, a, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        avService.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", avService.action(id, body.get("action"), body.get("reason"), user));
    }

    /** 반제(상계) 추가 */
    @PostMapping("/{id}/repay")
    public ApiResponse<Void> repay(@PathVariable Long id, @RequestBody AvRepay repay, @AuthenticationPrincipal LoginUser user) {
        avService.addRepay(id, repay, user);
        return ApiResponse.ok("반제 처리되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        avService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
