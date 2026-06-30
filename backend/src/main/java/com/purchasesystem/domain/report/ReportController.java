package com.purchasesystem.domain.report;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.report.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportMapper reportMapper;

    private String yr(String year) { return (year == null || year.isBlank()) ? String.valueOf(LocalDate.now().getYear()) : year; }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(@RequestParam(required = false) String year) {
        return ApiResponse.ok(reportMapper.summary(yr(year)));
    }

    @GetMapping("/gr-monthly")
    public ApiResponse<List<Map<String, Object>>> grMonthly(@RequestParam(required = false) String year) {
        return ApiResponse.ok(reportMapper.grMonthly(yr(year)));
    }

    @GetMapping("/gr-vendor")
    public ApiResponse<List<Map<String, Object>>> grVendor(@RequestParam(required = false) String year) {
        return ApiResponse.ok(reportMapper.grByVendor(yr(year)));
    }

    @GetMapping("/gr-item")
    public ApiResponse<List<Map<String, Object>>> grItem(@RequestParam(required = false) String year) {
        return ApiResponse.ok(reportMapper.grByItem(yr(year)));
    }

    @GetMapping("/po-monthly")
    public ApiResponse<List<Map<String, Object>>> poMonthly(@RequestParam(required = false) String year) {
        return ApiResponse.ok(reportMapper.poMonthly(yr(year)));
    }

    @GetMapping("/price-history")
    public ApiResponse<List<Map<String, Object>>> priceHistory(@RequestParam(required = false) String vdCd,
                                                               @RequestParam(required = false) String itemCd) {
        return ApiResponse.ok(reportMapper.priceHistory(vdCd, itemCd));
    }

    @GetMapping("/settle-adj")
    public ApiResponse<List<Map<String, Object>>> settleAdj(@RequestParam(required = false) String closeYm) {
        return ApiResponse.ok(reportMapper.settleAdj(closeYm));
    }
}
