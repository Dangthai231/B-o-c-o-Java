package com.eventmanager.backend.controller;

import com.eventmanager.backend.admin.common.HangSo;
import com.eventmanager.backend.admin.service.ThongBaoAdminService;
import com.eventmanager.backend.entity.SinhVien;
import com.eventmanager.backend.entity.ThongBao;
import com.eventmanager.backend.repository.SinhVienRepository;
import com.eventmanager.backend.repository.TaiKhoanRepository;
import com.eventmanager.backend.repository.ThongBaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Use case "Quản lý thông tin cá nhân": xem và cập nhật thông tin.
 * Kèm thông báo dành cho sinh viên (liên kết với phần Admin duyệt thông báo).
 */
@RestController
@RequestMapping("/api/sinh-vien")
public class SinhVienController {

    @Autowired
    private SinhVienRepository repo;

    @Autowired
    private TaiKhoanRepository taiKhoanRepo;

    @Autowired
    private ThongBaoRepository thongBaoRepo;

    // Thông báo sinh viên được xem: Admin đã duyệt / đã đăng, gửi cho "Tất cả người tham gia" hoặc "Sinh viên".
    // Thông báo chờ duyệt, bị từ chối, đã gỡ hoặc gửi riêng cho cán bộ / ban tổ chức thì không hiện.
    // GET /api/sinh-vien/thong-bao
    @GetMapping("/thong-bao")
    public List<Map<String, Object>> getThongBao() {
        List<Map<String, Object>> kq = new ArrayList<>();
        for (ThongBao tb : thongBaoRepo.findAll()) {
            String tt = ThongBaoAdminService.trangThaiChuan(tb);
            if (!HangSo.DA_DUYET.equals(tt) && !HangSo.TB_DA_DANG.equals(tt)) continue;
            if (!danhChoSinhVien(tb.getDoiTuong())) continue;

            Map<String, Object> m = new HashMap<>();
            m.put("id", tb.getId());
            m.put("maThongBao", tb.getMaThongBao());
            m.put("tieuDe", tb.getTieuDe());
            m.put("noiDung", tb.getNoiDung());
            m.put("doiTuong", tb.getDoiTuong());
            // Ngày hiển thị: ngày Admin đăng, chưa đăng thì lấy ngày gửi
            LocalDateTime ngay = tb.getNgayDang() != null ? tb.getNgayDang()
                    : tb.getNgayGui() != null ? tb.getNgayGui().atStartOfDay() : null;
            m.put("ngay", ngay);
            kq.add(m);
        }
        kq.sort(Comparator.comparing((Map<String, Object> m) -> (LocalDateTime) m.get("ngay"),
                Comparator.nullsLast(Comparator.reverseOrder())));
        return kq;
    }

    private static boolean danhChoSinhVien(String doiTuong) {
        return doiTuong == null || doiTuong.isBlank() || doiTuong.startsWith("Tất cả") || doiTuong.equals("Sinh viên");
    }

    // GET /api/sinh-vien/thong-tin?tenDangNhap=hocsinh01
    @GetMapping("/thong-tin")
    public ResponseEntity<?> getThongTin(@RequestParam String tenDangNhap) {
        if (taiKhoanRepo.findByTenDangNhap(tenDangNhap).isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "Tài khoản không tồn tại"));
        }
        // Tài khoản chưa có hồ sơ thì tạo hồ sơ trống
        SinhVien sv = repo.findByTenDangNhap(tenDangNhap).orElseGet(() -> {
            SinhVien moi = new SinhVien();
            moi.setTenDangNhap(tenDangNhap);
            return repo.save(moi);
        });
        return ResponseEntity.ok(sv);
    }

    // PUT /api/sinh-vien/cap-nhat?tenDangNhap=hocsinh01   Body: {"hoTen": "...", "email": "...", ...}
    @PutMapping("/cap-nhat")
    public ResponseEntity<?> capNhat(@RequestParam String tenDangNhap, @RequestBody SinhVien moi) {
        SinhVien sv = repo.findByTenDangNhap(tenDangNhap).orElse(null);
        if (sv == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy hồ sơ sinh viên"));
        }
        if (moi.getHoTen() == null || moi.getHoTen().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Họ tên không được để trống"));
        }
        if (moi.getEmail() != null && !moi.getEmail().isBlank() && !moi.getEmail().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email không hợp lệ"));
        }
        if (moi.getSoDienThoai() != null && !moi.getSoDienThoai().isBlank() && !moi.getSoDienThoai().matches("^0\\d{9}$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0"));
        }

        // Không cho sửa tenDangNhap
        sv.setMaSinhVien(moi.getMaSinhVien());
        sv.setHoTen(moi.getHoTen().trim());
        sv.setNgaySinh(moi.getNgaySinh());
        sv.setLop(moi.getLop());
        sv.setEmail(moi.getEmail());
        sv.setSoDienThoai(moi.getSoDienThoai());
        return ResponseEntity.ok(repo.save(sv));
    }
}
