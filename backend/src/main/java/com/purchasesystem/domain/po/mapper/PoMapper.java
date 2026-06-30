package com.purchasesystem.domain.po.mapper;

import com.purchasesystem.domain.po.PoDlvSchedule;
import com.purchasesystem.domain.po.PoItem;
import com.purchasesystem.domain.po.PoOrder;
import com.purchasesystem.domain.po.PoPayPlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PoMapper {

    List<PoOrder> findList(@Param("keyword") String keyword, @Param("sts") String sts);

    PoOrder findById(@Param("id") Long id);

    List<PoItem> findItems(@Param("orderId") Long orderId);

    int insertOrder(PoOrder po);

    int updateOrder(PoOrder po);

    int updateStatus(@Param("id") Long id, @Param("sts") String sts, @Param("modId") String modId);

    int deleteOrder(@Param("id") Long id, @Param("modId") String modId);

    int insertItem(PoItem item);

    int deleteItems(@Param("orderId") Long orderId);

    // 분할결제 계획
    List<PoPayPlan> findPayPlans(@Param("orderId") Long orderId);
    int insertPayPlan(PoPayPlan p);
    int deletePayPlans(@Param("orderId") Long orderId);

    // 분할납품 일정
    List<PoDlvSchedule> findDlvSchedules(@Param("orderId") Long orderId);
    int insertDlvSchedule(PoDlvSchedule s);
    int deleteDlvSchedules(@Param("orderId") Long orderId);

    // 여신
    java.math.BigDecimal findCreditLimit(@Param("vdCd") String vdCd);
    java.math.BigDecimal sumOutstanding(@Param("vdCd") String vdCd, @Param("excludeId") Long excludeId);
}
