package com.purchasesystem.domain.pr;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/pr")
@RequiredArgsConstructor
public class PrController {

    private final PrService prService;

    @GetMapping
    public ApiResponse<List<PrRequest>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sts) {
        return ApiResponse.ok(prService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<PrRequest> detail(@PathVariable Long id) {
        return ApiResponse.ok(prService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody PrRequest pr, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", prService.create(pr, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody PrRequest pr,
                                    @AuthenticationPrincipal LoginUser user) {
        prService.update(id, pr, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    /** 상신: 결재선 지정 */
    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        prService.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    /** 상태전이: 회수/취소 */
    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        String action = body.get("action");
        String reason = body.get("reason");
        String toSts = prService.action(id, action, reason, user);
        return ApiResponse.ok("처리되었습니다.", toSts);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        prService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
