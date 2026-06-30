package com.purchasesystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 신규 통합 구매시스템 진입점.
 * DIPS + DFS(eMRO SmartSuite9) 기능 합집합을 현대화 스택(Spring Boot 3 / MyBatis / Vue)으로 재구축.
 */
@SpringBootApplication
@MapperScan("com.purchasesystem.**.mapper")
public class PurchaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PurchaseApplication.class, args);
    }
}
