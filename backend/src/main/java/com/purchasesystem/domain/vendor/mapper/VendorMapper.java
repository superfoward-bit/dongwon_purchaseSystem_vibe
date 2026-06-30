package com.purchasesystem.domain.vendor.mapper;

import com.purchasesystem.domain.vendor.VdContact;
import com.purchasesystem.domain.vendor.VdLicense;
import com.purchasesystem.domain.vendor.VdStopHis;
import com.purchasesystem.domain.vendor.VdVendor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VendorMapper {

    List<VdVendor> findList(@Param("keyword") String keyword, @Param("vdSts") String vdSts);
    /** 등록심사 대상 목록 (regSts 필터) */
    List<VdVendor> findRegList(@Param("keyword") String keyword, @Param("regSts") String regSts);

    VdVendor findById(@Param("id") Long id);

    /** 결제조건 스냅샷용 경량 조회 (코드로) */
    VdVendor findPayInfo(@Param("vdCd") String vdCd);

    int insert(VdVendor v);
    int update(VdVendor v);
    int updateRegStatus(@Param("id") Long id, @Param("regSts") String regSts,
                        @Param("vdSts") String vdSts, @Param("modId") String modId);
    int deleteVendor(@Param("id") Long id, @Param("modId") String modId);

    // 거래중지/재개
    int updateTradeStatus(@Param("id") Long id, @Param("vdSts") String vdSts,
                          @Param("stopYmd") String stopYmd, @Param("reason") String reason, @Param("modId") String modId);
    List<VdStopHis> findStopHis(@Param("vendorId") Long vendorId);
    void insertStopHis(VdStopHis h);

    // 담당자
    List<VdContact> findContacts(@Param("vendorId") Long vendorId);
    void insertContact(VdContact c);
    void deleteContacts(@Param("vendorId") Long vendorId);

    // 면허/인증
    List<VdLicense> findLicenses(@Param("vendorId") Long vendorId);
    void insertLicense(VdLicense l);
    void deleteLicenses(@Param("vendorId") Long vendorId);
}
