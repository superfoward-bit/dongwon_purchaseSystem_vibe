package com.purchasesystem.domain.attach;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/attach")
@RequiredArgsConstructor
public class AttachController {

    private final AttachService attachService;

    @GetMapping
    public ApiResponse<List<CmAttach>> list(@RequestParam String refTyp, @RequestParam Long refId) {
        return ApiResponse.ok(attachService.list(refTyp, refId));
    }

    @PostMapping
    public ApiResponse<CmAttach> upload(@RequestParam String refTyp, @RequestParam Long refId,
                                        @RequestParam("file") MultipartFile file,
                                        @AuthenticationPrincipal LoginUser user) {
        return ApiResponse.ok("업로드되었습니다.", attachService.upload(refTyp, refId, file, user));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable Long id) {
        CmAttach a = attachService.get(id);
        byte[] bytes = attachService.readBytes(a);
        String fn = URLEncoder.encode(a.getFileNm(), StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fn)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        attachService.delete(id, user);
        return ApiResponse.ok("삭제되었습니다.", null);
    }
}
