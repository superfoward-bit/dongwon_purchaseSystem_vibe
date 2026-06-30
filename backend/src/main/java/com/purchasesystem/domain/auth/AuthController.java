package com.purchasesystem.domain.auth;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.domain.auth.dto.LoginRequest;
import com.purchasesystem.domain.auth.dto.LoginResponse;
import com.purchasesystem.security.jwt.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<LoginUser> me(@AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok(user);
    }
}
