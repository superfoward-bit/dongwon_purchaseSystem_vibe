package com.purchasesystem.domain.iface;

import com.purchasesystem.common.docno.DocNoService;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.iface.mapper.IfMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 대외 인터페이스(SAP/EAI/국세청) 연계 프레임.
 * 이력 기록 + 시뮬레이션 발송(성공). 실제 EAI 도입 시 dispatch() 내부만 교체.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IfService {

    private final IfMapper ifMapper;
    private final DocNoService docNoService;

    public List<IfInterface> getList(String ifTyp, String ifStatus, String keyword) {
        return ifMapper.findList(ifTyp, ifStatus, keyword);
    }

    /** 인터페이스 송신 기록 + 시뮬레이션 발송. 실패해도 호출측에 영향 없도록 예외를 삼킨다. */
    @Transactional
    public Long send(String ifTyp, String ifNm, String sysCd, String refTyp, Long refId, String refNo,
                     String payload, String compCd, String regId) {
        IfInterface i = new IfInterface();
        i.setCompCd(compCd);
        i.setIfNo(docNoService.generate("IF"));
        i.setIfTyp(ifTyp);
        i.setIfNm(ifNm);
        i.setDirection("SEND");
        i.setSysCd(sysCd);
        i.setRefTyp(refTyp);
        i.setRefId(refId);
        i.setRefNo(refNo);
        i.setPayload(payload);
        i.setRegId(regId);
        ifMapper.insert(i);
        dispatch(i, false);
        return i.getId();
    }

    /** 재전송 */
    @Transactional
    public String resend(Long id) {
        IfInterface i = ifMapper.findById(id);
        if (i == null) throw new BusinessException("인터페이스 이력을 찾을 수 없습니다.");
        dispatch(i, true);
        return ifMapper.findById(id).getIfStatus();
    }

    /** 시뮬레이션 디스패치: 성공 처리(실제 연동 시 이 메서드 교체) */
    private void dispatch(IfInterface i, boolean retry) {
        try {
            log.info("[IF-SIM] {} {} → {} ({}) :: {}", i.getIfNo(), i.getIfTyp(), i.getSysCd(), i.getRefNo(), i.getPayload());
            ifMapper.updateResult(i.getId(), "SUCCESS", "정상 연계(시뮬레이션) - " + (i.getSysCd() == null ? "EXT" : i.getSysCd()), retry);
        } catch (Exception e) {
            ifMapper.updateResult(i.getId(), "FAIL", e.getMessage(), retry);
        }
    }
}
