package com.purchasesystem.common.docno.mapper;

import com.purchasesystem.common.docno.CmDocNo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DocNoMapper {

    /** 채번 규칙 조회 (행 잠금) */
    CmDocNo selectForUpdate(@Param("docTyp") String docTyp);

    int updateSeq(@Param("id") Long id, @Param("lastYmd") String lastYmd, @Param("lastSeq") long lastSeq);
}
