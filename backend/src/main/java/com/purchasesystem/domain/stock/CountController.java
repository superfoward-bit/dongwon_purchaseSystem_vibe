package com.purchasesystem.domain.stock;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inv/count")
@RequiredArgsConstructor
public class CountController {

    private final CountService countService;

    @GetMapping
    public ApiResponse<List<StCount>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String sts) {
        return ApiResponse.ok(countService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<StCount> detail(@PathVariable Long id) { return ApiResponse.ok(countService.getDetail(id)); }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody StCount c, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", countService.create(c, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody StCount c, @AuthenticationPrincipal LoginUser user) {
        countService.update(id, c, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<Void> confirm(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        countService.confirm(id, user);
        return ApiResponse.ok("실사 확정(재고 조정)되었습니다.", null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        countService.cancel(id, user);
        return ApiResponse.ok("취소되었습니다.", null);
    }
}
