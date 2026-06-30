package com.purchasesystem.domain.po;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/po")
@RequiredArgsConstructor
public class PoController {

    private final PoService poService;

    @GetMapping
    public ApiResponse<List<PoOrder>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String sts) {
        return ApiResponse.ok(poService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<PoOrder> detail(@PathVariable Long id) {
        return ApiResponse.ok(poService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody PoOrder po, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", poService.create(po, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PoOrder po,
                                    @AuthenticationPrincipal LoginUser user) {
        poService.update(id, po, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    /** 상신: 결재선 지정 */
    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        poService.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.",
                poService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        poService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
