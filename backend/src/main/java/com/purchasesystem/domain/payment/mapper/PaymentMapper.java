package com.purchasesystem.domain.payment.mapper;

import com.purchasesystem.domain.payment.PyPayment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaymentMapper {
    List<PyPayment> findList(@Param("keyword") String keyword, @Param("paySts") String paySts);
    PyPayment findById(@Param("id") Long id);
    void insert(PyPayment p);
    void updatePaid(@Param("id") Long id, @Param("payYmd") String payYmd, @Param("noteNo") String noteNo,
                    @Param("noteDueYmd") String noteDueYmd, @Param("modId") String modId);
    void updateStatus(@Param("id") Long id, @Param("paySts") String paySts, @Param("modId") String modId);
    int countBySrc(@Param("srcTyp") String srcTyp, @Param("srcId") Long srcId);
}
