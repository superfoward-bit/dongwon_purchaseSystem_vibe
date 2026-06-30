package com.purchasesystem.domain.dashboard;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.dashboard.mapper.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardMapper mapper;

    @GetMapping
    public ApiResponse<Map<String, Object>> summary() {
        Map<String, Object> m = new LinkedHashMap<>();
        Map<String, Object> cards = new LinkedHashMap<>();
        cards.put("prOpen", mapper.countPrOpen());
        cards.put("poDone", mapper.countPoDone());
        cards.put("grConfirmed", mapper.countGrConfirmed());
        cards.put("rfxOpen", mapper.countRfxOpen());
        cards.put("poAmtThisMonth", mapper.poAmtThisMonth());
        m.put("cards", cards);
        m.put("prByStatus", mapper.prByStatus());
        m.put("poByStatus", mapper.poByStatus());
        m.put("grByStatus", mapper.grByStatus());
        m.put("vendorByGrade", mapper.vendorByGrade());
        m.put("poMonthly", mapper.poMonthly());
        m.put("recentPo", mapper.recentPo());
        m.put("recentGr", mapper.recentGr());
        return ApiResponse.ok(m);
    }
}
