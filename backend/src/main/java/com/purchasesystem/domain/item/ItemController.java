package com.purchasesystem.domain.item;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/base/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ApiResponse<List<ItItem>> list(@RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) String cateCd,
                                          @RequestParam(required = false) String itemSts) {
        return ApiResponse.ok(itemService.getList(keyword, cateCd, itemSts));
    }

    @GetMapping("/{id}")
    public ApiResponse<ItItem> detail(@PathVariable Long id) {
        return ApiResponse.ok(itemService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody ItItem it, @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("등록되었습니다.", itemService.create(it, user));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ItItem it,
                                    @AuthenticationPrincipal LoginUser user) {
        itemService.update(id, it, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        itemService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
