package com.purchasesystem.domain.item;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ItemService itemService;

    @GetMapping
    public ApiResponse<List<ItCategory>> list() {
        return ApiResponse.ok(itemService.getCategories());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody ItCategory c, @AuthenticationPrincipal LoginUser user) {
        itemService.createCategory(c, user);
        return ApiResponse.ok("등록되었습니다.", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ItCategory c,
                                    @AuthenticationPrincipal LoginUser user) {
        itemService.updateCategory(id, c, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam(required = false) String cateCd,
                                    @AuthenticationPrincipal LoginUser user) {
        itemService.deleteCategory(id, cateCd, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
