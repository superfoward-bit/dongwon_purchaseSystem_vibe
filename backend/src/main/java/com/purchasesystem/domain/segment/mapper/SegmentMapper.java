package com.purchasesystem.domain.segment.mapper;

import com.purchasesystem.domain.segment.VdSegment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SegmentMapper {
    List<VdSegment> findList();
    VdSegment findByCd(@Param("segCd") String segCd);
    int countByCd(@Param("segCd") String segCd);
    void insert(VdSegment s);
    void update(VdSegment s);
    void delete(@Param("id") Long id, @Param("modId") String modId);
    /** 협력사 세그먼트 배정 */
    void assignVendor(@Param("vendorId") Long vendorId, @Param("segCd") String segCd, @Param("segNm") String segNm, @Param("modId") String modId);
}
