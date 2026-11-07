package com.eventmanager.backend.controller;

import com.eventmanager.backend.entity.SuKien;
import com.eventmanager.backend.repository.DangKySuKienRepository;
import com.eventmanager.backend.repository.SuKienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use case "Tra cứu sự kiện": xem danh sách, tìm kiếm, lọc theo ngày, xem chi tiết.
 */
@RestController
@RequestMapping("/api/su-kien")
public class SuKienController {

    @Autowired
    private SuKienRepository repo;

    @Autowired
    private DangKySuKienRepository dangKyRepo;

    // Xem danh sách + tìm kiếm + lọc theo ngày (tham số nào bỏ trống thì không lọc theo nó)
    // VD: GET /api/su-kien/danh-sach?tuKhoa=hiến máu&tuNgay=2026-11-01&denNgay=2026-11-30
    @GetMapping("/danh-sach")
    public List<Map<String, Object>> getDanhSach(
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {

        LocalDateTime tu = tuNgay != null ? tuNgay.atStartOfDay() : LocalDateTime.of(1900, 1, 1, 0, 0);
        // hết ngày denNgay = đầu ngày hôm sau trừ 1 giây
        LocalDateTime den = denNgay != null ? denNgay.plusDays(1).atStartOfDay().minusSeconds(1)
                : LocalDateTime.of(2999, 12, 31, 23, 59);

        return repo.findByTenSuKienContainingIgnoreCaseAndThoiGianBatDauBetweenOrderByThoiGianBatDauAsc(
                        tuKhoa.trim(), tu, den)
                .stream().filter(SuKienController::daDuyet).map(this::kemSoDangKy).toList();
    }

    // Thông tin sự kiện + số người đang giữ chỗ, để giao diện hiện "12 / 40 chỗ"
    private Map<String, Object> kemSoDangKy(SuKien sk) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", sk.getId());
        m.put("maSuKien", sk.getMaSuKien());
        m.put("tenSuKien", sk.getTenSuKien());
        m.put("moTa", sk.getMoTa());
        m.put("diaDiem", sk.getDiaDiem());
        m.put("thoiGianBatDau", sk.getThoiGianBatDau());
        m.put("thoiGianKetThuc", sk.getThoiGianKetThuc());
        m.put("soLuongToiDa", sk.getSoLuongToiDa());
        m.put("donViToChuc", sk.getDonViToChuc());
        m.put("trangThai", trangThaiHienTai(sk));
        m.put("soDaDangKy", dangKyRepo.countBySuKienIdAndTrangThaiNot(sk.getId(), DangKyController.DA_HUY));
        return m;
    }

    // Trạng thái lưu trong DB không tự đổi theo thời gian -> tính lại theo giờ hiện tại
    // (sự kiện "Đã hủy" giữ nguyên)
    public static String trangThaiHienTai(SuKien sk) {
        String tt = sk.getTrangThai();
        if (!"Sắp diễn ra".equals(tt) && !"Đang diễn ra".equals(tt)) return tt;
        LocalDateTime now = LocalDateTime.now();
        if (sk.getThoiGianKetThuc() != null && now.isAfter(sk.getThoiGianKetThuc())) return "Đã kết thúc";
        if (sk.getThoiGianBatDau() != null && now.isAfter(sk.getThoiGianBatDau())) return "Đang diễn ra";
        return "Sắp diễn ra";
    }

    // Sinh viên chỉ thấy sự kiện đã được Admin duyệt (NULL = sự kiện cũ, coi như đã duyệt)
    public static boolean daDuyet(SuKien sk) {
        return sk.getTrangThaiDuyet() == null || "Đã duyệt".equals(sk.getTrangThaiDuyet());
    }

    // Xem chi tiết sự kiện, kèm số người đã đăng ký
    @GetMapping("/{id}")
    public ResponseEntity<?> getChiTiet(@PathVariable Long id) {
        SuKien sk = repo.findById(id).orElse(null);
        if (sk == null || !daDuyet(sk)) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy sự kiện"));
        }

        Map<String, Object> thongTin = kemSoDangKy(sk);
        Map<String, Object> kq = new HashMap<>();
        kq.put("suKien", thongTin);
        kq.put("soDaDangKy", thongTin.get("soDaDangKy"));
        return ResponseEntity.ok(kq);
    }
}