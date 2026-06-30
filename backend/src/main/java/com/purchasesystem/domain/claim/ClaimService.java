package com.purchasesystem.domain.claim;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.claim.mapper.ClaimMapper;
import com.purchasesystem.domain.notice.NotificationService;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final String DOC_TYP = "CLM";

    private final ClaimMapper claimMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final NotificationService notificationService;

    public List<CmClaim> getList(String keyword, String sts, String vdCd) { return claimMapper.findList(keyword, sts, vdCd); }

    public CmClaim getDetail(Long id) {
        CmClaim c = claimMapper.findById(id);
        if (c == null) throw new BusinessException("클레임을 찾을 수 없습니다.");
        c.setActions(statusService.availableActions(DOC_TYP, c.getSts()));
        c.setHistory(statusService.history(DOC_TYP, id));
        return c;
    }

    @Transactional
    public Long create(CmClaim c, LoginUser user) {
        if (c.getVdCd() == null) throw new BusinessException("협력사를 선택하세요.");
        c.setCompCd(user.compCd());
        c.setSts("OPEN");
        c.setRegId(user.usrId());
        c.setClmNo(docNoService.generate(DOC_TYP));
        claimMapper.insert(c);
        return c.getId();
    }

    @Transactional
    public void update(Long id, CmClaim c, LoginUser user) {
        CmClaim cur = claimMapper.findById(id);
        if (cur == null) throw new BusinessException("클레임을 찾을 수 없습니다.");
        if ("CLOSED".equals(cur.getSts()) || "CANCEL".equals(cur.getSts()))
            throw new BusinessException("종결/취소된 클레임은 수정할 수 없습니다.");
        c.setId(id);
        c.setModId(user.usrId());
        claimMapper.update(c);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        CmClaim cur = claimMapper.findById(id);
        if (cur == null) throw new BusinessException("클레임을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getClmNo(), cur.getSts(), actionCode, reason, user.usrId());
        claimMapper.updateStatus(id, toSts, user.usrId());
        if ("START".equals(actionCode)) {
            try {
                notificationService.notify("EMAIL", null, cur.getVdCd(),
                        "[클레임 처리요청] " + cur.getClmNo(),
                        "클레임(" + cur.getClmTyp() + ")이 접수되었습니다. 처리 부탁드립니다. 품목: " + cur.getItemNm(),
                        DOC_TYP, id, "CLM_START", cur.getCompCd(), user.usrId());
            } catch (Exception ignore) { }
        }
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        CmClaim cur = claimMapper.findById(id);
        if (cur == null) return;
        if (!"OPEN".equals(cur.getSts())) throw new BusinessException("접수 상태에서만 삭제할 수 있습니다.");
        claimMapper.delete(id, user.usrId());
    }
}
