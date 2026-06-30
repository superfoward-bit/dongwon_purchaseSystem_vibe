package com.purchasesystem.domain.dashboard.dto;

import lombok.Data;

/** 상태/구분별 건수 집계. */
@Data
public class StatCount {
    private String code;
    private String name;
    private Integer cnt;
}
