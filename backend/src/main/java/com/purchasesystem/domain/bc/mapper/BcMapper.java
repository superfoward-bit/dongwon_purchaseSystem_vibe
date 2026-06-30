package com.purchasesystem.domain.bc.mapper;

import com.purchasesystem.domain.bc.BcContract;
import com.purchasesystem.domain.bc.BcTerm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BcMapper {
    List<BcContract> findList(@Param("keyword") String keyword, @Param("sts") String sts, @Param("vdCd") String vdCd);
    /** 만료예정(ACTIVE, 종료일 N일 이내) */
    List<BcContract> findExpiring(@Param("days") int days);
    BcContract findById(@Param("id") Long id);
    List<BcTerm> findTerms(@Param("contractId") Long contractId);

    void insertContract(BcContract c);
    void updateContract(BcContract c);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void deleteContract(@Param("id") Long id, @Param("modId") String modId);

    void insertTerm(BcTerm t);
    void deleteTerms(@Param("contractId") Long contractId);
}
