package com.purchasesystem.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "로그인ID를 입력하세요") String loginId,
        @NotBlank(message = "비밀번호를 입력하세요") String password) {
}
