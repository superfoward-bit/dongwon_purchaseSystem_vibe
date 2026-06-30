package com.purchasesystem.domain.quality.mapper;

import com.purchasesystem.domain.quality.QlInspection;
import com.purchasesystem.domain.quality.QlResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QltMapper {

    List<QlInspection> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    QlInspection findById(@Param("id") Long id);
    List<QlResult> findResults(@Param("inspId") Long inspId);

    int insertInspection(QlInspection q);
    int updateResultSummary(@Param("id") Long id, @Param("inspResult") String inspResult);
    int updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    int deleteInspection(@Param("id") Long id, @Param("modId") String modId);
    int insertResult(QlResult r);
    int deleteResults(@Param("inspId") Long inspId);

    // GR 연계
    List<QlInspection> findInspectableGrs(@Param("keyword") String keyword);
    QlInspection findGrHeader(@Param("grId") Long grId);
}
