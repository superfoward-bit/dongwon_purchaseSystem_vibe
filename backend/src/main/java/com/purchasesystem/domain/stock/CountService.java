package com.purchasesystem.domain.stock;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.stock.mapper.CountMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CountService {

    private final CountMapper countMapper;
    private final StockService stockService;
    private final DocNoService docNoService;

    public List<StCount> getList(String keyword, String sts) { return countMapper.findList(keyword, sts); }

    public StCount getDetail(Long id) {
        StCount c = countMapper.findById(id);
        if (c == null) throw new BusinessException("재고실사를 찾을 수 없습니다.");
        c.setItems(countMapper.findItems(id));
        return c;
    }

    @Transactional
    public Long create(StCount c, LoginUser user) {
        c.setCompCd(user.compCd());
        c.setRegId(user.usrId());
        c.setCountNo(docNoService.generate("SC"));
        countMapper.insert(c);
        saveItems(c);
        return c.getId();
    }

    @Transactional
    public void update(Long id, StCount c, LoginUser user) {
        StCount cur = countMapper.findById(id);
        if (cur == null) throw new BusinessException("재고실사를 찾을 수 없습니다.");
        if (!"TMP".equals(cur.getSts())) throw new BusinessException("작성중 상태에서만 수정할 수 있습니다.");
        c.setId(id);
        c.setModId(user.usrId());
        countMapper.updateHeader(c);
        countMapper.deleteItems(id);
        saveItems(c);
    }

    /** 실사확정 → 차이(실사-장부)만큼 재고 조정(ADJ) + 수불부 기록 */
    @Transactional
    public void confirm(Long id, LoginUser user) {
        StCount c = getDetail(id);
        if (!"TMP".equals(c.getSts())) throw new BusinessException("작성중 상태에서만 확정할 수 있습니다.");
        for (StCountItem it : c.getItems()) {
            BigDecimal diff = it.getDiffQty() == null ? BigDecimal.ZERO : it.getDiffQty();
            if (diff.signum() == 0) continue;
            BigDecimal in = diff.signum() > 0 ? diff : BigDecimal.ZERO;
            BigDecimal out = diff.signum() < 0 ? diff.negate() : BigDecimal.ZERO;
            stockService.move(c.getCompCd(), "ADJ", c.getWhCd(), it.getItemCd(), it.getItemNm(), it.getLotNo(),
                    null, null, it.getUnitCd(), in, out, "SC", c.getId(), c.getCountNo(),
                    c.getCountYmd(), "재고실사 조정(차이 " + diff + ")", user.usrId());
        }
        countMapper.updateStatus(id, "CFM", user.usrId());
    }

    @Transactional
    public void cancel(Long id, LoginUser user) {
        StCount cur = countMapper.findById(id);
        if (cur == null) return;
        if ("CFM".equals(cur.getSts())) throw new BusinessException("확정된 실사는 취소할 수 없습니다.");
        countMapper.updateStatus(id, "CANCEL", user.usrId());
    }

    private void saveItems(StCount c) {
        int line = 1;
        if (c.getItems() != null) for (StCountItem it : c.getItems()) {
            it.setCountId(c.getId());
            it.setLineNo(line++);
            BigDecimal book = it.getBookQty() == null ? BigDecimal.ZERO : it.getBookQty();
            BigDecimal real = it.getRealQty() == null ? BigDecimal.ZERO : it.getRealQty();
            it.setDiffQty(real.subtract(book));
            countMapper.insertItem(it);
        }
    }
}
