package com.purchasesystem.domain.notice;

import com.purchasesystem.domain.notice.mapper.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 알림 발송 서비스: 발송이력(CM_NOTICE) 기록 + 게이트웨이(시뮬레이션) 발송.
 * 도메인 서비스에 의존하지 않아 어디서든 호출 가능.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NoticeMapper noticeMapper;
    private final List<NotificationSender> senderList;
    private Map<String, NotificationSender> senders;

    private NotificationSender sender(String notiTyp) {
        if (senders == null) {
            senders = senderList.stream().collect(Collectors.toMap(NotificationSender::notiTyp, Function.identity()));
        }
        return senders.get(notiTyp);
    }

    public List<CmNotice> getList(String notiTyp, String sendSts, String toUsrId, String refTyp, Long refId) {
        return noticeMapper.findList(notiTyp, sendSts, toUsrId, refTyp, refId);
    }

    /** 알림 발송: 이력 저장 후 게이트웨이 발송, 결과 갱신. 실패해도 호출측 트랜잭션은 영향받지 않도록 예외를 삼킨다. */
    @Transactional
    public Long notify(String notiTyp, String toAddr, String toUsrId, String title, String content,
                       String refTyp, Long refId, String eventCd, String compCd, String regId) {
        CmNotice n = new CmNotice();
        n.setCompCd(compCd);
        n.setNotiTyp(notiTyp);
        n.setToAddr(toAddr);
        n.setToUsrId(toUsrId);
        n.setTitle(title);
        n.setContent(content);
        n.setRefTyp(refTyp);
        n.setRefId(refId);
        n.setEventCd(eventCd);
        n.setSendSts("READY");
        n.setRegId(regId);
        noticeMapper.insert(n);

        NotificationSender s = sender(notiTyp);
        try {
            if (s == null) throw new IllegalStateException("발송 게이트웨이 없음: " + notiTyp);
            s.send(n);
            noticeMapper.updateResult(n.getId(), "SENT", null);
        } catch (Exception e) {
            noticeMapper.updateResult(n.getId(), "FAIL", e.getMessage());
        }
        return n.getId();
    }

    /** 이메일 간편 발송 */
    public Long email(String toAddr, String toUsrId, String title, String content,
                      String refTyp, Long refId, String eventCd, String compCd, String regId) {
        return notify("EMAIL", toAddr, toUsrId, title, content, refTyp, refId, eventCd, compCd, regId);
    }
}
