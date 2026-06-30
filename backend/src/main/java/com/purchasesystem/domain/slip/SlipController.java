package com.purchasesystem.domain.slip;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ifc/slip")
@RequiredArgsConstructor
public class SlipController {

    private final SlipService slipService;

    @GetMapping
    public ApiResponse<List<AcSlip>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String slipSts) {
        return ApiResponse.ok(slipService.getList(keyword, slipSts));
    }

    @GetMapping("/{id}")
    public ApiResponse<AcSlip> detail(@PathVariable Long id) {
        return ApiResponse.ok(slipService.getDetail(id));
    }

    @PostMapping("/from-close")
    public ApiResponse<Long> fromClose(@RequestBody Map<String, Object> body, @AuthenticationPrincipal LoginUser user) {
        Long closeId = Long.valueOf(String.valueOf(body.get("closeId")));
        return ApiResponse.ok("매입전표가 생성되었습니다.", slipService.createFromClose(closeId, user));
    }

    @PostMapping("/{id}/post")
    public ApiResponse<Void> post(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        slipService.post(id, user);
        return ApiResponse.ok("전기되었습니다.", null);
    }

    @PostMapping("/{id}/send")
    public ApiResponse<Void> send(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        slipService.sendSap(id, user);
        return ApiResponse.ok("SAP 전송되었습니다.", null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        slipService.cancel(id, user);
        return ApiResponse.ok("취소되었습니다.", null);
    }
}
