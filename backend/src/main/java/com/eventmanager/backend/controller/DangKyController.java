package com.eventmanager.backend.controller;

import com.eventmanager.backend.entity.DangKySuKien;
import com.eventmanager.backend.entity.SinhVien;
import com.eventmanager.backend.entity.SuKien;
import com.eventmanager.backend.repository.DangKySuKienRepository;
import com.eventmanager.backend.repository.SinhVienRepository;
import com.eventmanager.backend.repository.SuKienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Use case "Đăng ký tham gia sự kiện": xem thông tin đăng ký, đăng ký, hủy đăng ký, xem trạng thái.
 * Kèm 2 API cho Cán bộ: xem sinh viên đăng ký theo sự kiện và điểm danh (liên kết Cán bộ - Sinh viên).
 */
@RestController
@RequestMapping("/api/dang-ky")
public class DangKyController {

    public static final String DA_DANG_KY = "Đã đăng ký";
    public static final String DA_HUY = "Đã hủy";
    public static final String DA_THAM_GIA = "Đã tham gia";
    public static final String VANG_MAT = "Vắng mặt";

    @Autowired
    private DangKySuKienRepository repo;

    @Autowired
    private SuKienRepository suKienRepo;

    @Autowired
    private SinhVienRepository sinhVienRepo;

    // Cán bộ: sinh viên đã đăng ký (một sự kiện, hoặc tất cả nếu bỏ suKienId), kèm hồ sơ sinh viên.
    // GET /api/dang-ky/theo-su-kien?suKienId=3
    @GetMapping("/theo-su-kien")
    public List<Map<String, Object>> getTheoSuKien(@RequestParam(required = false) Long suKienId) {
        List<DangKySuKien> ds = suKienId == null ? repo.findAll() : repo.findBySuKienId(suKienId);
        Map<Long, SuKien> suKien = new HashMap<>();
        List<Map<String, Object>> kq = new ArrayList<>();
        for (DangKySuKien dk : ds) {
            SuKien sk = suKien.computeIfAbsent(dk.getSuKienId(), id -> suKienRepo.findById(id).orElse(null));
            if (sk == null) continue;
            SinhVien sv = sinhVienRepo.findByTenDangNhap(dk.getTenDangNhap()).orElse(null);

            Map<String, Object> row = new HashMap<>();
            row.put("id", dk.getId());
            row.put("suKienId", sk.getId());
            row.put("tenSuKien", sk.getTenSuKien());
            row.put("thoiGianBatDau", sk.getThoiGianBatDau());
            row.put("tenDangNhap", dk.getTenDangNhap());
            row.put("maSinhVien", sv == null ? "" : sv.getMaSinhVien());
            row.put("hoTen", sv == null || sv.getHoTen() == null ? dk.getTenDangNhap() : sv.getHoTen());
            row.put("email", sv == null ? "" : sv.getEmail());
            row.put("soDienThoai", sv == null ? "" : sv.getSoDienThoai());
            row.put("lop", sv == null ? "" : sv.getLop());
            row.put("ngayDangKy", dk.getNgayDangKy());
            row.put("trangThai", dk.getTrangThai());
            kq.add(row);
        }
        kq.sort(Comparator.comparing((Map<String, Object> m) -> (String) m.get("tenSuKien"))
                .thenComparing(m -> (String) m.get("hoTen")));
        return kq;
    }

    // Cán bộ điểm danh. PUT /api/dang-ky/diem-danh/5   Body: {"trangThai": "Đã tham gia" | "Vắng mặt" | "Đã đăng ký"}
    // ("Đã đăng ký" = đưa về chưa điểm danh). Sinh viên thấy trạng thái mới ngay ở "Sự kiện của tôi".
    @PutMapping("/diem-danh/{id}")
    public ResponseEntity<?> diemDanh(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        DangKySuKien dk = repo.findById(id).orElse(null);
        if (dk == null) {
            return loi("Không tìm thấy lượt đăng ký");
        }
        String trangThai = body.get("trangThai") == null ? "" : body.get("trangThai").toString();
        if (!Set.of(DA_THAM_GIA, VANG_MAT, DA_DANG_KY).contains(trangThai)) {
            return loi("Trạng thái điểm danh không hợp lệ");
        }
        if (DA_HUY.equals(dk.getTrangThai())) {
            return loi("Sinh viên đã hủy đăng ký, không thể điểm danh");
        }
        dk.setTrangThai(trangThai);
        return ResponseEntity.ok(repo.save(dk));
    }

