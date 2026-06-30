package com.purchasesystem.domain.item;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.item.mapper.ItemMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;
    private final DocNoService docNoService;

    // ----- 분류 -----
    public List<ItCategory> getCategories() {
        return itemMapper.findCategories();
    }

    @Transactional
    public void createCategory(ItCategory c, LoginUser user) {
        if (itemMapper.countByCateCd(c.getCateCd()) > 0) {
            throw new BusinessException("이미 존재하는 분류코드입니다: " + c.getCateCd());
        }
        c.setRegId(user.usrId());
        itemMapper.insertCategory(c);
    }

    @Transactional
    public void updateCategory(Long id, ItCategory c, LoginUser user) {
        c.setId(id);
        c.setModId(user.usrId());
        itemMapper.updateCategory(c);
    }

    @Transactional
    public void deleteCategory(Long id, String cateCd, LoginUser user) {
        if (cateCd != null) {
            if (itemMapper.countChildCate(cateCd) > 0) throw new BusinessException("하위 분류가 있어 삭제할 수 없습니다.");
            if (itemMapper.countItemByCate(cateCd) > 0) throw new BusinessException("해당 분류의 품목이 있어 삭제할 수 없습니다.");
        }
        itemMapper.deleteCategory(id, user.usrId());
    }

    // ----- 품목 -----
    public List<ItItem> getList(String keyword, String cateCd, String itemSts) {
        return itemMapper.findList(keyword, cateCd, itemSts);
    }

    public ItItem getDetail(Long id) {
        ItItem it = itemMapper.findById(id);
        if (it == null) throw new BusinessException("품목을 찾을 수 없습니다.");
        return it;
    }

    @Transactional
    public Long create(ItItem it, LoginUser user) {
        it.setCompCd(user.compCd());
        it.setItemCd(docNoService.generate("IT"));   // IT-00001
        it.setRegId(user.usrId());
        itemMapper.insertItem(it);
        return it.getId();
    }

    @Transactional
    public void update(Long id, ItItem it, LoginUser user) {
        it.setId(id);
        it.setModId(user.usrId());
        if (itemMapper.updateItem(it) == 0) throw new BusinessException("수정 대상이 없습니다.");
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        itemMapper.deleteItem(id, user.usrId());
    }
}
