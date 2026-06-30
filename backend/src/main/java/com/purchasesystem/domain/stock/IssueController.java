package com.purchasesystem.domain.stock;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inv/issue")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @GetMapping
    public ApiResponse<List<StIssue>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String sts) {
        return ApiResponse.ok(issueService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<StIssue> detail(@PathVariable Long id) { return ApiResponse.ok(issueService.getDetail(id)); }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody StIssue i, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", issueService.create(i, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody StIssue i, @AuthenticationPrincipal LoginUser user) {
        issueService.update(id, i, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        issueService.confirm(id, user);
        return ApiResponse.ok("출고 확정(재고 차감)되었습니다.", null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        issueService.cancel(id, user);
        return ApiResponse.ok("취소되었습니다.", null);
    }
}
