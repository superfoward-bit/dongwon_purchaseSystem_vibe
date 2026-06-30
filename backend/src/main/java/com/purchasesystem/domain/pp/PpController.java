package com.purchasesystem.domain.pp;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/pp")
@RequiredArgsConstructor
public class PpController {

    private final PpService ppService;

    @GetMapping
    public ApiResponse<List<PpPlan>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sts,
                                          @RequestParam(required = false) String planYear) {
        return ApiResponse.ok(ppService.getList(keyword, sts, planYear));
    }

    @GetMapping("/{id}")
    public ApiResponse<PpPlan> detail(@PathVariable Long id) {
        return ApiResponse.ok(ppService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody PpPlan p, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", ppService.create(p, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PpPlan p, @AuthenticationPrincipal LoginUser user) {
        ppService.update(id, p, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", ppService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        ppService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
