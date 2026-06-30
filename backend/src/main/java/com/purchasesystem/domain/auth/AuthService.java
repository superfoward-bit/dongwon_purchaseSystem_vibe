package com.purchasesystem.domain.auth;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.auth.dto.LoginRequest;
import com.purchasesystem.domain.auth.dto.LoginResponse;
import com.purchasesystem.domain.user.CmUser;
import com.purchasesystem.domain.user.mapper.UserMapper;
import com.purchasesystem.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public LoginResponse login(LoginRequest req) {
        CmUser user = userMapper.findByLoginId(req.loginId());
        if (user == null) {
            throw new BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!"Y".equals(user.getUseYn())) {
            throw new BusinessException("사용할 수 없는 계정입니다.");
        }
        if (user.getPwdHash() == null || !passwordEncoder.matches(req.password(), user.getPwdHash())) {
            throw new BusinessException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        userMapper.updateLastLogin(user.getLoginId());
        List<String> roles = userMapper.findRoleIds(user.getCompCd(), user.getUsrId());
        String token = tokenProvider.createAccessToken(
                user.getLoginId(), user.getCompCd(), user.getUsrId(), user.getUsrNm(), user.getVdCd());

        return new LoginResponse(token, user.getUsrId(), user.getUsrNm(), user.getCompCd(), roles);
    }
}
