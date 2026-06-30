package com.purchasesystem.domain.iface.mapper;

import com.purchasesystem.domain.iface.IfInterface;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IfMapper {
    List<IfInterface> findList(@Param("ifTyp") String ifTyp, @Param("ifStatus") String ifStatus, @Param("keyword") String keyword);
    IfInterface findById(@Param("id") Long id);
    void insert(IfInterface i);
    void updateResult(@Param("id") Long id, @Param("ifStatus") String ifStatus, @Param("resultMsg") String resultMsg, @Param("retry") boolean retry);
}
