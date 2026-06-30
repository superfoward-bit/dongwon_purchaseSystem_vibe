package com.purchasesystem.domain.mrp;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pro/mrp")
@RequiredArgsConstructor
public class MrpController {

    private final MrpService mrpService;

    @GetMapping("/suggests")
    public ApiResponse<List<MrSuggest>> suggests(@RequestParam(required = false) String sts) {
        return ApiResponse.ok(mrpService.getSuggests(sts));
    }

    @PostMapping("/run")
    public ApiResponse<Integer> run(@AuthenticationPrincipal LoginUser user) {
        int n = mrpService.runMrp(user);
        return ApiResponse.ok("MRP 실행 완료: 발주제안 " + n + "건", n);
    }

    @PostMapping("/create-pr")
    public ApiResponse<String> createPr(@RequestBody Map<String, Object> body, @AuthenticationPrincipal LoginUser user) {
        @SuppressWarnings("unchecked")
        List<Object> raw = (List<Object>) body.get("ids");
        List<Long> ids = raw.stream().map(o -> Long.valueOf(String.valueOf(o))).toList();
        String prNo = mrpService.createPr(ids, user);
        return ApiResponse.ok("구매요청 " + prNo + " 생성되었습니다.", prNo);
    }

    @PostMapping("/{id}/ignore")
    public ApiResponse<Void> ignore(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        mrpService.ignore(id, user);
        return ApiResponse.ok("무시 처리되었습니다.", null);
    }
}
