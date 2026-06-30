package com.purchasesystem.domain.qc;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qlt/nc")
@RequiredArgsConstructor
public class QcController {

    private final QcService qcService;

    @GetMapping
    public ApiResponse<List<QcNonconf>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sts,
                                             @RequestParam(required = false) String vdCd) {
        return ApiResponse.ok(qcService.getList(keyword, sts, vdCd));
    }

    @GetMapping("/{id}")
    public ApiResponse<QcNonconf> detail(@PathVariable Long id) {
        return ApiResponse.ok(qcService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody QcNonconf n, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", qcService.create(n, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody QcNonconf n, @AuthenticationPrincipal LoginUser user) {
        qcService.update(id, n, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", qcService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        qcService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
