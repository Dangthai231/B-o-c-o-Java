package com.eventmanager.backend.controller;

import com.eventmanager.backend.entity.DangKySuKien;
import com.eventmanager.backend.entity.SuKien;
import com.eventmanager.backend.repository.DangKySuKienRepository;
import com.eventmanager.backend.repository.SuKienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use case "Đăng ký tham gia sự kiện": xem thông tin đăng ký, đăng ký, hủy đăng ký, xem trạng thái.
 */
@RestController
@RequestMapping("/api/dang-ky")
public class DangKyController {

    public static final String DA_DANG_KY = "Đã đăng ký";
    public static final String DA_HUY = "Đã hủy";

    @Autowired
    private DangKySuKienRepository repo;

    @Autowired
    private SuKienRepository suKienRepo;

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