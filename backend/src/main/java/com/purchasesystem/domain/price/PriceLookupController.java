package com.purchasesystem.domain.price;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pro/price")
@RequiredArgsConstructor
public class PriceLookupController {

    private final PriceLookupService service;

    /** 단가 자동적용 조회 (계약단가 우선, 없으면 품목단가) */
    @GetMapping("/lookup")
    public ApiResponse<PriceLookup> lookup(@RequestParam String vdCd, @RequestParam String itemCd,
                                           @RequestParam(required = false) String ymd,
                                           @AuthenticationPrincipal LoginUser user) {
        String d = (ymd == null || ymd.isBlank()) ? java.time.LocalDate.now().toString() : ymd;
        return ApiResponse.ok(service.lookup(user.compCd(), vdCd, itemCd, d));
    }
}
