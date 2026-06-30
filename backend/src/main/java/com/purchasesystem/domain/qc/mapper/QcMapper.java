package com.purchasesystem.domain.qc.mapper;

import com.purchasesystem.domain.qc.QcAction;
import com.purchasesystem.domain.qc.QcNonconf;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QcMapper {
    List<QcNonconf> findList(@Param("keyword") String keyword, @Param("sts") String sts, @Param("vdCd") String vdCd);
    QcNonconf findById(@Param("id") Long id);
    void insert(QcNonconf n);
    void update(QcNonconf n);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void delete(@Param("id") Long id, @Param("modId") String modId);

    List<QcAction> findActions(@Param("ncId") Long ncId);
    void insertAction(QcAction a);
    void deleteActions(@Param("ncId") Long ncId);
}
