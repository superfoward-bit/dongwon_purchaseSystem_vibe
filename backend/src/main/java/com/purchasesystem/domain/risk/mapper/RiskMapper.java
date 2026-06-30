package com.purchasesystem.domain.risk.mapper;

import com.purchasesystem.domain.risk.VdAudit;
import com.purchasesystem.domain.risk.VdAuditResult;
import com.purchasesystem.domain.risk.VdRegal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RiskMapper {

    // ----- 감시 -----
    List<VdAudit> findAuditList(@Param("keyword") String keyword, @Param("sts") String sts);
    VdAudit findAuditById(@Param("id") Long id);
    List<VdAuditResult> findAuditResults(@Param("auditId") Long auditId);
    int insertAudit(VdAudit a);
    int updateAuditSummary(@Param("id") Long id, @Param("resultGrade") String resultGrade,
                           @Param("findingCnt") int findingCnt, @Param("actionReqYn") String actionReqYn);
    int updateAuditStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    /** 감시 결과 리스크등급을 협력사 마스터로 환류 */
    int updateVendorRisk(@Param("vdCd") String vdCd, @Param("riskGrade") String riskGrade);
    int deleteAudit(@Param("id") Long id, @Param("modId") String modId);
    int insertAuditResult(VdAuditResult r);
    int deleteAuditResults(@Param("auditId") Long auditId);

    // ----- 규제 -----
    List<VdRegal> findRegalList(@Param("keyword") String keyword, @Param("sts") String sts);
    VdRegal findRegalById(@Param("id") Long id);
    int countRegal(@Param("regalNo") String regalNo);
    int insertRegal(VdRegal r);
    int updateRegal(VdRegal r);
    int deleteRegal(@Param("id") Long id, @Param("modId") String modId);
}
