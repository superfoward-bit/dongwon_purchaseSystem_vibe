package com.purchasesystem.domain.risk;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/srm/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    public ApiResponse<List<VdAudit>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String sts) {
        return ApiResponse.ok(auditService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<VdAudit> detail(@PathVariable Long id) {
        return ApiResponse.ok(auditService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody VdAudit a, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", auditService.create(a, user));
    }

    @PostMapping("/{id}/results")
    public ApiResponse<Void> saveResults(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> raw = (List<Map<String, Object>>) body.get("results");
        List<VdAuditResult> results = raw.stream().map(m -> {
            VdAuditResult r = new VdAuditResult();
            r.setAuditItemNm((String) m.get("auditItemNm"));
            r.setResult((String) m.get("result"));
            r.setFinding((String) m.get("finding"));
            r.setActionReq((String) m.get("actionReq"));
            return r;
        }).toList();
        auditService.saveResults(id, results, user);
        return ApiResponse.ok("저장되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", auditService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        auditService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
