package com.purchasesystem.domain.approval;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    /** 내 결재함 (done=false: 대기, true: 처리완료) */
    @GetMapping("/inbox")
    public ApiResponse<List<AppLine>> inbox(@RequestParam(defaultValue = "false") boolean done,
                                            @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(approvalService.getInbox(user.usrId(), done));
    }

    /** 내 상신함 */
    @GetMapping("/draft")
    public ApiResponse<List<AppApproval>> drafts(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(approvalService.getDrafts(user.usrId()));
    }

    /** 문서별 결재 현황 */
    @GetMapping("/by-doc")
    public ApiResponse<AppApproval> byDoc(@RequestParam String docTyp, @RequestParam Long docId) {
        return ApiResponse.ok(approvalService.getByDoc(docTyp, docId));
    }

    @PostMapping("/{lineId}/approve")
    public ApiResponse<String> approve(@PathVariable Long lineId, @RequestBody(required = false) Map<String, String> body,
                                       @AuthenticationPrincipal LoginUser user) {
        String opinion = body == null ? null : body.get("opinion");
        return ApiResponse.ok("승인되었습니다.", approvalService.approve(lineId, opinion, user));
    }

    /** 전결 (이후 단계 생략하고 완료) */
    @PostMapping("/{lineId}/final")
    public ApiResponse<String> approveFinal(@PathVariable Long lineId, @RequestBody(required = false) Map<String, String> body,
                                            @AuthenticationPrincipal LoginUser user) {
        String opinion = body == null ? null : body.get("opinion");
        return ApiResponse.ok("전결 처리되었습니다.", approvalService.approveFinal(lineId, opinion, user));
    }

    @PostMapping("/{lineId}/reject")
    public ApiResponse<String> reject(@PathVariable Long lineId, @RequestBody Map<String, String> body,
                                      @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("반려되었습니다.", approvalService.reject(lineId, body.get("opinion"), user));
    }
}
