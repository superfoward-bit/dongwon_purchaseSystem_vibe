package com.purchasesystem.domain.ip.mapper;

import com.purchasesystem.domain.ip.IpRsv;
import com.purchasesystem.domain.ip.IpRsvLine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PriceRsvMapper {
    List<IpRsv> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    IpRsv findById(@Param("id") Long id);
    List<IpRsvLine> findLines(@Param("rsvId") Long rsvId);

    void insertRsv(IpRsv r);
    void updateRsv(IpRsv r);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);
    void deleteRsv(@Param("id") Long id, @Param("modId") String modId);

    void insertLine(IpRsvLine l);
    void deleteLines(@Param("rsvId") Long rsvId);
}
