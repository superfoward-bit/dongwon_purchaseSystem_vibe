package com.purchasesystem.domain.notice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 시뮬레이션 발송기 (실제 게이트웨이 미연동 환경).
 * 발송 내용을 로그로 남기고 성공 처리한다. 실제 SMTP/SMS 도입 시 별도 @Component 로 교체/추가.
 */
public class SimulatedSenders {

    @Slf4j
    @Component
    public static class SimulatedEmailSender implements NotificationSender {
        @Override public String notiTyp() { return "EMAIL"; }
        @Override public void send(CmNotice n) {
            log.info("[MAIL-SIM] to={} title={} :: {}", n.getToAddr(), n.getTitle(), n.getContent());
        }
    }

    @Slf4j
    @Component
    public static class SimulatedSmsSender implements NotificationSender {
        @Override public String notiTyp() { return "SMS"; }
        @Override public void send(CmNotice n) {
            log.info("[SMS-SIM] to={} :: {}", n.getToAddr(), n.getContent());
        }
    }
}
