package com.purchasesystem.domain.menu;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /** 로그인 사용자의 메뉴 트리 */
    @GetMapping("/my")
    public ApiResponse<List<CmMenu>> myMenu(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(menuService.getUserMenuTree(user.compCd(), user.usrId()));
    }

    /** 전체 메뉴 트리 (관리) */
    @GetMapping("/all")
    public ApiResponse<List<CmMenu>> all() {
        return ApiResponse.ok(menuService.getAllMenuTree());
    }

    /** 전체 메뉴 평면 목록 (상위메뉴 선택용) */
    @GetMapping("/flat")
    public ApiResponse<List<CmMenu>> flat() {
        return ApiResponse.ok(menuService.getAllMenuFlat());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody CmMenu menu, @AuthenticationPrincipal LoginUser user) {
        menuService.createMenu(menu, user.usrId());
        return ApiResponse.ok("등록되었습니다.", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody CmMenu menu,
                                    @AuthenticationPrincipal LoginUser user) {
        menu.setId(id);
        menuService.updateMenu(menu, user.usrId());
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam(required = false) String menuId,
                                    @AuthenticationPrincipal LoginUser user) {
        menuService.deleteMenu(id, menuId, user.usrId());
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
