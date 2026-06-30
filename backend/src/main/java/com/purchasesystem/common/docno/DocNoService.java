package com.purchasesystem.common.docno;

import com.purchasesystem.common.docno.mapper.DocNoMapper;
import com.purchasesystem.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 문서번호 채번. 예: PR + '-' + YYYYMM + '-' + 0001
 * DATE_FMT 기준일이 바뀌면 일련번호를 리셋한다.
 */
@Service
@RequiredArgsConstructor
public class DocNoService {

    private final DocNoMapper docNoMapper;

    @Transactional
    public String generate(String docTyp) {
        CmDocNo rule = docNoMapper.selectForUpdate(docTyp);
        if (rule == null) {
            throw new BusinessException("채번 규칙이 없습니다: " + docTyp);
        }

        String ymd = currentYmd(rule.getDateFmt());
        // 날짜포맷이 없으면(코드성 채번) Oracle이 빈 문자열을 NULL로 저장해 비교가 깨지므로 '*' 키 사용
        String ymdKey = ymd.isEmpty() ? "*" : ymd;
        long nextSeq = ymdKey.equals(rule.getLastYmd()) ? rule.getLastSeq() + 1 : 1;

        docNoMapper.updateSeq(rule.getId(), ymdKey, nextSeq);

        int len = rule.getSeqLen() == null ? 4 : rule.getSeqLen();
        String seqStr = String.format("%0" + len + "d", nextSeq);

        StringBuilder sb = new StringBuilder();
        if (rule.getPrefix() != null) sb.append(rule.getPrefix()).append("-");
        if (!ymd.isEmpty()) sb.append(ymd).append("-");
        sb.append(seqStr);
        return sb.toString();
    }

    private String currentYmd(String dateFmt) {
        if (dateFmt == null || dateFmt.isBlank()) return "";
        String pattern = dateFmt.replace("YYYY", "yyyy").replace("DD", "dd");
        return LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
    }
}
