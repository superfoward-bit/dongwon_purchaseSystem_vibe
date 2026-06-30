package com.purchasesystem.domain.budget;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.budget.mapper.BudgetMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetMapper budgetMapper;

    public List<BgBudget> getList(String keyword, String fiscalYear) {
        List<BgBudget> list = budgetMapper.findList(keyword, fiscalYear);
        for (BgBudget b : list) b.setAvailableAmt(nvl(b.getBudgetAmt()).subtract(nvl(b.getUsedAmt())));
        return list;
    }

    public BgBudget getDetail(Long id) {
        BgBudget b = budgetMapper.findById(id);
        if (b == null) throw new BusinessException("예산을 찾을 수 없습니다.");
        b.setAvailableAmt(nvl(b.getBudgetAmt()).subtract(nvl(b.getUsedAmt())));
        return b;
    }

    @Transactional
    public Long create(BgBudget b, LoginUser user) {
        b.setCompCd(user.compCd());
        b.setRegId(user.usrId());
        budgetMapper.insert(b);
        return b.getId();
    }

    @Transactional
    public void update(Long id, BgBudget b, LoginUser user) {
        b.setId(id);
        b.setModId(user.usrId());
        budgetMapper.update(b);
    }

    @Transactional
    public void delete(Long id, LoginUser user) { budgetMapper.delete(id, user.usrId()); }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
