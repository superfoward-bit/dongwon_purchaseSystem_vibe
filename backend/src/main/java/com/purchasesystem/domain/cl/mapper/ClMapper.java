package com.purchasesystem.domain.cl.mapper;

import com.purchasesystem.domain.cl.ClClose;
import com.purchasesystem.domain.cl.ClCloseGr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClMapper {

    List<ClClose> findList(@Param("keyword") String keyword, @Param("sts") String sts,
                           @Param("closeYm") String closeYm);
    ClClose findById(@Param("id") Long id);
    List<ClCloseGr> findDetailGrs(@Param("closeId") Long closeId);

    /** 마감대상(입고확정 & 미마감) 입고 조회 */
    List<ClCloseGr> findEligibleGrs(@Param("vdCd") String vdCd, @Param("closeYm") String closeYm);

    int insertClose(ClClose c);
    int insertCloseGr(ClCloseGr g);
    int updateStatus(@Param("id") Long id, @Param("sts") String sts,
                     @Param("lockYn") String lockYn, @Param("modId") String modId);
    int deleteClose(@Param("id") Long id, @Param("modId") String modId);
    int deleteCloseGrs(@Param("closeId") Long closeId);
}
