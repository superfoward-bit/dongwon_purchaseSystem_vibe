package com.purchasesystem.domain.code;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.code.mapper.CodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeMapper codeMapper;

    public List<CmCodeGrp> getGroups(String keyword) {
        return codeMapper.findGroups(keyword);
    }

    public List<CmCode> getCodes(String grpCd) {
        return codeMapper.findCodes(grpCd);
    }

    @Transactional
    public void createCode(CmCode code, String userId) {
        if (codeMapper.countCode(code.getGrpCd(), code.getCd()) > 0) {
            throw new BusinessException("이미 존재하는 코드입니다: " + code.getCd());
        }
        code.setRegId(userId);
        codeMapper.insertCode(code);
    }

    @Transactional
    public void updateCode(CmCode code, String userId) {
        code.setModId(userId);
        if (codeMapper.updateCode(code) == 0) {
            throw new BusinessException("수정 대상이 없습니다.");
        }
    }

    @Transactional
    public void deleteCode(Long id, String userId) {
        codeMapper.deleteCode(id, userId);
    }
}
