package com.purchasesystem.domain.tax;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ifc/tax")
@RequiredArgsConstructor
public class TaxController {

    private final TaxService taxService;

    @GetMapping
    public ApiResponse<List<TxTaxbill>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String billSts) {
        return ApiResponse.ok(taxService.getList(keyword, billSts));
    }

    @GetMapping("/{id}")
    public ApiResponse<TxTaxbill> detail(@PathVariable Long id) {
        return ApiResponse.ok(taxService.getDetail(id));
    }

    @PostMapping("/from-close")
    public ApiResponse<Long> fromClose(@RequestBody Map<String, Object> body, @AuthenticationPrincipal LoginUser user) {
        Long closeId = Long.valueOf(String.valueOf(body.get("closeId")));
        return ApiResponse.ok("세금계산서가 생성되었습니다.", taxService.createFromClose(closeId, user));
    }

    @PostMapping("/{id}/issue")
    public ApiResponse<Void> issue(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        taxService.issue(id, user);
        return ApiResponse.ok("발행·국세청 전송되었습니다.", null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        taxService.cancel(id, user);
        return ApiResponse.ok("취소되었습니다.", null);
    }
}
