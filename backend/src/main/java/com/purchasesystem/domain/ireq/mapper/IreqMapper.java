package com.purchasesystem.domain.ireq.mapper;

import com.purchasesystem.domain.ireq.ItItemReq;
import com.purchasesystem.domain.item.ItCateAttr;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IreqMapper {
    List<ItItemReq> findList(@Param("keyword") String keyword, @Param("sts") String sts);
    ItItemReq findById(@Param("id") Long id);
    void insert(ItItemReq r);
    void update(ItItemReq r);
    void updateStatus(@Param("id") Long id, @Param("sts") String sts,
                      @Param("createdItemCd") String createdItemCd, @Param("modId") String modId);
    void delete(@Param("id") Long id, @Param("modId") String modId);

    List<ItCateAttr> findReqAttrs(@Param("reqId") Long reqId);
    void insertReqAttr(@Param("reqId") Long reqId, @Param("attrNm") String attrNm, @Param("attrVal") String attrVal);
    void deleteReqAttrs(@Param("reqId") Long reqId);
}
