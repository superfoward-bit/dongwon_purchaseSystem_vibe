package com.purchasesystem.common.status;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.mapper.StatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 공통 문서 상태전이 처리.
 * 전이 규칙(CM_STATUS_FLOW)을 검증하고, 이력(LOG_DOC_STATUS_HIS)을 남긴 뒤
 * 전이 후 상태(TO_STS)를 반환한다. 실제 문서 STS 갱신은 각 도메인이 수행한다.
 */
@Service
@RequiredArgsConstructor
public class StatusTransitionService {

    private final StatusMapper statusMapper;

    /** 현재 상태에서 가능한 액션 목록 (화면 버튼용) */
    public List<CmStatusFlow> availableActions(String docTyp, String fromSts) {
        return statusMapper.findActions(docTyp, fromSts);
    }

    public List<DocStatusHis> history(String docTyp, Long docId) {
        return statusMapper.findHis(docTyp, docId);
    }

    /**
     * 상태전이 수행. 규칙 검증 + 사유 필수 체크 + 이력 기록.
     * @return 전이 후 상태(TO_STS)
     */
    @Transactional
    public String transition(String docTyp, Long docId, String docNo, String fromSts,
                             String action, String rsn, String actorId) {
        CmStatusFlow flow = statusMapper.findFlow(docTyp, fromSts, action);
        if (flow == null) {
            throw new BusinessException("현재 상태(" + fromSts + ")에서 허용되지 않은 처리입니다: " + action);
        }
        if ("Y".equals(flow.getRsnReqYn()) && (rsn == null || rsn.isBlank())) {
            throw new BusinessException("[" + flow.getActionNm() + "] 처리에는 사유 입력이 필요합니다.");
        }

        DocStatusHis his = new DocStatusHis();
        his.setDocTyp(docTyp);
        his.setDocId(docId);
        his.setDocNo(docNo);
        his.setSeq(statusMapper.nextSeq(docTyp, docId));
        his.setFromSts(fromSts);
        his.setToSts(flow.getToSts());
        his.setAction(action);
        his.setActionNm(flow.getActionNm());
        his.setActorId(actorId);
        his.setRsn(rsn);
        statusMapper.insertHis(his);

        return flow.getToSts();
    }
}
