package com.purchasesystem.domain.approval.mapper;

import com.purchasesystem.domain.approval.AppApproval;
import com.purchasesystem.domain.approval.AppLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApprovalMapper {

    int insertApproval(AppApproval a);
    int insertLine(AppLine l);
    AppApproval findById(@Param("id") Long id);
    AppApproval findActiveByDoc(@Param("docTyp") String docTyp, @Param("docId") Long docId);
    List<AppLine> findLines(@Param("aprvId") Long aprvId);

    int updateApprovalStatus(@Param("id") Long id, @Param("aprvSts") String aprvSts,
                             @Param("curStepNo") Integer curStepNo);
    int updateLineStatus(@Param("id") Long id, @Param("lineSts") String lineSts, @Param("opinion") String opinion);
    int updateLineStatusAs(@Param("id") Long id, @Param("lineSts") String lineSts,
                           @Param("opinion") String opinion, @Param("actAsUsrId") String actAsUsrId);
    int setLineIng(@Param("aprvId") Long aprvId, @Param("stepNo") Integer stepNo);
    /** 전결: 지정 단계 이후의 미처리(WAIT/ING) 결재/합의 단계를 전결생략(SKIP) 처리 */
    int skipRemaining(@Param("aprvId") Long aprvId, @Param("afterStepNo") Integer afterStepNo);

    /** 내 결재함 (위임받은 건 포함) */
    List<AppLine> findInbox(@Param("usrId") String usrId, @Param("done") boolean done,
                            @Param("delegators") List<String> delegators);
    /** 내 상신함 */
    List<AppApproval> findDrafts(@Param("usrId") String usrId);
    AppLine findLine(@Param("id") Long id);
}
