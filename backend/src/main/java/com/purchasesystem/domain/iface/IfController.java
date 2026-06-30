package com.purchasesystem.domain.iface;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.iface.mapper.IfMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ifc")
@RequiredArgsConstructor
public class IfController {

    private final IfService ifService;
    private final IfMapper ifMapper;

    @GetMapping
    public ApiResponse<List<IfInterface>> list(@RequestParam(required = false) String ifTyp,
                                               @RequestParam(required = false) String ifStatus,
                                               @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(ifService.getList(ifTyp, ifStatus, keyword));
    }

    @GetMapping("/{id}")
    public ApiResponse<IfInterface> detail(@PathVariable Long id) {
        return ApiResponse.ok(ifMapper.findById(id));
    }

    @PostMapping("/{id}/resend")
    public ApiResponse<String> resend(@PathVariable Long id) {
        return ApiResponse.ok("재전송되었습니다.", ifService.resend(id));
    }

    /** 마스터 등 수동 전송 (refTyp/refId/refNo/ifTyp/sysCd/payload) */
    @PostMapping("/send")
    public ApiResponse<Long> send(@RequestBody Map<String, Object> b, @AuthenticationPrincipal LoginUser user) {
        Long refId = b.get("refId") == null ? null : Long.valueOf(String.valueOf(b.get("refId")));
        Long id = ifService.send((String) b.get("ifTyp"), (String) b.get("ifNm"), (String) b.getOrDefault("sysCd", "SAP"),
                (String) b.get("refTyp"), refId, (String) b.get("refNo"), (String) b.get("payload"),
                user.compCd(), user.usrId());
        return ApiResponse.ok("전송되었습니다.", id);
    }
}
