package com.purchasesystem.domain.pr.mapper;

import com.purchasesystem.domain.pr.PrItem;
import com.purchasesystem.domain.pr.PrRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrMapper {

    List<PrRequest> findList(@Param("keyword") String keyword, @Param("sts") String sts);

    PrRequest findById(@Param("id") Long id);

    List<PrItem> findItems(@Param("requestId") Long requestId);

    int insertRequest(PrRequest pr);

    int updateRequest(PrRequest pr);

    int updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);

    int deleteRequest(@Param("id") Long id, @Param("modId") String modId);

    int insertItem(PrItem item);

    int deleteItems(@Param("requestId") Long requestId);
}
