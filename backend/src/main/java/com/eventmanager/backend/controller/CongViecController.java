package com.eventmanager.backend.controller;

import com.eventmanager.backend.entity.CongViec;
import com.eventmanager.backend.entity.SuKien;
import com.eventmanager.backend.entity.TaiKhoan;
import com.eventmanager.backend.repository.CongViecRepository;
import com.eventmanager.backend.repository.SuKienRepository;
import com.eventmanager.backend.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API cho chức năng Quản lý công việc của Cán bộ phụ trách sự kiện:
 * - Lập kế hoạch sự kiện (thêm / sửa công việc gắn với sự kiện)
 * - Phân công nhiệm vụ (gán nhân sự phụ trách)
 * - Theo dõi tiến độ (thống kê, tiến độ %, lọc theo sự kiện/trạng thái)
 * - Cập nhật trạng thái công việc
 */
@RestController
@RequestMapping("/api/cong-viec")
public class CongViecController {

    @Autowired
    private CongViecRepository repo;

    @Autowired
    private SuKienRepository suKienRepo;

    @Autowired
    private TaiKhoanRepository taiKhoanRepo;

    // 1. Lấy danh sách tất cả công việc (hỗ trợ lọc tùy chọn)
    @GetMapping("/danh-sach")
    public List<CongViec> getDanhSach(
            @RequestParam(required = false) Long suKienId,
            @RequestParam(required = false) String trangThai) {
        if (suKienId != null) {
            return repo.findBySuKienIdOrderByIdDesc(suKienId);
        }
        if (trangThai != null && !trangThai.isBlank() && !"Tất cả".equals(trangThai)) {
            return repo.findByTrangThaiOrderByIdDesc(trangThai);
        }
        return repo.findAllByOrderByIdDesc();
    }

    // Lọc theo sự kiện
    @GetMapping("/theo-su-kien/{suKienId}")
    public List<CongViec> theoSuKien(@PathVariable Long suKienId) {
        return repo.findBySuKienIdOrderByIdDesc(suKienId);
    }

    // Lọc theo trạng thái
    @GetMapping("/theo-trang-thai")
    public List<CongViec> theoTrangThai(@RequestParam String trangThai) {
        return repo.findByTrangThaiOrderByIdDesc(trangThai);
    }

    // 2. Thống kê tổng quan & theo dõi tiến độ
    @GetMapping("/thong-ke")
    public Map<String, Object> thongKe() {
        Map<String, Object> map = new HashMap<>();
        map.put("tongCong", repo.count());
        map.put("chuaBatDau", repo.demTheoTrangThai("Chưa bắt đầu"));
        map.put("dangThucHien", repo.demTheoTrangThai("Đang thực hiện"));
        map.put("hoanThanh", repo.demTheoTrangThai("Hoàn thành"));
        map.put("tamHoan", repo.demTheoTrangThai("Tạm hoãn"));
        map.put("quaHan", repo.demQuaHan());
        Double avg = repo.tinhTienDoTrungBinh();
        map.put("tienDoTrungBinh", avg == null ? 0 : Math.round(avg));
        return map;
    }

