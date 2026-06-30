package com.purchasesystem.domain.approval;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.approval.mapper.DelegateMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 결재 위임/대결 */
@RestController
@RequestMapping("/api/approval/delegate")
@RequiredArgsConstructor
public class DelegateController {

    private final DelegateMapper delegateMapper;

    @GetMapping
    public ApiResponse<List<AppDelegate>> list(@RequestParam(required = false) String fromUsrId,
                                               @RequestParam(required = false) String toUsrId) {
        return ApiResponse.ok(delegateMapper.findList(fromUsrId, toUsrId));
    }

    @GetMapping("/{id}")
    public ApiResponse<AppDelegate> detail(@PathVariable Long id) {
        return ApiResponse.ok(delegateMapper.findById(id));
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody AppDelegate d, @AuthenticationPrincipal LoginUser user) {
        d.setCompCd(user.compCd());
        d.setRegId(user.usrId());
        delegateMapper.insert(d);
        return ApiResponse.ok("등록되었습니다.", d.getId());
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody AppDelegate d, @AuthenticationPrincipal LoginUser user) {
        d.setId(id);
        d.setRegId(user.usrId());
        delegateMapper.update(d);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        delegateMapper.delete(id, user.usrId());
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
