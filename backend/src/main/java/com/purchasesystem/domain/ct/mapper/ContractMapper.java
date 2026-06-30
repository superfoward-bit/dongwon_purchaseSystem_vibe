package com.purchasesystem.domain.ct.mapper;

import com.purchasesystem.domain.ct.CtContract;
import com.purchasesystem.domain.ct.CtItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractMapper {

    List<CtContract> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    CtContract findById(@Param("id") Long id);
    List<CtItem> findItems(@Param("contractId") Long contractId);

    int insertContract(CtContract c);
    int updateContract(CtContract c);
    int updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    int deleteContract(@Param("id") Long id, @Param("modId") String modId);
    int insertItem(CtItem it);
    int deleteItems(@Param("contractId") Long contractId);

    /** 유효 계약단가 조회 (계약중 + 일자 유효) — 단가 자동적용용 */
    CtItem findActivePrice(@Param("vdCd") String vdCd, @Param("itemCd") String itemCd, @Param("ymd") String ymd);
}
