package com.purchasesystem.domain.qc;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.notice.NotificationService;
import com.purchasesystem.domain.qc.mapper.QcMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QcService {

    private static final String DOC_TYP = "QC";

    private final QcMapper qcMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final NotificationService notificationService;

    public List<QcNonconf> getList(String keyword, String sts, String vdCd) { return qcMapper.findList(keyword, sts, vdCd); }

    public QcNonconf getDetail(Long id) {
        QcNonconf n = qcMapper.findById(id);
        if (n == null) throw new BusinessException("부적합을 찾을 수 없습니다.");
        n.setActionList(qcMapper.findActions(id));
        n.setActions(statusService.availableActions(DOC_TYP, n.getSts()));
        n.setHistory(statusService.history(DOC_TYP, id));
        return n;
    }

    @Transactional
    public Long create(QcNonconf n, LoginUser user) {
        if (n.getVdCd() == null) throw new BusinessException("협력사를 선택하세요.");
        n.setCompCd(user.compCd());
        n.setSts("OPEN");
        n.setRegId(user.usrId());
        n.setNcNo(docNoService.generate(DOC_TYP));
        qcMapper.insert(n);
        saveActions(n);
        return n.getId();
    }

    @Transactional
    public void update(Long id, QcNonconf n, LoginUser user) {
        QcNonconf cur = qcMapper.findById(id);
        if (cur == null) throw new BusinessException("부적합을 찾을 수 없습니다.");
        if ("CLOSED".equals(cur.getSts()) || "CANCEL".equals(cur.getSts()))
            throw new BusinessException("종료/취소된 부적합은 수정할 수 없습니다.");
        n.setId(id);
        n.setModId(user.usrId());
        qcMapper.update(n);
        qcMapper.deleteActions(id);
        saveActions(n);
    }

    /** 상태전이: 개선요청(협력사 알림)/조치착수/검증종료/보완요청/취소 */
    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        QcNonconf cur = qcMapper.findById(id);
        if (cur == null) throw new BusinessException("부적합을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getNcNo(), cur.getSts(), actionCode, reason, user.usrId());
        qcMapper.updateStatus(id, toSts, user.usrId());
        if ("REQUEST".equals(actionCode)) {
            // 협력사에 개선요청 통보 (시뮬레이션 발송 이력)
            try {
                notificationService.notify("EMAIL", null, cur.getVdCd(),
                        "[품질 개선요청] " + cur.getNcNo(),
                        "부적합(" + cur.getNcTyp() + ") 발생으로 개선대책을 제출해 주세요. 품목: " + cur.getItemNm(),
                        DOC_TYP, id, "NC_REQUEST", cur.getCompCd(), user.usrId());
            } catch (Exception ignore) { }
        }
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        QcNonconf cur = qcMapper.findById(id);
        if (cur == null) return;
        if (!"OPEN".equals(cur.getSts())) throw new BusinessException("접수 상태에서만 삭제할 수 있습니다.");
        qcMapper.deleteActions(id);
        qcMapper.delete(id, user.usrId());
    }

    private void saveActions(QcNonconf n) {
        if (n.getActionList() == null) return;
        int line = 1;
        for (QcAction a : n.getActionList()) {
            a.setNcId(n.getId());
            a.setLineNo(line++);
            qcMapper.insertAction(a);
        }
    }
}
