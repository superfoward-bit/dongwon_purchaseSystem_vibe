package com.purchasesystem.domain.cl;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.cl.mapper.ClAdjMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 정산 조정율 마스터 */
@RestController
@RequestMapping("/api/pro/cladj")
@RequiredArgsConstructor
public class ClAdjRateController {

    private final ClAdjMapper adjMapper;

    @GetMapping
    public ApiResponse<List<ClAdjRate>> list(@RequestParam(required = false) String vdCd,
                                             @RequestParam(required = false) String adjTyp) {
        return ApiResponse.ok(adjMapper.findRateList(vdCd, adjTyp));
    }

    @GetMapping("/{id}")
    public ApiResponse<ClAdjRate> detail(@PathVariable Long id) {
        return ApiResponse.ok(adjMapper.findRateById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody ClAdjRate r, @AuthenticationPrincipal LoginUser user) {
        r.setCompCd(user.compCd());
        r.setRegId(user.usrId());
        adjMapper.insertRate(r);
        return ApiResponse.ok("등록되었습니다.", r.getId());
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody ClAdjRate r, @AuthenticationPrincipal LoginUser user) {
        r.setId(id);
        r.setRegId(user.usrId());
        adjMapper.updateRate(r);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        adjMapper.deleteRate(id, user.usrId());
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
