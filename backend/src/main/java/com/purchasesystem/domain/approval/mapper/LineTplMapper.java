package com.purchasesystem.domain.approval.mapper;

import com.purchasesystem.domain.approval.AppLineTpl;
import com.purchasesystem.domain.approval.AppLineTplStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface LineTplMapper {
    List<AppLineTpl> findList(@Param("docTyp") String docTyp);
    AppLineTpl findById(@Param("id") Long id);
    List<AppLineTplStep> findSteps(@Param("tplId") Long tplId);

    /** 문서유형+금액으로 매칭되는 템플릿 1건 */
    AppLineTpl findMatch(@Param("compCd") String compCd, @Param("docTyp") String docTyp, @Param("amount") BigDecimal amount);

    void insertTpl(AppLineTpl t);
    void updateTpl(AppLineTpl t);
    void deleteTpl(@Param("id") Long id, @Param("modId") String modId);
    void insertStep(AppLineTplStep s);
    void deleteSteps(@Param("tplId") Long tplId);
}
