package com.purchasesystem.domain.approval.mapper;

import com.purchasesystem.domain.approval.AppDelegate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DelegateMapper {
    List<AppDelegate> findList(@Param("fromUsrId") String fromUsrId, @Param("toUsrId") String toUsrId);
    AppDelegate findById(@Param("id") Long id);

    /** 특정 일자 기준, 나(toUsrId)에게 위임한 사람들의 ID 목록 */
    List<String> findDelegatorsTo(@Param("toUsrId") String toUsrId, @Param("ymd") String ymd);

    /** from→to 위임이 해당 일자에 유효한지 */
    int countActive(@Param("fromUsrId") String fromUsrId, @Param("toUsrId") String toUsrId, @Param("ymd") String ymd);

    void insert(AppDelegate d);
    void update(AppDelegate d);
    void delete(@Param("id") Long id, @Param("modId") String modId);
}
