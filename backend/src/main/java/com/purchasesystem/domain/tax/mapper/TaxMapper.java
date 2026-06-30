package com.purchasesystem.domain.tax.mapper;

import com.purchasesystem.domain.tax.TxTaxbill;
import com.purchasesystem.domain.tax.TxTaxbillItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaxMapper {
    List<TxTaxbill> findList(@Param("keyword") String keyword, @Param("billSts") String billSts);
    TxTaxbill findById(@Param("id") Long id);
    TxTaxbill findSupplier(@Param("vdCd") String vdCd);
    TxTaxbill findBuyer(@Param("compCd") String compCd);
    void insert(TxTaxbill t);
    void updateStatus(@Param("id") Long id, @Param("billSts") String billSts, @Param("ntsStatus") String ntsStatus, @Param("modId") String modId);
    void updateIssued(@Param("id") Long id, @Param("ntsStatus") String ntsStatus, @Param("ntsApprovalNo") String ntsApprovalNo, @Param("issueYmd") String issueYmd, @Param("modId") String modId);
    void delete(@Param("id") Long id, @Param("modId") String modId);
    int countByClose(@Param("closeId") Long closeId);
    // 품목명세
    List<TxTaxbillItem> findItems(@Param("billId") Long billId);
    List<TxTaxbillItem> findCloseItemLines(@Param("closeId") Long closeId);
    void insertItem(TxTaxbillItem it);
}
