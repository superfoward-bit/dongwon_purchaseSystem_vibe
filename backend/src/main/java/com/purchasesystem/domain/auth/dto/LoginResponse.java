package com.purchasesystem.domain.auth.dto;

import java.util.List;

public record LoginResponse(
        String accessToken,
        String usrId,
        String usrNm,
        String compCd,
        List<String> roles) {
}