    // 3. Danh sách sự kiện phục vụ Lập kế hoạch công việc
    @GetMapping("/su-kien")
    public List<Map<String, Object>> danhSachSuKien() {
        return suKienRepo.findAll().stream().map(sk -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", sk.getId());
            m.put("maSuKien", sk.getMaSuKien());
            m.put("tenSuKien", sk.getTenSuKien());
            m.put("trangThai", sk.getTrangThai());
            return m;
        }).toList();
    }

    // 4. Danh sách nhân sự (Cán bộ) phục vụ Phân công nhiệm vụ
    @GetMapping("/nhan-su")
    public List<Map<String, Object>> danhSachNhanSu() {
        List<TaiKhoan> canBoList = taiKhoanRepo.findByVaiTroOrderByHoTenAsc("CAN_BO");
        return canBoList.stream().map(tk -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", tk.getId());
            m.put("tenDangNhap", tk.getTenDangNhap());
            String hoTen = (tk.getHoTen() != null && !tk.getHoTen().isBlank())
                    ? tk.getHoTen() + " (" + tk.getTenDangNhap() + ")"
                    : tk.getTenDangNhap();
            m.put("hienThi", hoTen);
            return m;
        }).toList();
    }

    // 5. Lập kế hoạch / Thêm công việc mới
    @PostMapping("/them")
    public ResponseEntity<CongViec> themCongViec(@RequestBody CongViec cv) {
        if (cv.getMaCv() == null || cv.getMaCv().isBlank()) {
            long count = repo.count() + 1;
            cv.setMaCv(String.format("CV%03d", count));
        }
        dongBoTenSuKien(cv);
        if (cv.getTrangThai() == null || cv.getTrangThai().isBlank()) {
            cv.setTrangThai("Chưa bắt đầu");
        }
        if (cv.getTienDo() == null) {
            cv.setTienDo("Hoàn thành".equals(cv.getTrangThai()) ? 100 : 0);
        }
        CongViec saved = repo.save(cv);
        return ResponseEntity.ok(saved);
    }

    // 6. Cập nhật toàn bộ thông tin công việc
    @PutMapping("/cap-nhat/{id}")
    public ResponseEntity<CongViec> capNhat(@PathVariable Long id, @RequestBody CongViec cvDetails) {
        return repo.findById(id).map(cv -> {
            if (cvDetails.getMaCv() != null && !cvDetails.getMaCv().isBlank()) {
                cv.setMaCv(cvDetails.getMaCv());
            }
            cv.setTenCongViec(cvDetails.getTenCongViec());
            cv.setMoTa(cvDetails.getMoTa());
            cv.setTrangThai(cvDetails.getTrangThai());
            cv.setTienDo(cvDetails.getTienDo());
            cv.setNhanSu(cvDetails.getNhanSu());
            cv.setSuKienId(cvDetails.getSuKienId());
            cv.setTenSuKien(cvDetails.getTenSuKien());
            dongBoTenSuKien(cv);
            cv.setMucDoUuTien(cvDetails.getMucDoUuTien());
            cv.setHanChot(cvDetails.getHanChot());
            if ("Hoàn thành".equals(cv.getTrangThai())) {
                cv.setTienDo(100);
            }
            return ResponseEntity.ok(repo.save(cv));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 7. Phân công nhiệm vụ (Use Case: Phân công nhiệm vụ)
    @PutMapping("/phan-cong/{id}")
    public ResponseEntity<CongViec> phanCong(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return repo.findById(id).map(cv -> {
            if (body.containsKey("nhanSu") && body.get("nhanSu") != null) {
                cv.setNhanSu(body.get("nhanSu").toString().trim());
            }
            if (body.containsKey("mucDoUuTien") && body.get("mucDoUuTien") != null) {
                cv.setMucDoUuTien(body.get("mucDoUuTien").toString().trim());
            }
            if (body.containsKey("moTa") && body.get("moTa") != null) {
                cv.setMoTa(body.get("moTa").toString().trim());
            }
            return ResponseEntity.ok(repo.save(cv));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 8. Cập nhật trạng thái & tiến độ (Use Case: Theo dõi tiến độ & Cập nhật trạng thái công việc)
    @PutMapping("/cap-nhat-trang-thai/{id}")
    public ResponseEntity<CongViec> capNhatTrangThai(@PathVariable Long id,
                                                      @RequestParam String trangThai,
                                                      @RequestParam(required = false) Integer tienDo) {
        return repo.findById(id).map(cv -> {
            cv.setTrangThai(trangThai);
            if (tienDo != null) {
                cv.setTienDo(Math.max(0, Math.min(100, tienDo)));
            }
            if ("Hoàn thành".equals(trangThai)) {
                cv.setTienDo(100);
            } else if ("Chưa bắt đầu".equals(trangThai) && tienDo == null) {
                cv.setTienDo(0);
            }
            return ResponseEntity.ok(repo.save(cv));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 9. Xóa công việc
    @DeleteMapping("/xoa/{id}")
    public ResponseEntity<Void> xoa(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    private void dongBoTenSuKien(CongViec cv) {
        if (cv.getSuKienId() != null) {
            suKienRepo.findById(cv.getSuKienId())
                    .map(SuKien::getTenSuKien)
                    .ifPresent(cv::setTenSuKien);
        }
    }
}
