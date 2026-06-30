package com.purchasesystem.domain.role;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ApiResponse<List<CmRole>> list(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(roleService.getRoles(keyword));
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody CmRole role, @AuthenticationPrincipal LoginUser user) {
        roleService.createRole(role, user.usrId());
        return ApiResponse.ok("등록되었습니다.", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CmRole role,
                                    @AuthenticationPrincipal LoginUser user) {
        role.setId(id);
        roleService.updateRole(role, user.usrId());
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        roleService.deleteRole(id, user.usrId());
        return ApiResponse.ok("삭제되었습니다.", null);
    }

    /** 역할 메뉴권한 조회 */
    @GetMapping("/{roleId}/funcs")
    public ApiResponse<List<CmRoleFunc>> funcs(@PathVariable String roleId) {
        return ApiResponse.ok(roleService.getRoleFuncs(roleId));
    }

    /** 역할 메뉴권한 일괄 저장 */
    @PutMapping("/{roleId}/funcs")
    public ApiResponse<Void> saveFuncs(@PathVariable String roleId, @RequestBody List<CmRoleFunc> funcs) {
        roleService.saveRoleFuncs(roleId, funcs);
        return ApiResponse.ok("권한이 저장되었습니다.", null);
    }
}
