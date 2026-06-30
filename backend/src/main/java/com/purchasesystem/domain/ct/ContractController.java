package com.purchasesystem.domain.ct;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/ct")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;

    @GetMapping
    public ApiResponse<List<CtContract>> list(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String sts) {
        return ApiResponse.ok(contractService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<CtContract> detail(@PathVariable Long id) {
        return ApiResponse.ok(contractService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody CtContract c, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", contractService.create(c, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CtContract c,
                                    @AuthenticationPrincipal LoginUser user) {
        contractService.update(id, c, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id, @RequestBody Map<String, Object> body,
                                    @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Map<String, String>> approvers = (List<Map<String, String>>) body.get("approvers");
        contractService.submit(id, approvers, user);
        return ApiResponse.ok("상신되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", contractService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        contractService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }

    /** 유효 계약단가 조회 (단가 자동적용) */
    @GetMapping("/active-price")
    public ApiResponse<CtItem> activePrice(@RequestParam String vdCd, @RequestParam String itemCd,
                                           @RequestParam(required = false) String ymd) {
        String d = (ymd == null || ymd.isBlank()) ? java.time.LocalDate.now().toString() : ymd;
        return ApiResponse.ok(contractService.findActivePrice(vdCd, itemCd, d));
    }
}
