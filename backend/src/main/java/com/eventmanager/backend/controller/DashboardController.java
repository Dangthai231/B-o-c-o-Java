package com.eventmanager.backend.controller;

import com.eventmanager.backend.admin.service.TongQuanService;
import com.eventmanager.backend.repository.TrangChuRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
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

    // Báo cáo cho Cán bộ. Lọc theo ngày bắt đầu sự kiện (bỏ trống = không lọc)
    // VD: GET /api/thongke/bao-cao?tuNgay=2026-10-01&denNgay=2026-10-31
    @GetMapping("/api/thongke/bao-cao")
    public ResponseEntity<?> getBaoCaoThongKe(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
            return ResponseEntity.badRequest().body(Map.of("message", "\"Từ ngày\" phải trước \"Đến ngày\""));
        }
        return ResponseEntity.ok(repo.getThongKeBaoCao(tuNgay, denNgay));
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
