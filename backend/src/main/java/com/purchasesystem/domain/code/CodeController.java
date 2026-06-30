package com.purchasesystem.domain.code;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/code")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    /** 코드그룹 목록 */
    @GetMapping("/groups")
    public ApiResponse<List<CmCodeGrp>> groups(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(codeService.getGroups(keyword));
    }

    /** 그룹 내 코드 목록 */
    @GetMapping("/codes")
    public ApiResponse<List<CmCode>> codes(@RequestParam String grpCd) {
        return ApiResponse.ok(codeService.getCodes(grpCd));
    }

    @PostMapping("/codes")
    public ApiResponse<Void> create(@RequestBody CmCode code, @AuthenticationPrincipal LoginUser user) {
        codeService.createCode(code, user.usrId());
        return ApiResponse.ok("등록되었습니다.", null);
    }

    @PutMapping("/codes/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CmCode code,
                                    @AuthenticationPrincipal LoginUser user) {
        code.setId(id);
        codeService.updateCode(code, user.usrId());
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/codes/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        codeService.deleteCode(id, user.usrId());
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
