package com.purchasesystem.domain.budget;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ApiResponse<List<BgBudget>> list(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String fiscalYear) {
        return ApiResponse.ok(budgetService.getList(keyword, fiscalYear));
    }

    @GetMapping("/{id}")
    public ApiResponse<BgBudget> detail(@PathVariable Long id) { return ApiResponse.ok(budgetService.getDetail(id)); }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody BgBudget b, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", budgetService.create(b, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody BgBudget b, @AuthenticationPrincipal LoginUser user) {
        budgetService.update(id, b, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        budgetService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
