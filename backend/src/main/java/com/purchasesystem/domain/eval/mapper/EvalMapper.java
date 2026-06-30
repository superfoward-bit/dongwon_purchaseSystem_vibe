package com.purchasesystem.domain.eval.mapper;

import com.purchasesystem.domain.eval.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface EvalMapper {

    // ----- 평가시트 -----
    List<VdEvalSheet> findSheets();
    VdEvalSheet findSheetByCd(@Param("sheetCd") String sheetCd);
    List<VdEvalSheetItem> findSheetItems(@Param("sheetCd") String sheetCd);
    int countSheet(@Param("sheetCd") String sheetCd);
    int insertSheet(VdEvalSheet s);
    int updateSheet(VdEvalSheet s);
    int deleteSheet(@Param("id") Long id, @Param("modId") String modId);
    int deleteSheetItems(@Param("sheetCd") String sheetCd);
    int insertSheetItem(VdEvalSheetItem it);
    // 카테고리(2단계 가중)
    List<VdEvalCate> findSheetCates(@Param("sheetCd") String sheetCd);
    int insertSheetCate(VdEvalCate c);
    int deleteSheetCates(@Param("sheetCd") String sheetCd);

    // ----- 등급 -----
    List<VdEvalGrade> findGrades();
    /** 세그먼트 우선, 없으면 공통 등급행렬로 점수→등급 */
    VdEvalGrade findGradeBySegScore(@Param("segCd") String segCd, @Param("score") BigDecimal score);

    // 협력사 세그먼트 조회
    String findVendorSeg(@Param("vdCd") String vdCd);
    String findVendorSegNm(@Param("vdCd") String vdCd);

    // ----- 평가 실행 -----
    List<VdEval> findEvalList(@Param("keyword") String keyword, @Param("sts") String sts);
    VdEval findEvalById(@Param("id") Long id);
    List<VdEvalResult> findResults(@Param("evalId") Long evalId);
    int insertEval(VdEval e);
    int updateEvalScore(@Param("id") Long id, @Param("totScore") BigDecimal totScore,
                        @Param("gradeCd") String gradeCd, @Param("gradeNm") String gradeNm);
    int updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    int deleteEval(@Param("id") Long id, @Param("modId") String modId);
    int insertResult(VdEvalResult r);
    int updateResult(@Param("id") Long id, @Param("score") BigDecimal score,
                     @Param("weightedScore") BigDecimal weightedScore, @Param("opinion") String opinion);
    int deleteResults(@Param("evalId") Long evalId);

    // 협력사 등급 갱신
    int updateVendorGrade(@Param("vdCd") String vdCd, @Param("gradeCd") String gradeCd, @Param("gradeNm") String gradeNm);
}
