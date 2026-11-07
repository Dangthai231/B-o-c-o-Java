package com.eventmanager.backend.admin.controller;

import com.eventmanager.backend.admin.service.TongQuanService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** API trang "Tổng quan" và "Báo cáo". */
@RestController
@RequestMapping("/api/admin")
public class AdminTongQuanController {

    private final TongQuanService service;

    public AdminTongQuanController(TongQuanService service) {
        this.service = service;
    }

    @GetMapping("/tong-quan")
    public Map<String, Object> tongQuan() {
        return service.tongQuan();
    }

    @GetMapping("/bao-cao")
    public Map<String, Object> baoCao() {
        return service.baoCao();
    }
}
