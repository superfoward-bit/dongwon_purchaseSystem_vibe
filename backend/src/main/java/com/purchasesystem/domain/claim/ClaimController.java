package com.purchasesystem.domain.claim;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/srm/claim")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @GetMapping
    public ApiResponse<List<CmClaim>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String sts,
                                           @RequestParam(required = false) String vdCd) {
        return ApiResponse.ok(claimService.getList(keyword, sts, vdCd));
    }

    @GetMapping("/{id}")
    public ApiResponse<CmClaim> detail(@PathVariable Long id) {
        return ApiResponse.ok(claimService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody CmClaim c, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", claimService.create(c, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CmClaim c, @AuthenticationPrincipal LoginUser user) {
        claimService.update(id, c, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", claimService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        claimService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
