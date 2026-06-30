package com.purchasesystem.domain.approval;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.approval.mapper.LineTplMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** 결재선 템플릿 (금액조건별) */
@RestController
@RequestMapping("/api/approval/template")
@RequiredArgsConstructor
public class LineTplController {

    private final LineTplMapper tplMapper;

    @GetMapping
    public ApiResponse<List<AppLineTpl>> list(@RequestParam(required = false) String docTyp) {
        return ApiResponse.ok(tplMapper.findList(docTyp));
    }

    @GetMapping("/{id}")
    public ApiResponse<AppLineTpl> detail(@PathVariable Long id) {
        AppLineTpl t = tplMapper.findById(id);
        if (t != null) t.setSteps(tplMapper.findSteps(id));
        return ApiResponse.ok(t);
    }

    /** 문서유형+금액으로 결재선 자동 추천 */
    @GetMapping("/resolve")
    public ApiResponse<List<AppLineTplStep>> resolve(@RequestParam String docTyp, @RequestParam BigDecimal amount,
                                                     @AuthenticationPrincipal LoginUser user) {
        AppLineTpl t = tplMapper.findMatch(user.compCd(), docTyp, amount);
        if (t == null) return ApiResponse.ok(List.of());
        return ApiResponse.ok(tplMapper.findSteps(t.getId()));
    }

    @PostMapping
    @Transactional
    public ApiResponse<Long> create(@RequestBody AppLineTpl t, @AuthenticationPrincipal LoginUser user) {
        t.setCompCd(user.compCd());
        t.setRegId(user.usrId());
        tplMapper.insertTpl(t);
        saveSteps(t);
        return ApiResponse.ok("등록되었습니다.", t.getId());
    }

    @PutMapping("/{id}")
    @Transactional
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody AppLineTpl t, @AuthenticationPrincipal LoginUser user) {
        t.setId(id);
        t.setRegId(user.usrId());
        tplMapper.updateTpl(t);
        tplMapper.deleteSteps(id);
        saveSteps(t);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        tplMapper.deleteSteps(id);
        tplMapper.deleteTpl(id, user.usrId());
        return ApiResponse.ok("삭제되었습니다.", null);
    }

    private void saveSteps(AppLineTpl t) {
        if (t.getSteps() == null) return;
        int step = 1;
        for (AppLineTplStep s : t.getSteps()) {
            s.setTplId(t.getId());
            s.setStepNo(step++);
            tplMapper.insertStep(s);
        }
    }
}
