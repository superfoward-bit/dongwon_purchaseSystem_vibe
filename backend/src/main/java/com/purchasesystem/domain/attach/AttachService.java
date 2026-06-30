package com.purchasesystem.domain.attach;

import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.attach.mapper.AttachMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachService {

    @Value("${app.upload-dir:C:/purchasesystem/uploads}")
    private String uploadDir;

    private final AttachMapper attachMapper;

    public List<CmAttach> list(String refTyp, Long refId) { return attachMapper.findByRef(refTyp, refId); }

    @Transactional
    public CmAttach upload(String refTyp, Long refId, MultipartFile file, LoginUser user) {
        if (file == null || file.isEmpty()) throw new BusinessException("업로드할 파일이 없습니다.");
        try {
            Path dir = Paths.get(uploadDir, refTyp);
            Files.createDirectories(dir);
            String orig = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String ext = orig.contains(".") ? orig.substring(orig.lastIndexOf('.')) : "";
            String storNm = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(storNm);
            file.transferTo(target.toFile());

            CmAttach a = new CmAttach();
            a.setCompCd(user.compCd());
            a.setRefTyp(refTyp);
            a.setRefId(refId);
            a.setFileNm(orig);
            a.setStorNm(storNm);
            a.setFilePath(target.toString());
            a.setFileSize(file.getSize());
            a.setContentTyp(file.getContentType());
            a.setRegId(user.usrId());
            attachMapper.insert(a);
            return a;
        } catch (IOException e) {
            throw new BusinessException("파일 저장 실패: " + e.getMessage());
        }
    }

    public CmAttach get(Long id) {
        CmAttach a = attachMapper.findById(id);
        if (a == null) throw new BusinessException("첨부파일을 찾을 수 없습니다.");
        return a;
    }

    public byte[] readBytes(CmAttach a) {
        try {
            return Files.readAllBytes(Paths.get(a.getFilePath()));
        } catch (IOException e) {
            throw new BusinessException("파일 읽기 실패: " + e.getMessage());
        }
    }

    @Transactional
    public void delete(Long id, LoginUser user) {
        CmAttach a = attachMapper.findById(id);
        if (a == null) return;
        attachMapper.softDelete(id, user.usrId());
    }
}
