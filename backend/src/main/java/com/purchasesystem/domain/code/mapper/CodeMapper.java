package com.purchasesystem.domain.code.mapper;

import com.purchasesystem.domain.code.CmCode;
import com.purchasesystem.domain.code.CmCodeGrp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CodeMapper {

    List<CmCodeGrp> findGroups(@Param("keyword") String keyword);

    List<CmCode> findCodes(@Param("grpCd") String grpCd);

    int insertCode(CmCode code);

    int updateCode(CmCode code);

    int deleteCode(@Param("id") Long id, @Param("modId") String modId);

    int countCode(@Param("grpCd") String grpCd, @Param("cd") String cd);
}
