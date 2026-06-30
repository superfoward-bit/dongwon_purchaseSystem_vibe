package com.purchasesystem.domain.item;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.item.mapper.AttrMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 분류별 속성 정의 */
@RestController
@RequestMapping("/api/base/cate-attr")
@RequiredArgsConstructor
public class CategoryAttrController {

    private final AttrMapper attrMapper;

    @GetMapping
    public ApiResponse<List<ItCateAttr>> list(@RequestParam String cateCd) {
        return ApiResponse.ok(attrMapper.findCateAttrs(cateCd));
    }

    /** 분류 속성 일괄 저장(교체) */
    @PutMapping
    @Transactional
    public ApiResponse<Void> save(@RequestParam String cateCd, @RequestBody List<ItCateAttr> attrs,
                                  @AuthenticationPrincipal LoginUser user) {
        attrMapper.deleteCateAttrs(cateCd);
        int seq = 1;
        for (ItCateAttr a : attrs) {
            a.setCateCd(cateCd);
            a.setAttrSeq(seq);
            a.setSortNo(seq++);
            attrMapper.insertCateAttr(a);
        }
        return ApiResponse.ok("저장되었습니다.", null);
    }
}
