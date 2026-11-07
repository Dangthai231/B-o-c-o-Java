package com.eventmanager.backend.admin.controller;

import com.eventmanager.backend.admin.service.YeuCauThayDoiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API cho NGƯỜI DÙNG gửi yêu cầu thay đổi thông tin (Admin sẽ duyệt).
 * POST /api/yeu-cau-thay-doi/gui  Body: {tenDangNhap, hoTenMoi, emailMoi, soDienThoaiMoi, lopMoi, lyDo}
 */
@RestController
@RequestMapping("/api/yeu-cau-thay-doi")
public class YeuCauThayDoiController {

    private final YeuCauThayDoiService service;

    public YeuCauThayDoiController(YeuCauThayDoiService service) {
        this.service = service;
    }

    @PostMapping("/gui")
    public Map<String, Object> gui(@RequestBody Map<String, Object> body) {
        return service.guiYeuCau(body);
    }
}
