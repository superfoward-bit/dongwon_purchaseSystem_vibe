package com.purchasesystem.domain.segment;

import com.purchasesystem.common.dto.ApiResponse;
import com.purchasesystem.common.exception.BusinessException;
import com.purchasesystem.domain.segment.mapper.SegmentMapper;
import com.purchasesystem.security.jwt.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 협력사 세그먼트 마스터 */
@RestController
@RequestMapping("/api/srm/segment")
@RequiredArgsConstructor
public class SegmentController {

    private final SegmentMapper segmentMapper;

    @GetMapping
    public ApiResponse<List<VdSegment>> list() {
        return ApiResponse.ok(segmentMapper.findList());
    }

    @PostMapping
    public ApiResponse<Long> create(@RequestBody VdSegment s, @AuthenticationPrincipal LoginUser user) {
        if (s.getSegCd() == null || s.getSegCd().isBlank()) throw new BusinessException("세그먼트 코드를 입력하세요.");
        if (segmentMapper.countByCd(s.getSegCd()) > 0) throw new BusinessException("이미 존재하는 세그먼트 코드입니다: " + s.getSegCd());
        s.setCompCd(user.compCd());
        s.setRegId(user.usrId());
        segmentMapper.insert(s);
        return ApiResponse.ok("등록되었습니다.", s.getId());
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable Long id, @RequestBody VdSegment s, @AuthenticationPrincipal LoginUser user) {
        s.setId(id);
        s.setRegId(user.usrId());
        segmentMapper.update(s);
        return ApiResponse.ok("수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        segmentMapper.delete(id, user.usrId());
        return ApiResponse.ok("삭제되었습니다.", null);
    }

    /** 협력사 세그먼트 배정 */
    @PostMapping("/assign")
    public ApiResponse<Void> assign(@RequestBody Map<String, Object> body, @AuthenticationPrincipal LoginUser user) {
        Long vendorId = Long.valueOf(String.valueOf(body.get("vendorId")));
        String segCd = (String) body.get("segCd");
        VdSegment seg = segCd == null ? null : segmentMapper.findByCd(segCd);
        segmentMapper.assignVendor(vendorId, segCd, seg != null ? seg.getSegNm() : null, user.usrId());
        return ApiResponse.ok("세그먼트가 배정되었습니다.", null);
    }
}