    // Xem thông tin đăng ký: các sự kiện mà sinh viên đã đăng ký (kèm tên, thời gian sự kiện)
    // GET /api/dang-ky/cua-toi?tenDangNhap=hocsinh01
    @GetMapping("/cua-toi")
    public List<Map<String, Object>> getCuaToi(@RequestParam String tenDangNhap) {
        List<Map<String, Object>> kq = new ArrayList<>();
        for (DangKySuKien dk : repo.findByTenDangNhapOrderByNgayDangKyDesc(tenDangNhap)) {
            SuKien sk = suKienRepo.findById(dk.getSuKienId()).orElse(null);
            if (sk == null) continue;

            Map<String, Object> row = new HashMap<>();
            row.put("id", dk.getId());
            row.put("suKienId", sk.getId());
            row.put("maSuKien", sk.getMaSuKien());
            row.put("tenSuKien", sk.getTenSuKien());
            row.put("diaDiem", sk.getDiaDiem());
            row.put("thoiGianBatDau", sk.getThoiGianBatDau());
            row.put("ngayDangKy", dk.getNgayDangKy());
            row.put("trangThai", dk.getTrangThai());
            kq.add(row);
        }
        return kq;
    }

    // Xem trạng thái tham gia của 1 sinh viên với 1 sự kiện
    // GET /api/dang-ky/trang-thai?suKienId=1&tenDangNhap=hocsinh01
    @GetMapping("/trang-thai")
    public Map<String, Object> getTrangThai(@RequestParam Long suKienId, @RequestParam String tenDangNhap) {
        return repo.findBySuKienIdAndTenDangNhap(suKienId, tenDangNhap)
                .<Map<String, Object>>map(dk -> Map.of("trangThai", dk.getTrangThai(), "id", dk.getId()))
                .orElse(Map.of("trangThai", "Chưa đăng ký"));
    }

    // Đăng ký tham gia. Body: {"suKienId": 1, "tenDangNhap": "hocsinh01"}
    @PostMapping("/dang-ky")
    public ResponseEntity<?> dangKy(@RequestBody Map<String, Object> body) {
        if (body.get("suKienId") == null || body.get("tenDangNhap") == null) {
            return loi("Thiếu suKienId hoặc tenDangNhap");
        }
        Long suKienId = ((Number) body.get("suKienId")).longValue();
        String tenDangNhap = body.get("tenDangNhap").toString();

        SuKien sk = suKienRepo.findById(suKienId).orElse(null);
        if (sk == null || !SuKienController.daDuyet(sk)) {
            return loi("Sự kiện không tồn tại");
        }
        if ("Đã hủy".equals(sk.getTrangThai()) || "Đã kết thúc".equals(sk.getTrangThai())) {
            return loi("Sự kiện " + sk.getTrangThai().toLowerCase() + ", không thể đăng ký");
        }
        if (sk.getThoiGianBatDau() != null && sk.getThoiGianBatDau().isBefore(LocalDateTime.now())) {
            return loi("Sự kiện đã bắt đầu, không thể đăng ký");
        }

        DangKySuKien dk = repo.findBySuKienIdAndTenDangNhap(suKienId, tenDangNhap).orElse(null);
        if (dk != null && !DA_HUY.equals(dk.getTrangThai())) {
            return loi("Bạn đã đăng ký sự kiện này rồi");
        }

        long soDaDangKy = repo.countBySuKienIdAndTrangThaiNot(suKienId, DA_HUY);
        if (sk.getSoLuongToiDa() != null && soDaDangKy >= sk.getSoLuongToiDa()) {
            return loi("Sự kiện đã đủ số lượng người tham gia");
        }

        // Hủy rồi đăng ký lại thì dùng lại dòng cũ
        if (dk == null) {
            dk = new DangKySuKien();
            dk.setSuKienId(suKienId);
            dk.setTenDangNhap(tenDangNhap);
        }
        dk.setNgayDangKy(LocalDateTime.now());
        dk.setTrangThai(DA_DANG_KY);
        return ResponseEntity.ok(repo.save(dk));
    }

    // Hủy đăng ký. PUT /api/dang-ky/huy/5?tenDangNhap=hocsinh01
    @PutMapping("/huy/{id}")
    public ResponseEntity<?> huyDangKy(@PathVariable Long id, @RequestParam String tenDangNhap) {
        DangKySuKien dk = repo.findById(id).orElse(null);
        if (dk == null || !dk.getTenDangNhap().equals(tenDangNhap)) {
            return loi("Không tìm thấy đăng ký của bạn");
        }
        if (!DA_DANG_KY.equals(dk.getTrangThai())) {
            return loi("Chỉ hủy được khi trạng thái là \"" + DA_DANG_KY + "\"");
        }
        SuKien sk = suKienRepo.findById(dk.getSuKienId()).orElse(null);
        if (sk != null && sk.getThoiGianBatDau() != null && sk.getThoiGianBatDau().isBefore(LocalDateTime.now())) {
            return loi("Sự kiện đã bắt đầu, không thể hủy đăng ký");
        }

        dk.setTrangThai(DA_HUY);
        return ResponseEntity.ok(repo.save(dk));
    }

    private ResponseEntity<Map<String, String>> loi(String message) {
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }
}