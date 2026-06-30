package com.purchasesystem.domain.ip;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.ip.mapper.ItemPriceMapper;
import com.purchasesystem.domain.ip.mapper.PriceRsvMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 단가 변경예약을 확정단가 구간(IP_ITEM_PRICE)에 반영하는 구간분할 엔진.
 * ApprovalService 에 의존하지 않아 결재 핸들러에서 순환참조 없이 호출 가능.
 */
@Service
@RequiredArgsConstructor
public class PriceApplyService {

    private final PriceRsvMapper rsvMapper;
    private final ItemPriceMapper priceMapper;

    /** 변경예약 명세를 확정구간에 반영. 명세별 구간분할 처리. */
    @Transactional
    public void applyReservation(Long rsvId, String actorId) {
        IpRsv r = rsvMapper.findById(rsvId);
        if (r == null) throw new BusinessException("변경예약을 찾을 수 없습니다.");
        List<IpRsvLine> lines = rsvMapper.findLines(rsvId);
        for (IpRsvLine line : lines) applyLine(r, line, actorId);
    }

    private void applyLine(IpRsv r, IpRsvLine line, String actorId) {
        String compCd = r.getCompCd();
        String vdCd = r.getVdCd();
        String itemCd = line.getItemCd();
        String newSd = line.getApplySd();
        String newEd = line.getApplyEd();
        if (newSd == null || newEd == null) throw new BusinessException("적용기간이 누락된 명세가 있습니다: " + itemCd);

        List<IpPrice> overlaps = priceMapper.findOverlapping(compCd, vdCd, itemCd, newSd, newEd);
        for (IpPrice s : overlaps) {
            boolean frontKept = s.getApplySd().compareTo(newSd) < 0;   // 신규 시작 전에 남는 앞 구간
            boolean tailKept = s.getApplyEd().compareTo(newEd) > 0;    // 신규 종료 후 남는 뒤 구간

            if (frontKept && tailKept) {
                // 가운데가 잘림: 앞구간 당김 + 잔여(뒤) 복원
                String origEd = s.getApplyEd();
                priceMapper.updateApplyEd(s.getId(), minusDay(newSd), actorId);
                his(r, line, "PULL", s.getApplySd(), origEd, s.getUnitPrc(), s.getApplySd(), minusDay(newSd), s.getUnitPrc(), actorId);
                insertSeg(r, line, plusDay(newEd), origEd, s.getUnitPrc(), s.getUnitCd(), "RESTORE", s.getId(), actorId);
                his(r, line, "RESTORE", null, null, null, plusDay(newEd), origEd, s.getUnitPrc(), actorId);
            } else if (frontKept) {
                String origEd = s.getApplyEd();
                priceMapper.updateApplyEd(s.getId(), minusDay(newSd), actorId);
                his(r, line, "PULL", s.getApplySd(), origEd, s.getUnitPrc(), s.getApplySd(), minusDay(newSd), s.getUnitPrc(), actorId);
            } else if (tailKept) {
                String origSd = s.getApplySd();
                priceMapper.updateApplySd(s.getId(), plusDay(newEd), actorId);
                his(r, line, "TAIL", origSd, s.getApplyEd(), s.getUnitPrc(), plusDay(newEd), s.getApplyEd(), s.getUnitPrc(), actorId);
            } else {
                priceMapper.softDelete(s.getId(), actorId);
                his(r, line, "DELETE", s.getApplySd(), s.getApplyEd(), s.getUnitPrc(), null, null, null, actorId);
            }
        }
        insertSeg(r, line, newSd, newEd, line.getUnitPrc(), line.getUnitCd(), "RSV", line.getId(), actorId);
        his(r, line, "INSERT", null, null, null, newSd, newEd, line.getUnitPrc(), actorId);
    }

    private void insertSeg(IpRsv r, IpRsvLine line, String sd, String ed, BigDecimal prc,
                           String unitCd, String srcTyp, Long srcId, String actorId) {
        IpPrice p = new IpPrice();
        p.setCompCd(r.getCompCd());
        p.setVdCd(r.getVdCd());
        p.setVdNm(r.getVdNm());
        p.setItemCd(line.getItemCd());
        p.setItemNm(line.getItemNm());
        p.setUnitCd(unitCd);
        p.setApplySd(sd);
        p.setApplyEd(ed);
        p.setUnitPrc(prc);
        p.setSrcTyp(srcTyp);
        p.setSrcId(srcId);
        p.setRegId(actorId);
        priceMapper.insertPrice(p);
    }

    private void his(IpRsv r, IpRsvLine line, String chgTyp, String oldSd, String oldEd, BigDecimal oldPrc,
                     String newSd, String newEd, BigDecimal newPrc, String actorId) {
        IpPriceHis h = new IpPriceHis();
        h.setCompCd(r.getCompCd());
        h.setVdCd(r.getVdCd());
        h.setItemCd(line.getItemCd());
        h.setChgTyp(chgTyp);
        h.setOldSd(oldSd); h.setOldEd(oldEd); h.setOldPrc(oldPrc);
        h.setNewSd(newSd); h.setNewEd(newEd); h.setNewPrc(newPrc);
        h.setSrcTyp("RSV");
        h.setSrcId(line.getId());
        h.setRegId(actorId);
        priceMapper.insertHis(h);
    }

    private String plusDay(String ymd) { return LocalDate.parse(ymd).plusDays(1).toString(); }
    private String minusDay(String ymd) { return LocalDate.parse(ymd).minusDays(1).toString(); }
}
