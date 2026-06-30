package com.purchasesystem.domain.notice.mapper;

import com.purchasesystem.domain.notice.CmNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NoticeMapper {
    List<CmNotice> findList(@Param("notiTyp") String notiTyp, @Param("sendSts") String sendSts,
                            @Param("toUsrId") String toUsrId, @Param("refTyp") String refTyp, @Param("refId") Long refId);
    void insert(CmNotice n);
    void updateResult(@Param("id") Long id, @Param("sendSts") String sendSts, @Param("errMsg") String errMsg);
}
