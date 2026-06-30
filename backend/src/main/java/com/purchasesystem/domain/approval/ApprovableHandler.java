package com.purchasesystem.domain.approval;

/**
 * 결재 대상 문서 핸들러. 결재 완료/반려 시 원문서 상태를 갱신한다.
 * 도메인별(PR/PO…)로 구현하여 결재엔진과 느슨하게 연결한다.
 */
public interface ApprovableHandler {
    String docTyp();
    void onApprovalCompleted(Long docId, String actorId);
    void onApprovalRejected(Long docId, String actorId, String reason);
}
