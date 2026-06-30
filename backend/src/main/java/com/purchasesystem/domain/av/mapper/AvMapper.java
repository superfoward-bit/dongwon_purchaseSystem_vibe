package com.purchasesystem.domain.av.mapper;

import com.purchasesystem.domain.av.AvAdvance;
import com.purchasesystem.domain.av.AvRepay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AvMapper {
    List<AvAdvance> findList(@Param("keyword") String keyword, @Param("sts") String sts, @Param("vdCd") String vdCd);
    AvAdvance findById(@Param("id") Long id);
    void insert(AvAdvance a);
    void update(AvAdvance a);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void updateBalance(@Param("id") Long id, @Param("balance") BigDecimal balance, @Param("sts") String sts, @Param("modId") String modId);
    void delete(@Param("id") Long id, @Param("modId") String modId);

    List<AvRepay> findRepays(@Param("advanceId") Long advanceId);
    void insertRepay(AvRepay r);
    void deleteRepays(@Param("advanceId") Long advanceId);
    BigDecimal sumRepay(@Param("advanceId") Long advanceId);
    int nextRepayLine(@Param("advanceId") Long advanceId);
}
