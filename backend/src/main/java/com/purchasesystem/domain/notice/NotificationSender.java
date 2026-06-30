package com.purchasesystem.domain.notice;

/**
 * 알림 발송 게이트웨이 추상화.
 * 현재는 시뮬레이션 구현만 존재. 실제 SMTP/SMS 게이트웨이 연동 시 이 인터페이스를 구현하여 교체.
 */
public interface NotificationSender {
    /** 지원 유형 (EMAIL / SMS) */
    String notiTyp();
    /** 발송 (실패 시 예외) */
    void send(CmNotice notice) throws Exception;
}
