package com.purchasesystem.domain.ireq;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.common.status.StatusTransitionService;
import com.purchasesystem.domain.ireq.mapper.IreqMapper;
import com.purchasesystem.domain.item.ItCateAttr;
import com.purchasesystem.domain.item.ItItem;
import com.purchasesystem.domain.item.ItemService;
import com.purchasesystem.domain.item.mapper.AttrMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IreqService {

    private static final String DOC_TYP = "IR";

    private final IreqMapper ireqMapper;
    private final DocNoService docNoService;
    private final StatusTransitionService statusService;
    private final ItemService itemService;
    private final AttrMapper attrMapper;

    public List<ItItemReq> getList(String keyword, String sts) { return ireqMapper.findList(keyword, sts); }

    public ItItemReq getDetail(Long id) {
        ItItemReq r = ireqMapper.findById(id);
        if (r == null) throw new BusinessException("등록요청을 찾을 수 없습니다.");
        r.setAttrList(ireqMapper.findReqAttrs(id));
        r.setActions(statusService.availableActions(DOC_TYP, r.getSts()));
        r.setHistory(statusService.history(DOC_TYP, id));
        return r;
    }

    @Transactional
    public Long create(ItItemReq r, LoginUser user) {
        if (r.getItemNm() == null || r.getItemNm().isBlank()) throw new BusinessException("품목명을 입력하세요.");
        r.setCompCd(user.compCd());
        r.setSts("REQ");
        r.setRegId(user.usrId());
        r.setReqNo(docNoService.generate(DOC_TYP));
        ireqMapper.insert(r);
        saveAttrs(r);
        return r.getId();
    }

    @Transactional
    public void update(Long id, ItItemReq r, LoginUser user) {
        ItItemReq cur = ireqMapper.findById(id);
        if (cur == null) throw new BusinessException("등록요청을 찾을 수 없습니다.");
        if (!"REQ".equals(cur.getSts())) throw new BusinessException("요청 상태에서만 수정할 수 있습니다.");
        r.setId(id);
        r.setModId(user.usrId());
        ireqMapper.update(r);
        ireqMapper.deleteReqAttrs(id);
        saveAttrs(r);
    }

    /** 상태전이: 승인(품목 생성)/반려/취소 */
    @Transactional
    public String action(Long id, String actionCode, String reason, LoginUser user) {
        ItItemReq cur = ireqMapper.findById(id);
        if (cur == null) throw new BusinessException("등록요청을 찾을 수 없습니다.");
        String toSts = statusService.transition(DOC_TYP, id, cur.getReqNo(), cur.getSts(), actionCode, reason, user.usrId());

        String createdItemCd = null;
        if ("APPROVE".equals(actionCode)) {
            // 품목 마스터 생성
            ItItem it = new ItItem();
            it.setItemNm(cur.getItemNm());
            it.setItemNmEn(cur.getItemNmEn());
            it.setSpec(cur.getSpec());
            it.setCateCd(cur.getCateCd());
            it.setUnitCd(cur.getUnitCd());
            it.setItemTyp(cur.getItemTyp());
            it.setTaxTyp(cur.getTaxTyp());
            it.setItemSts("ACTIVE");
            it.setRemark("품목등록요청 " + cur.getReqNo() + " 승인 생성");
            Long itemId = itemService.create(it, user);
            createdItemCd = it.getItemCd();
            // 속성값 복사
            for (ItCateAttr a : ireqMapper.findReqAttrs(id)) {
                attrMapper.insertItemAttr(itemId, a.getAttrNm(), a.getAttrVal());
            }
        }
        ireqMapper.updateStatus(id, toSts, createdItemCd, user.usrId());
        return toSts;
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        ItItemReq cur = ireqMapper.findById(id);
        if (cur == null) return;
        if (!"REQ".equals(cur.getSts())) throw new BusinessException("요청 상태에서만 삭제할 수 있습니다.");
        ireqMapper.deleteReqAttrs(id);
        ireqMapper.delete(id, user.usrId());
    }

    private void saveAttrs(ItItemReq r) {
        if (r.getAttrList() == null) return;
        for (ItCateAttr a : r.getAttrList()) {
            if (a.getAttrNm() == null || a.getAttrNm().isBlank()) continue;
            ireqMapper.insertReqAttr(r.getId(), a.getAttrNm(), a.getAttrVal());
        }
    }
}
