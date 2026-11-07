package com.eventmanager.backend.admin.controller;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.service.SuKienAdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** API màn hình "Quản lý sự kiện" của Admin. */
@RestController
@RequestMapping("/api/admin/su-kien")
public class AdminSuKienController {

    private final SuKienAdminService service;

    public AdminSuKienController(SuKienAdminService service) {
        this.service = service;
    }

    // GET /api/admin/su-kien?tuKhoa=&trangThai=Sắp diễn ra&trangThaiDuyet=Chờ duyệt
    @GetMapping
    public List<Map<String, Object>> danhSach(@RequestParam(defaultValue = "") String tuKhoa,
                                              @RequestParam(defaultValue = "") String trangThai,
                                              @RequestParam(defaultValue = "") String trangThaiDuyet) {
        return service.danhSach(tuKhoa, trangThai, trangThaiDuyet);
    }

    @GetMapping("/{id}")
    public Map<String, Object> chiTiet(@PathVariable Long id) {
        return service.chiTiet(id);
    }

    // POST /api/admin/su-kien?nguoiTao=admin
    // Body: {tenSuKien, diaDiem, thoiGianBatDau:"15/11/2026 08:00", thoiGianKetThuc, soLuongToiDa, donViToChuc, moTa}
    @PostMapping
    public Map<String, Object> taoMoi(@RequestBody Map<String, Object> body,
                                      @RequestParam(defaultValue = "admin") String nguoiTao) {
        return service.taoMoi(body, nguoiTao);
    }

    @PutMapping("/{id}")
    public Map<String, Object> capNhat(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return service.capNhat(id, body);
    }

    @PutMapping("/{id}/duyet")
    public Map<String, String> duyet(@PathVariable Long id) {
        service.duyetKeHoach(id);
        return Map.of("message", "Duyệt kế hoạch thành công");
    }

    @PutMapping("/{id}/tu-choi")
    public Map<String, String> tuChoi(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        service.tuChoiKeHoach(id, DuLieu.chuoi(body, "lyDo"));
        return Map.of("message", "Đã từ chối kế hoạch");
    }

    @GetMapping("/{id}/theo-doi")
    public Map<String, Object> theoDoi(@PathVariable Long id) {
        return service.theoDoi(id);
    }

    @GetMapping("/{id}/phan-cong")
    public List<Map<String, Object>> dsPhanCong(@PathVariable Long id) {
        return service.dsPhanCong(id);
    }

    // POST /api/admin/su-kien/3/phan-cong   Body: {"tenDangNhap": "canbo01", "vaiTroPhuTrach": "Hậu cần"}
    @PostMapping("/{id}/phan-cong")
    public Map<String, String> phanCong(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        service.phanCong(id, DuLieu.chuoi(body, "tenDangNhap"), DuLieu.chuoi(body, "vaiTroPhuTrach"));
        return Map.of("message", "Phân công thành công");
    }

    @DeleteMapping("/phan-cong/{phanCongId}")
    public Map<String, String> huyPhanCong(@PathVariable Long phanCongId) {
        service.huyPhanCong(phanCongId);
        return Map.of("message", "Đã hủy phân công");
    }
}
