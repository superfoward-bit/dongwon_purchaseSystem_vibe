package com.purchasesystem.domain.stock;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/** 재고실사 (ST_COUNT). */
@Data
public class StCount {
    private Long id;
    private String compCd;
    private String countNo;
    private String whCd;
    private String whNm;
    private String countYmd;
    private String sts;
    private String stsNm;
    private String remark;
    private String regId;
    private String regDt;
    private String modId;

    private List<StCountItem> items = new ArrayList<>();
}
