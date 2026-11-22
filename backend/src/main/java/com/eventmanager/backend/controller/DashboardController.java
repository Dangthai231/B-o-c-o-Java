package com.eventmanager.backend.controller;

import com.eventmanager.backend.admin.service.TongQuanService;
import com.eventmanager.backend.repository.TrangChuRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DashboardController {

    @Autowired
    private TongQuanService tongQuanService;

    @Autowired
    private TrangChuRepository repo;

    @GetMapping("/api/thongke/tong-quan")
    public ResponseEntity<Map<String, Object>> getTongQuanDashboard() {
        return ResponseEntity.ok(repo.getThongKeTongQuan());
    }

    @GetMapping("/api/thongke/bao-cao")
    public ResponseEntity<Map<String, Object>> getBaoCaoThongKe() {
        return ResponseEntity.ok(repo.getThongKeBaoCao());
    }

    @GetMapping("/api/admin/tong-quan")
    public Map<String, Object> tongQuan() {
        return tongQuanService.tongQuan();
    }

    @GetMapping("/api/admin/bao-cao")
    public Map<String, Object> baoCao() {
        return tongQuanService.baoCao();
    }
}
