package com.purchasesystem.domain.price;

import com.purchasesystem.domain.ct.CtItem;
import com.purchasesystem.domain.ct.mapper.ContractMapper;
import com.purchasesystem.domain.ip.IpPrice;
import com.purchasesystem.domain.ip.mapper.ItemPriceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 단가 자동적용: 단가계약(ACTIVE) 우선, 없으면 품목 확정단가 구간을 조회.
 */
@Service
@RequiredArgsConstructor
public class PriceLookupService {

    private final ContractMapper contractMapper;
    private final ItemPriceMapper itemPriceMapper;

    public PriceLookup lookup(String compCd, String vdCd, String itemCd, String ymd) {
        PriceLookup r = new PriceLookup();
        r.setVdCd(vdCd);
        r.setItemCd(itemCd);
        r.setYmd(ymd);

        // 1순위: 단가계약(ACTIVE)
        CtItem ct = contractMapper.findActivePrice(vdCd, itemCd, ymd);
        if (ct != null && ct.getCtPrc() != null) {
            r.setUnitPrc(ct.getCtPrc());
            r.setSrcTyp("CT");
            r.setSrcTypNm("계약단가");
            r.setApplySd(ct.getApplySd());
            r.setApplyEd(ct.getApplyEd());
            return r;
        }

        // 2순위: 품목 확정단가 구간
        IpPrice ip = itemPriceMapper.findEffectivePrice(compCd, vdCd, itemCd, ymd);
        if (ip != null && ip.getUnitPrc() != null) {
            r.setUnitPrc(ip.getUnitPrc());
            r.setSrcTyp("IP");
            r.setSrcTypNm("품목단가");
            r.setApplySd(ip.getApplySd());
            r.setApplyEd(ip.getApplyEd());
            return r;
        }

        r.setSrcTyp("NONE");
        r.setSrcTypNm("없음");
        return r;
    }
}
