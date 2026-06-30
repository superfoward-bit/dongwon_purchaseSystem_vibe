package com.purchasesystem.domain.eval;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/srm/eval")
@RequiredArgsConstructor
public class EvalController {

    private final EvalService evalService;

    @GetMapping
    public ApiResponse<List<VdEval>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String sts) {
        return ApiResponse.ok(evalService.getEvalList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<VdEval> detail(@PathVariable Long id) {
        return ApiResponse.ok(evalService.getEvalDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody VdEval e, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("평가가 시작되었습니다.", evalService.create(e, user));
    }

    @PostMapping("/{id}/scores")
    public ApiResponse<Void> saveScores(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                        @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> scores = (List<Map<String, Object>>) body.get("scores");
        evalService.saveScores(id, scores, user);
        return ApiResponse.ok("점수가 저장되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", evalService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        evalService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
