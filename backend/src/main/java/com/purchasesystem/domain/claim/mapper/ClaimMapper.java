package com.purchasesystem.domain.claim.mapper;

import com.purchasesystem.domain.claim.CmClaim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClaimMapper {
    List<CmClaim> findList(@Param("keyword") String keyword, @Param("sts") String sts, @Param("vdCd") String vdCd);
    CmClaim findById(@Param("id") Long id);
    void insert(CmClaim c);
    void update(CmClaim c);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void delete(@Param("id") Long id, @Param("modId") String modId);
}
