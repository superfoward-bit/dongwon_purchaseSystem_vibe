package com.purchasesystem.domain.eval;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/srm/sheet")
@RequiredArgsConstructor
public class SheetController {

    private final EvalService evalService;

    @GetMapping
    public ApiResponse<List<VdEvalSheet>> list() {
        return ApiResponse.ok(evalService.getSheets());
    }

    @GetMapping("/{sheetCd}")
    public ApiResponse<VdEvalSheet> detail(@PathVariable String sheetCd) {
        return ApiResponse.ok(evalService.getSheetDetail(sheetCd));
    }

    @GetMapping("/grades")
    public ApiResponse<List<VdEvalGrade>> grades() {
        return ApiResponse.ok(evalService.getGrades());
    }

    @PostMapping
    public ApiResponse<Void> create(@RequestBody VdEvalSheet s, @AuthenticationPrincipal LoginUser user) {
        evalService.createSheet(s, user);
        return ApiResponse.ok("등록되었습니다.", null);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody VdEvalSheet s,
                                    @AuthenticationPrincipal LoginUser user) {
        evalService.updateSheet(id, s, user);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @RequestParam(required = false) String sheetCd,
                                    @AuthenticationPrincipal LoginUser user) {
        evalService.deleteSheet(id, sheetCd, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
