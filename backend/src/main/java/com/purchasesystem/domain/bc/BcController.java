package com.purchasesystem.domain.bc;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/bc")
@RequiredArgsConstructor
public class BcController {

    private final BcService bcService;

    @GetMapping
    public ApiResponse<List<BcContract>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String sts,
                                              @RequestParam(required = false) String vdCd) {
        return ApiResponse.ok(bcService.getList(keyword, sts, vdCd));
    }

    /** 만료예정 계약 (종료일 N일 이내) */
    @GetMapping("/expiring")
    public ApiResponse<List<BcContract>> expiring(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.ok(bcService.getExpiring(days));
    }

    @GetMapping("/{id}")
    public ApiResponse<BcContract> detail(@PathVariable Long id) {
        return ApiResponse.ok(bcService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody BcContract c, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", bcService.create(c, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody BcContract c, @AuthenticationPrincipal LoginUser user) {
        bcService.update(id, c, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        bcService.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", bcService.action(id, body.get("action"), body.get("reason"), user));
    }

    /** 개정(다음 차수 작성본 생성) */
    @PostMapping("/{id}/revise")
    public ApiResponse<Long> revise(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("개정본이 생성되었습니다.", bcService.revise(id, user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        bcService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
