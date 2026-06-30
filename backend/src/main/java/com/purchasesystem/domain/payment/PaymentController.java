package com.purchasesystem.domain.payment;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ApiResponse<List<PyPayment>> list(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) String paySts) {
        return ApiResponse.ok(paymentService.getList(keyword, paySts));
    }

    @GetMapping("/{id}")
    public ApiResponse<PyPayment> detail(@PathVariable Long id) {
        return ApiResponse.ok(paymentService.getDetail(id));
    }

    @PostMapping("/from-close")
    public ApiResponse<Long> fromClose(@RequestBody Map<String, Object> body, @AuthenticationPrincipal LoginUser user) {
        Long closeId = Long.valueOf(String.valueOf(body.get("closeId")));
        return ApiResponse.ok("지급내역이 생성되었습니다.", paymentService.createFromClose(closeId, user));
    }

    @PostMapping("/{id}/pay")
    public ApiResponse<Void> pay(@PathVariable Long id, @RequestBody Map<String, String> body,
                                 @AuthenticationPrincipal LoginUser user) {
        paymentService.pay(id, body.get("payYmd"), body.get("noteNo"), body.get("noteDueYmd"), user);
        return ApiResponse.ok("지급 실행되었습니다.", null);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        paymentService.cancel(id, user);
        return ApiResponse.ok("취소되었습니다.", null);
    }
}
