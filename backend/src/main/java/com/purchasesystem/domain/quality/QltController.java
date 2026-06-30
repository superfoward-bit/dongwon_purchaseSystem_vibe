package com.purchasesystem.domain.quality;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qlt/inspection")
@RequiredArgsConstructor
public class QltController {

    private final QltService qltService;

    @GetMapping
    public ApiResponse<List<QlInspection>> list(@RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String sts) {
        return ApiResponse.ok(qltService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<QlInspection> detail(@PathVariable Long id) {
        return ApiResponse.ok(qltService.getDetail(id));
    }

    /** 검사대상 입고(CFM) 목록 */
    @GetMapping("/receivable-gr")
    public ApiResponse<List<QlInspection>> receivableGrs(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(qltService.getInspectableGrs(keyword));
    }

    @GetMapping("/from-gr/{grId}")
    public ApiResponse<QlInspection> fromGr(@PathVariable Long grId) {
        return ApiResponse.ok(qltService.prepareFromGr(grId));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody QlInspection q, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", qltService.create(q, user));
    }

    @PostMapping("/{id}/results")
    public ApiResponse<Void> saveResults(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("results");
        List<QlResult> results = raw.stream().map(m -> {
            QlResult r = new QlResult();
            r.setInspItemNm((String) m.get("inspItemNm"));
            r.setSpecVal((String) m.get("specVal"));
            r.setMeasureVal((String) m.get("measureVal"));
            r.setUnit((String) m.get("unit"));
            r.setPassYn((String) m.get("passYn"));
            return r;
        }).toList();
        qltService.saveResults(id, results, user);
        return ApiResponse.ok("저장되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", qltService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        qltService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
