package com.purchasesystem.domain.stock;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.gr.GrItem;
import com.purchasesystem.domain.gr.GrReceipt;
import com.purchasesystem.domain.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 재고 서비스. 도메인 의존 없음(StockMapper + GR 모델만) → 어디서나 주입 가능.
 * 모든 재고 변동은 move()를 통해 ST_STOCK 갱신 + ST_LEDGER(수불부) 기록.
 */
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;

    public List<StStock> getStockList(String keyword, String whCd, String zeroYn) {
        return stockMapper.findStockList(keyword, whCd, zeroYn);
    }
    public List<StStock> getStockByItem(String whCd, String itemCd) {
        return stockMapper.findStockByItemWh(whCd, itemCd);
    }
    public List<StLedger> getLedger(String keyword, String itemCd, String whCd, String transTyp) {
        return stockMapper.findLedger(keyword, itemCd, whCd, transTyp);
    }

    /** 재고 변동 1건 처리 + 수불부 기록. inQty>0=입고, outQty>0=출고. 반환=변동 후 잔량. */
    @Transactional
    public BigDecimal move(String compCd, String transTyp, String whCd, String itemCd, String itemNm,
                           String lotNo, String mfgYmd, String expYmd, String unitCd,
                           BigDecimal inQty, BigDecimal outQty, String srcTyp, Long srcId, String srcNo,
                           String transYmd, String remark, String actorId) {
        if (whCd == null || whCd.isBlank()) throw new BusinessException("창고가 지정되지 않았습니다.");
        String lot = (lotNo == null || lotNo.isBlank()) ? "*" : lotNo;
        BigDecimal in = nvl(inQty), out = nvl(outQty);
        BigDecimal delta = in.subtract(out);

        StStock row = stockMapper.findStockRow(compCd, whCd, itemCd, lot);
        BigDecimal bal;
        if (row == null) {
            if (delta.signum() < 0) throw new BusinessException("재고가 없어 출고할 수 없습니다. (" + itemCd + " / LOT " + lot + ")");
            StStock s = new StStock();
            s.setCompCd(compCd); s.setWhCd(whCd); s.setItemCd(itemCd); s.setItemNm(itemNm);
            s.setLotNo(lot); s.setMfgYmd(mfgYmd); s.setExpYmd(expYmd); s.setUnitCd(unitCd);
            s.setQty(delta); s.setLastInYmd(transYmd);
            stockMapper.insertStock(s);
            bal = delta;
        } else {
            bal = nvl(row.getQty()).add(delta);
            if (bal.signum() < 0)
                throw new BusinessException(String.format("재고 부족: %s LOT %s 현재고 %s < 출고 %s", itemCd, lot, row.getQty(), out));
            stockMapper.updateStockQty(row.getId(), bal,
                    in.signum() > 0 ? transYmd : null, out.signum() > 0 ? transYmd : null, actorId);
        }

        StLedger l = new StLedger();
        l.setCompCd(compCd); l.setTransTyp(transTyp); l.setWhCd(whCd); l.setItemCd(itemCd); l.setItemNm(itemNm);
        l.setLotNo(lot); l.setInQty(in); l.setOutQty(out); l.setBalQty(bal); l.setUnitCd(unitCd);
        l.setSrcTyp(srcTyp); l.setSrcId(srcId); l.setSrcNo(srcNo); l.setTransYmd(transYmd);
        l.setRemark(remark); l.setRegId(actorId);
        stockMapper.insertLedger(l);
        return bal;
    }

    /** 입고확정(GR) → 검사 합격수량 자동 입고 (LOT/유통기한 승계). */
    @Transactional
    public void receiveFromGr(GrReceipt gr, List<GrItem> items, String actorId) {
        String wh = (gr.getWhCd() == null || gr.getWhCd().isBlank()) ? "WH01" : gr.getWhCd();
        for (GrItem it : items) {
            BigDecimal pass = it.getInspPassQty();
            BigDecimal qty = (pass != null && pass.signum() > 0) ? pass
                    : (it.getGrQty() == null ? BigDecimal.ZERO : it.getGrQty());
            if (qty.signum() <= 0) continue;
            move(gr.getCompCd(), "IN", wh, it.getItemCd(), it.getItemNm(), it.getLotNo(),
                 it.getMfgYmd(), it.getExpYmd(), it.getUnitCd(), qty, BigDecimal.ZERO,
                 "GR", gr.getId(), gr.getGrNo(), gr.getGrYmd(), "입고확정 자동입고", actorId);
        }
    }

    private BigDecimal nvl(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
