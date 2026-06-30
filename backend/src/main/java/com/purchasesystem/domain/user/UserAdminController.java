package com.purchasesystem.domain.user;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @GetMapping
    public ApiResponse<List<CmUser>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(userAdminService.getUsers(keyword));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody CmUser user, @AuthenticationPrincipal LoginUser actor) {
        if (user.getCompCd() == null) user.setCompCd(actor.compCd());
        userAdminService.createUser(user, actor.usrId());
        return ApiResponse.ok("등록되었습니다. (초기 비밀번호: 1234)", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CmUser user,
                                    @AuthenticationPrincipal LoginUser actor) {
        user.setId(id);
        if (user.getCompCd() == null) user.setCompCd(actor.compCd());
        userAdminService.updateUser(user, actor.usrId());
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser actor) {
        userAdminService.deleteUser(id, actor.usrId());
        return ApiResponse.ok("삭제되었습니다.", null);
    }

    @PutMapping("/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @AuthenticationPrincipal LoginUser actor) {
        userAdminService.resetPassword(id, actor.usrId());
        return ApiResponse.ok("비밀번호가 1234로 초기화되었습니다.", null);
    }

    @GetMapping("/{compCd}/{usrId}/roles")
    public ApiResponse<List<String>> roles(@PathVariable String compCd, @PathVariable String usrId) {
        return ApiResponse.ok(userAdminService.getUserRoles(compCd, usrId));
    }
}
