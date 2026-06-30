package com.purchasesystem.domain.notice;

import com.purchasesystem.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 알림 발송이력 조회 */
@RestController
@RequestMapping("/api/sys/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<List<CmNotice>> list(@RequestParam(required = false) String notiTyp,
                                            @RequestParam(required = false) String sendSts,
                                            @RequestParam(required = false) String toUsrId,
                                            @RequestParam(required = false) String refTyp,
                                            @RequestParam(required = false) Long refId) {
        return ApiResponse.ok(notificationService.getList(notiTyp, sendSts, toUsrId, refTyp, refId));
    }
}
