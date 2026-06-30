package com.purchasesystem.common.status.mapper;

import com.purchasesystem.common.status.CmStatusFlow;
import com.purchasesystem.common.status.DocStatusHis;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StatusMapper {

    /** 현재 상태에서 수행 가능한 액션 목록 */
    List<CmStatusFlow> findActions(@Param("docTyp") String docTyp, @Param("fromSts") String fromSts);

    /** 특정 전이 규칙 1건 */
    CmStatusFlow findFlow(@Param("docTyp") String docTyp, @Param("fromSts") String fromSts,
                          @Param("action") String action);

    /** 상태이력 다음 순번 */
    int nextSeq(@Param("docTyp") String docTyp, @Param("docId") Long docId);

    int insertHis(DocStatusHis his);

    List<DocStatusHis> findHis(@Param("docTyp") String docTyp, @Param("docId") Long docId);
}
