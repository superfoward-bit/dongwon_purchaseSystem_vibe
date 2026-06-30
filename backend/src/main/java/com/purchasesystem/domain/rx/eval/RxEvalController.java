package com.purchasesystem.domain.rx.eval;

import com.purchasesystem.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** RFP 평가입찰 */
@RestController
@RequestMapping("/api/pro/rx/{rfxId}/eval")
@RequiredArgsConstructor
public class RxEvalController {

    private final RxEvalService evalService;

    @GetMapping("/setup")
    public ApiResponse<RxEvalDtos.Setup> getSetup(@PathVariable Long rfxId) {
        return ApiResponse.ok(evalService.getSetup(rfxId));
    }

    @PutMapping("/setup")
    public ApiResponse<Void> saveSetup(@PathVariable Long rfxId, @RequestBody RxEvalDtos.Setup setup) {
        evalService.saveSetup(rfxId, setup);
        return ApiResponse.ok("평가설정이 저장되었습니다.", null);
    }

    @GetMapping("/scores")
    public ApiResponse<List<RxEvalScore>> getScores(@PathVariable Long rfxId) {
        return ApiResponse.ok(evalService.getScores(rfxId));
    }

    @PutMapping("/scores")
    public ApiResponse<Void> saveScores(@PathVariable Long rfxId, @RequestBody List<RxEvalScore> scores) {
        evalService.saveScores(rfxId, scores);
        return ApiResponse.ok("평가점수가 저장되었습니다.", null);
    }

    @GetMapping("/result")
    public ApiResponse<List<RxEvalDtos.Result>> result(@PathVariable Long rfxId) {
        return ApiResponse.ok(evalService.computeResult(rfxId));
    }
}
