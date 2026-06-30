package com.purchasesystem.domain.stock;

import com.purchasesystem.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inv")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /** 재고 현황 */
    @GetMapping("/stock")
    public ApiResponse<List<StStock>> stock(@RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) String whCd,
                                            @RequestParam(required = false) String zeroYn) {
        return ApiResponse.ok(stockService.getStockList(keyword, whCd, zeroYn));
    }

    /** 특정 품목의 재고(LOT별) — 출고 시 선택용 */
    @GetMapping("/stock/by-item")
    public ApiResponse<List<StStock>> byItem(@RequestParam String itemCd, @RequestParam(required = false) String whCd) {
        return ApiResponse.ok(stockService.getStockByItem(whCd, itemCd));
    }

    /** 수불부 */
    @GetMapping("/ledger")
    public ApiResponse<List<StLedger>> ledger(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String itemCd,
                                              @RequestParam(required = false) String whCd,
                                              @RequestParam(required = false) String transTyp) {
        return ApiResponse.ok(stockService.getLedger(keyword, itemCd, whCd, transTyp));
    }
}
