package com.purchasesystem.domain.risk;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/srm/regal")
@RequiredArgsConstructor
public class RegalController {

    private final RegalService regalService;

    @GetMapping
    public ApiResponse<List<VdRegal>> list(@RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String sts) {
        return ApiResponse.ok(regalService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<VdRegal> detail(@PathVariable Long id) {
        return ApiResponse.ok(regalService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody VdRegal r, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", regalService.create(r, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody VdRegal r,
                                    @AuthenticationPrincipal LoginUser user) {
        regalService.update(id, r, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        regalService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
