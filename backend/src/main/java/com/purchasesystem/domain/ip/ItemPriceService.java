package com.purchasesystem.domain.ip;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.approval.ApprovalService;
import com.purchasesystem.domain.ip.mapper.ItemPriceMapper;
import com.purchasesystem.domain.ip.mapper.PriceRsvMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ItemPriceService {

    private static final String DOC_TYP = "IP";

    private final PriceRsvMapper rsvMapper;
    private final ItemPriceMapper priceMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ApprovalService approvalService;

    // ===== 확정단가 구간 조회 =====
    public List<IpPrice> getPriceList(String vdCd, String itemCd, String keyword) {
        return priceMapper.findList(vdCd, itemCd, keyword);
    }

    public List<IpPriceHis> getHistory(String vdCd, String itemCd) {
        return priceMapper.findHis(vdCd, itemCd);
    }

    /** 유효일자 기준 단가 1건 (단가 자동적용) */
    public IpPrice findEffectivePrice(String compCd, String vdCd, String itemCd, String ymd) {
        return priceMapper.findEffectivePrice(compCd, vdCd, itemCd, ymd);
    }

    // ===== 변경예약 =====
    public List<IpRsv> getRsvList(String keyword, String sts) { return rsvMapper.findList(keyword, sts); }

    public IpRsv getRsvDetail(Long id) {
        IpRsv r = rsvMapper.findById(id);
        if (r == null) throw new BusinessException("변경예약을 찾을 수 없습니다.");
        r.setLines(rsvMapper.findLines(id));
        r.setActions(statusService.availableActions(DOC_TYP, r.getSts()).stream()
                .filter(f -> !List.of("SUBMIT", "APPROVE", "REJECT").contains(f.getAction())).toList());
        r.setHistory(statusService.history(DOC_TYP, id));
        r.setApproval(approvalService.getByDoc(DOC_TYP, id));
        return r;
    }

    @Transactional
    public Long createRsv(IpRsv r, LoginUser user) {
        if (r.getLines() == null || r.getLines().isEmpty()) throw new BusinessException("변경할 단가 명세를 입력하세요.");
        validateLines(r);
        r.setCompCd(user.compCd());
        r.setChrgUsrId(user.usrId());
        r.setSts("TMP");
        r.setRegId(user.usrId());
        r.setRsvNo(docNoService.generate(DOC_TYP));
        rsvMapper.insertRsv(r);
        saveLines(r);
        return r.getId();
    }

    @Transactional
    public void updateRsv(Long id, IpRsv r, LoginUser user) {
        IpRsv cur = rsvMapper.findById(id);
        if (cur == null) throw new BusinessException("변경예약을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        validateLines(r);
        r.setId(id);
        r.setModId(user.usrId());
        rsvMapper.updateRsv(r);
        rsvMapper.deleteLines(id);
        saveLines(r);
    }

    @Transactional
    public void submit(Long id, List<Map<String, String>> approvers, LoginUser user) {
        IpRsv cur = rsvMapper.findById(id);
        if (cur == null) throw new BusinessException("변경예약을 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 상신할 수 있습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getRsvNo(), cur.getSts(), "SUBMIT", null, user.usrId());
        rsvMapper.updateStatus(id, toSts, user.usrId());
        approvalService.create(DOC_TYP, id, cur.getRsvNo(), cur.getRsvTitle(), approvers, user);
    }

    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        IpRsv cur = rsvMapper.findById(id);
        if (cur == null) throw new BusinessException("변경예약을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getRsvNo(), cur.getSts(), actionCode, reason, user.usrId());
        rsvMapper.updateStatus(id, toSts, user.usrId());
        if ("RECALL".equals(actionCode)) approvalService.cancelByDoc(DOC_TYP, id);
        return toSts;
    }

    @Transactional
    public void deleteRsv(Long id, LoginUser user) {
        IpRsv cur = rsvMapper.findById(id);
        if (cur == null) return;
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 삭제할 수 있습니다.");
        rsvMapper.deleteLines(id);
        rsvMapper.deleteRsv(id, user.usrId());
    }

    // ===== 내부 =====
    private void validateLines(IpRsv r) {
        if (r.getLines() != null) {
            for (IpRsvLine l : r.getLines()) {
                if (l.getItemCd() == null || l.getItemCd().isBlank()) throw new BusinessException("품목을 선택하세요.");
                if (l.getApplySd() == null || l.getApplyEd() == null) throw new BusinessException("적용기간을 입력하세요.");
                if (l.getApplySd().compareTo(l.getApplyEd()) > 0) throw new BusinessException("적용 시작일이 종료일보다 늦습니다: " + l.getItemCd());
            }
        }
    }

    private void saveLines(IpRsv r) {
        int line = 1;
        for (IpRsvLine l : r.getLines()) {
            l.setRsvId(r.getId());
            l.setLineNo(line++);
            rsvMapper.insertLine(l);
        }
    }
}
