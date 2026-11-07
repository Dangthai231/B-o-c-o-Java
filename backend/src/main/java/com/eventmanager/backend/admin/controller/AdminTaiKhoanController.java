package com.eventmanager.backend.admin.controller;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.service.TaiKhoanAdminService;
import com.eventmanager.backend.admin.service.YeuCauThayDoiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * API màn hình "Quản lý tài khoản" (2 tab: Danh sách tài khoản, Duyệt thay đổi thông tin).
 * Controller chỉ nhận tham số rồi gọi Service; mọi kiểm tra nghiệp vụ nằm trong Service.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminTaiKhoanController {

    private final TaiKhoanAdminService taiKhoanService;
    private final YeuCauThayDoiService yeuCauService;

    public AdminTaiKhoanController(TaiKhoanAdminService taiKhoanService, YeuCauThayDoiService yeuCauService) {
        this.taiKhoanService = taiKhoanService;
        this.yeuCauService = yeuCauService;
    }

    // ---------- Tài khoản ----------

    // GET /api/admin/tai-khoan?tuKhoa=nam&vaiTro=CAN_BO&trangThai=HOAT_DONG&trang=0&kichThuoc=10
    @GetMapping("/tai-khoan")
    public Map<String, Object> danhSach(@RequestParam(defaultValue = "") String tuKhoa,
                                        @RequestParam(defaultValue = "") String vaiTro,
                                        @RequestParam(defaultValue = "") String trangThai,
                                        @RequestParam(defaultValue = "0") int trang,
                                        @RequestParam(defaultValue = "10") int kichThuoc) {
        return taiKhoanService.timKiem(tuKhoa, vaiTro, trangThai, trang, kichThuoc);
    }

    @GetMapping("/tai-khoan/thong-ke")
    public Map<String, Object> thongKe() {
        return taiKhoanService.thongKe();
    }

    @GetMapping("/tai-khoan/{id}")
    public Map<String, Object> chiTiet(@PathVariable Long id) {
        return taiKhoanService.chiTiet(id);
    }

    // POST /api/admin/tai-khoan   Body: {tenDangNhap, matKhau, vaiTro, hoTen, email, maSo, soDienThoai}
    @PostMapping("/tai-khoan")
    public Map<String, Object> taoMoi(@RequestBody Map<String, Object> body) {
        return taiKhoanService.taoMoi(body);
    }

    @PutMapping("/tai-khoan/{id}")
    public Map<String, Object> capNhat(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return taiKhoanService.capNhat(id, body);
    }

    // PUT /api/admin/tai-khoan/5/trang-thai   Body: {"trangThai": "BI_KHOA"}
    @PutMapping("/tai-khoan/{id}/trang-thai")
    public Map<String, Object> doiTrangThai(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return taiKhoanService.doiTrangThai(id, DuLieu.chuoi(body, "trangThai"));
    }

    @PutMapping("/tai-khoan/{id}/dat-lai-mat-khau")
    public Map<String, String> datLaiMatKhau(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        taiKhoanService.datLaiMatKhau(id, DuLieu.chuoi(body, "matKhauMoi"));
        return Map.of("message", "Đã đặt lại mật khẩu");
    }

    @DeleteMapping("/tai-khoan/{id}")
    public Map<String, String> xoa(@PathVariable Long id) {
        taiKhoanService.xoa(id);
        return Map.of("message", "Đã xóa tài khoản");
    }

    // PUT /api/admin/doi-mat-khau   Body: {tenDangNhap, matKhauCu, matKhauMoi}
    @PutMapping("/doi-mat-khau")
    public Map<String, String> doiMatKhau(@RequestBody Map<String, Object> body) {
        taiKhoanService.doiMatKhau(DuLieu.chuoi(body, "tenDangNhap"),
                DuLieu.chuoi(body, "matKhauCu"), DuLieu.chuoi(body, "matKhauMoi"));
        return Map.of("message", "Đổi mật khẩu thành công");
    }

    @GetMapping("/can-bo")
    public List<Map<String, Object>> dsCanBo() {
        return taiKhoanService.dsCanBoHoatDong();
    }

    // ---------- Yêu cầu thay đổi thông tin ----------

    @GetMapping("/yeu-cau")
    public List<Map<String, Object>> dsYeuCau(@RequestParam(defaultValue = "") String trangThai) {
        return yeuCauService.danhSach(trangThai);
    }

    @GetMapping("/yeu-cau/{id}")
    public Map<String, Object> chiTietYeuCau(@PathVariable Long id) {
        return yeuCauService.chiTiet(id);
    }

    @PutMapping("/yeu-cau/{id}/chap-nhan")
    public Map<String, String> chapNhan(@PathVariable Long id) {
        yeuCauService.chapNhan(id);
        return Map.of("message", "Thay đổi thông tin thành công");
    }

    @PutMapping("/yeu-cau/{id}/tu-choi")
    public Map<String, String> tuChoi(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        yeuCauService.tuChoi(id, DuLieu.chuoi(body, "lyDo"));
        return Map.of("message", "Đã từ chối yêu cầu");
    }
}
