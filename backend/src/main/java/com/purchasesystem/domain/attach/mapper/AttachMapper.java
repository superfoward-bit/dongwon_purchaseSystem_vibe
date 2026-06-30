package com.purchasesystem.domain.attach.mapper;

import com.purchasesystem.domain.attach.CmAttach;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AttachMapper {
    List<CmAttach> findByRef(@Param("refTyp") String refTyp, @Param("refId") Long refId);
    CmAttach findById(@Param("id") Long id);
    void insert(CmAttach a);
    void softDelete(@Param("id") Long id, @Param("regId") String regId);
}
