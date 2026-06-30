package com.purchasesystem.config;

import com.purchasesystem.domain.user.CmUser;
import com.purchasesystem.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 최초 기동 시 관리자 계정이 없으면 생성한다. (비밀번호 BCrypt 암호화)
 * 기본 계정 - 로그인ID: admin / 비밀번호: admin1234
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public ApplicationRunner initAdmin() {
        return (ApplicationArguments args) -> {
            if (userMapper.countByLoginId("admin") > 0) {
                log.info("[DataInitializer] admin 계정 이미 존재 - skip");
                return;
            }
            CmUser admin = new CmUser();
            admin.setCompCd("1000");
            admin.setUsrId("admin");
            admin.setUsrNm("시스템관리자");
            admin.setLoginId("admin");
            admin.setPwdHash(passwordEncoder.encode("admin1234"));
            admin.setEmail("admin@purchasesystem.local");
            admin.setUsrTyp("EMP");
            userMapper.insertUser(admin);

            // 관리자 역할 부여
            userMapper.insertUserRole("1000", "admin", "ADMIN");
            log.info("[DataInitializer] admin 계정 생성 완료 (admin / admin1234)");
        };
    }
}
