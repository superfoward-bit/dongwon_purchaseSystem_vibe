package com.purchasesystem.domain.risk;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.risk.mapper.RiskMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegalService {

    private final RiskMapper riskMapper;
    private final DocNoService docNoService;

    public List<VdRegal> getList(String keyword, String sts) { return riskMapper.findRegalList(keyword, sts); }

    public VdRegal getDetail(Long id) {
        VdRegal r = riskMapper.findRegalById(id);
        if (r == null) throw new BusinessException("규제정보를 찾을 수 없습니다.");
        return r;
    }

    @Transactional
    public Long create(VdRegal r, LoginUser user) {
        r.setRegalNo(docNoService.generate("RG"));
        r.setRegId(user.usrId());
        riskMapper.insertRegal(r);
        return r.getId();
    }

    @Transactional
    public void update(Long id, VdRegal r, LoginUser user) {
        r.setId(id);
        r.setModId(user.usrId());
        if (riskMapper.updateRegal(r) == 0) throw new BusinessException("수정 대상이 없습니다.");
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        riskMapper.deleteRegal(id, user.usrId());
    }
}
