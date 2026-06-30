package com.purchasesystem.domain.stock;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.stock.mapper.IssueMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueMapper issueMapper;
    private final StockService stockService;
    private final DocNoService docNoService;

    public List<StIssue> getList(String keyword, String sts) { return issueMapper.findList(keyword, sts); }

    public StIssue getDetail(Long id) {
        StIssue i = issueMapper.findById(id);
        if (i == null) throw new BusinessException("출고를 찾을 수 없습니다.");
        i.setItems(issueMapper.findItems(id));
        return i;
    }

    @Transactional
    public Long create(StIssue i, LoginUser user) {
        i.setCompCd(user.compCd());
        i.setRegId(user.usrId());
        i.setIssueNo(docNoService.generate("IS"));
        issueMapper.insert(i);
        saveItems(i);
        return i.getId();
    }

    @Transactional
    public void update(Long id, StIssue i, LoginUser user) {
        StIssue cur = issueMapper.findById(id);
        if (cur == null) throw new BusinessException("출고를 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        i.setId(id);
        i.setModId(user.usrId());
        issueMapper.updateHeader(i);
        issueMapper.deleteItems(id);
        saveItems(i);
    }

    /** 출고확정 → 재고 차감(OUT) + 수불부 기록 */
    @Transactional
    public void confirm(Long id, LoginUser user) {
        StIssue i = getDetail(id);
        if (!"TMP".equals(i.getSts())) throw new BusinessException("작성중 상태에서만 확정할 수 있습니다.");
        if (i.getItems().isEmpty()) throw new BusinessException("출고 품목이 없습니다.");
        for (StIssueItem it : i.getItems()) {
            BigDecimal qty = it.getQty() == null ? BigDecimal.ZERO : it.getQty();
            if (qty.signum() <= 0) continue;
            stockService.move(i.getCompCd(), "OUT", i.getWhCd(), it.getItemCd(), it.getItemNm(), it.getLotNo(),
                    null, null, it.getUnitCd(), BigDecimal.ZERO, qty, "IS", i.getId(), i.getIssueNo(),
                    i.getIssueYmd(), i.getIssueTypNm() + " 출고", user.usrId());
        }
        issueMapper.updateStatus(id, "CFM", user.usrId());
    }

    @Transactional
    public void cancel(Long id, LoginUser user) {
        StIssue cur = issueMapper.findById(id);
        if (cur == null) return;
        if ("CFM".equals(cur.getSts())) throw new BusinessException("확정된 출고는 취소할 수 없습니다. (재고조정으로 처리하세요)");
        issueMapper.updateStatus(id, "CANCEL", user.usrId());
    }

    private void saveItems(StIssue i) {
        int line = 1;
        if (i.getItems() != null) for (StIssueItem it : i.getItems()) {
            it.setIssueId(i.getId());
            it.setLineNo(line++);
            issueMapper.insertItem(it);
        }
    }
}
