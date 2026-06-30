package com.purchasesystem.domain.ireq;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/base/item-req")
@RequiredArgsConstructor
public class IreqController {

    private final IreqService ireqService;

    @GetMapping
    public ApiResponse<List<ItItemReq>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String sts) {
        return ApiResponse.ok(ireqService.getList(keyword, sts));
    }

    @GetMapping("/{id}")
    public ApiResponse<ItItemReq> detail(@PathVariable Long id) {
        return ApiResponse.ok(ireqService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody ItItemReq r, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록요청되었습니다.", ireqService.create(r, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ItItemReq r, @AuthenticationPrincipal LoginUser user) {
        ireqService.update(id, r, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @PostMapping("/{id}/action")
    public ApiResponse<String> action(@PathVariable Long id, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("처리되었습니다.", ireqService.action(id, body.get("action"), body.get("reason"), user));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        ireqService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
