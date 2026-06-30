package com.purchasesystem.domain.approval;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.approval.mapper.ApprovalMapper;
import com.purchasesystem.domain.approval.mapper.DelegateMapper;
import com.purchasesystem.domain.notice.NotificationService;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalMapper approvalMapper;
    private final DelegateMapper delegateMapper;
    private final DocNoService docNoService;
    private final NotificationService notificationService;
    private final List<ApprovableHandler> handlerList;

    private void notifySafe(String toUsrId, String title, String content, AppApproval a, String event) {
        try {
            notificationService.notify("EMAIL", null, toUsrId, title, content,
                    a.getDocTyp(), a.getDocId(), event, a.getCompCd(), "SYSTEM");
        } catch (Exception ignore) { /* 알림 실패는 결재 처리에 영향 없음 */ }
    }

    private Map<String, ApprovableHandler> handlers;

    private ApprovableHandler handler(String docTyp) {
        if (handlers == null) {
            handlers = handlerList.stream().collect(Collectors.toMap(ApprovableHandler::docTyp, Function.identity()));
        }
        return handlers.get(docTyp);
    }

    private String today() { return LocalDate.now().toString(); }

    /**
     * 결재 상신: 결재선 생성.
     * 결재유형 APRV(결재)/AGRE(합의)/REFR(참조). 참조는 즉시 REFER, 첫 결재/합의 단계만 ING.
     */
    @Transactional
    public Long create(String docTyp, Long docId, String docNo, String title,
                       List<Map<String, String>> approvers, LoginUser drafter) {
        if (approvers == null || approvers.isEmpty()) throw new BusinessException("결재선(결재자)을 지정하세요.");

        AppApproval a = new AppApproval();
        a.setCompCd(drafter.compCd());
        a.setAprvNo(docNoService.generate("AP"));
        a.setDocTyp(docTyp);
        a.setDocId(docId);
        a.setDocNo(docNo);
        a.setAprvTitle(title);
        a.setDraftUsrId(drafter.usrId());
        a.setDraftUsrNm(drafter.usrNm());
        approvalMapper.insertApproval(a);

        int step = 1;
        boolean firstActiveAssigned = false;
        String firstApproverId = null;
        for (Map<String, String> ap : approvers) {
            String lineTyp = ap.getOrDefault("lineTyp", "APRV");
            if (lineTyp == null || lineTyp.isBlank()) lineTyp = "APRV";
            String finalYn = "Y".equals(ap.get("finalYn")) ? "Y" : "N";

            AppLine l = new AppLine();
            l.setAprvId(a.getId());
            l.setStepNo(step);
            l.setAprvUsrId(ap.get("usrId"));
            l.setAprvUsrNm(ap.get("usrNm"));
            l.setLineTyp(lineTyp);
            l.setFinalYn(finalYn);

            if ("REFR".equals(lineTyp)) {
                l.setLineSts("REFER");                 // 참조: 진행 비차단
            } else if (!firstActiveAssigned) {
                l.setLineSts("ING");                   // 첫 결재/합의 단계
                firstActiveAssigned = true;
                firstApproverId = ap.get("usrId");
            } else {
                l.setLineSts("WAIT");
            }
            approvalMapper.insertLine(l);
            step++;
        }
        if (firstApproverId != null) {
            notifySafe(firstApproverId, "[결재요청] " + title,
                    drafter.usrNm() + " 님이 결재를 요청했습니다. (" + docNo + ")", a, "SUBMIT");
        }
        return a.getId();
    }

    public AppApproval getByDoc(String docTyp, Long docId) {
        AppApproval a = approvalMapper.findActiveByDoc(docTyp, docId);
        if (a != null) a.setLines(approvalMapper.findLines(a.getId()));
        return a;
    }

    /** 내 결재함 (위임받은 건 포함) */
    public List<AppLine> getInbox(String usrId, boolean done) {
        List<String> delegators = new ArrayList<>();
        delegators.add(usrId);
        delegators.addAll(delegateMapper.findDelegatorsTo(usrId, today()));
        return approvalMapper.findInbox(usrId, done, delegators.stream().distinct().toList());
    }

    public List<AppApproval> getDrafts(String usrId) { return approvalMapper.findDrafts(usrId); }

    /** 처리 권한 검증: 본인 또는 위임받은 대결자 */
    private void assertActor(AppLine line, LoginUser user) {
        if (user.usrId().equals(line.getAprvUsrId())) return;
        if (delegateMapper.countActive(line.getAprvUsrId(), user.usrId(), today()) > 0) return;
        throw new BusinessException("본인 결재 차례가 아닙니다.");
    }

    private String actAs(AppLine line, LoginUser user) {
        return user.usrId().equals(line.getAprvUsrId()) ? null : user.usrId();
    }

    /** 다음 진행할 결재/합의 단계(참조 제외)로 진행하거나 완료 처리 */
    private String advanceOrComplete(AppApproval a, AppLine current, LoginUser user) {
        List<AppLine> lines = approvalMapper.findLines(a.getId());
        AppLine next = lines.stream()
                .filter(l -> l.getStepNo() > current.getStepNo())
                .filter(l -> "APRV".equals(l.getLineTyp()) || "AGRE".equals(l.getLineTyp()))
                .filter(l -> "WAIT".equals(l.getLineSts()))
                .min((x, y) -> Integer.compare(x.getStepNo(), y.getStepNo()))
                .orElse(null);
        if (next != null) {
            approvalMapper.setLineIng(a.getId(), next.getStepNo());
            approvalMapper.updateApprovalStatus(a.getId(), "ING", next.getStepNo());
            notifySafe(next.getAprvUsrId(), "[결재요청] " + a.getAprvTitle(),
                    "다음 단계 결재 차례입니다. (" + a.getDocNo() + ")", a, "SUBMIT");
            return "ING";
        }
        approvalMapper.updateApprovalStatus(a.getId(), "DONE", current.getStepNo());
        notifySafe(a.getDraftUsrId(), "[결재완료] " + a.getAprvTitle(),
                "상신하신 문서가 결재완료되었습니다. (" + a.getDocNo() + ")", a, "APPROVED");
        ApprovableHandler h = handler(a.getDocTyp());
        if (h != null) h.onApprovalCompleted(a.getDocId(), user.usrId());
        return "DONE";
    }

    /** 승인/합의 (현재 단계 처리자 또는 대결자) */
    @Transactional
    public String approve(Long lineId, String opinion, LoginUser user) {
        AppLine line = approvalMapper.findLine(lineId);
        if (line == null) throw new BusinessException("결재 건을 찾을 수 없습니다.");
        if (!"ING".equals(line.getLineSts())) throw new BusinessException("이미 처리되었거나 대기 단계입니다.");
        assertActor(line, user);

        AppApproval a = approvalMapper.findById(line.getAprvId());
        String doneSts = "AGRE".equals(line.getLineTyp()) ? "AGREE" : "APPROVE";
        approvalMapper.updateLineStatusAs(lineId, doneSts, opinion, actAs(line, user));
        return advanceOrComplete(a, line, user);
    }

    /** 전결: 전결권 보유 단계에서 이후 결재/합의 단계를 생략하고 완료 */
    @Transactional
    public String approveFinal(Long lineId, String opinion, LoginUser user) {
        AppLine line = approvalMapper.findLine(lineId);
        if (line == null) throw new BusinessException("결재 건을 찾을 수 없습니다.");
        if (!"ING".equals(line.getLineSts())) throw new BusinessException("이미 처리되었거나 대기 단계입니다.");
        if (!"Y".equals(line.getFinalYn())) throw new BusinessException("전결 권한이 없는 단계입니다.");
        assertActor(line, user);

        AppApproval a = approvalMapper.findById(line.getAprvId());
        approvalMapper.updateLineStatusAs(lineId, "FINAL", opinion, actAs(line, user));
        approvalMapper.skipRemaining(a.getId(), line.getStepNo());
        approvalMapper.updateApprovalStatus(a.getId(), "DONE", line.getStepNo());
        notifySafe(a.getDraftUsrId(), "[전결완료] " + a.getAprvTitle(),
                "상신하신 문서가 전결 처리되었습니다. (" + a.getDocNo() + ")", a, "APPROVED");
        ApprovableHandler h = handler(a.getDocTyp());
        if (h != null) h.onApprovalCompleted(a.getDocId(), user.usrId());
        return "FINAL";
    }

    /** 반려 */
    @Transactional
    public String reject(Long lineId, String opinion, LoginUser user) {
        if (opinion == null || opinion.isBlank()) throw new BusinessException("반려 사유를 입력하세요.");
        AppLine line = approvalMapper.findLine(lineId);
        if (line == null) throw new BusinessException("결재 건을 찾을 수 없습니다.");
        if (!"ING".equals(line.getLineSts())) throw new BusinessException("이미 처리되었거나 대기 단계입니다.");
        assertActor(line, user);

        AppApproval a = approvalMapper.findById(line.getAprvId());
        approvalMapper.updateLineStatusAs(lineId, "REJECT", opinion, actAs(line, user));
        approvalMapper.updateApprovalStatus(a.getId(), "REJECT", line.getStepNo());
        notifySafe(a.getDraftUsrId(), "[반려] " + a.getAprvTitle(),
                "상신하신 문서가 반려되었습니다. 사유: " + opinion + " (" + a.getDocNo() + ")", a, "REJECTED");
        ApprovableHandler h = handler(a.getDocTyp());
        if (h != null) h.onApprovalRejected(a.getDocId(), user.usrId(), opinion);
        return "REJECT";
    }

    /** 문서 회수 시 진행중 결재 취소 */
    @Transactional
    public void cancelByDoc(String docTyp, Long docId) {
        AppApproval a = approvalMapper.findActiveByDoc(docTyp, docId);
        if (a != null && "ING".equals(a.getAprvSts())) {
            approvalMapper.updateApprovalStatus(a.getId(), "RECALL", a.getCurStepNo());
        }
    }
}
