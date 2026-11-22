package com.eventmanager.backend.controller;

import com.eventmanager.backend.admin.common.DuLieu;
import com.eventmanager.backend.admin.service.PhanQuyenService;
import com.eventmanager.backend.admin.service.TaiKhoanAdminService;
import com.eventmanager.backend.admin.service.YeuCauThayDoiService;
import com.eventmanager.backend.entity.TaiKhoan;
import com.eventmanager.backend.repository.TaiKhoanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    @Autowired
    private TaiKhoanAdminService taiKhoanService;

    @Autowired
    private YeuCauThayDoiService yeuCauService;

    @Autowired
    private PhanQuyenService phanQuyenService;

    @Autowired
    private TaiKhoanRepository repo;

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("tenDangNhap");
        String password = request.get("matKhau");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Thiếu tài khoản hoặc mật khẩu"));
        }

        Optional<TaiKhoan> tkOpt = repo.findByTenDangNhap(username.trim());

        if (tkOpt.isPresent()) {
            TaiKhoan tk = tkOpt.get();
            if (tk.getMatKhau().equals(password.trim())) {
                // [Admin] Tài khoản bị khóa thì không cho đăng nhập
                if ("BI_KHOA".equals(tk.getTrangThai())) {
                    return ResponseEntity.status(403).body(Map.of("status", "error",
                            "message", "Tài khoản đã bị khóa. Vui lòng liên hệ Quản trị viên."));
                }
                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "vaiTro", tk.getVaiTro(),
                        "tenDangNhap", tk.getTenDangNhap()
                ));
            }
        }

        return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Sai tài khoản hoặc mật khẩu"));
    }

    // ---------- Tài khoản ----------

    // GET /api/admin/tai-khoan?tuKhoa=nam&vaiTro=CAN_BO&trangThai=HOAT_DONG&trang=0&kichThuoc=10
    @GetMapping("/api/admin/tai-khoan")
    public Map<String, Object> danhSach(@RequestParam(defaultValue = "") String tuKhoa,
                                        @RequestParam(defaultValue = "") String vaiTro,
                                        @RequestParam(defaultValue = "") String trangThai,
                                        @RequestParam(defaultValue = "0") int trang,
                                        @RequestParam(defaultValue = "10") int kichThuoc) {
        return taiKhoanService.timKiem(tuKhoa, vaiTro, trangThai, trang, kichThuoc);
    }

    @GetMapping("/api/admin/tai-khoan/thong-ke")
    public Map<String, Object> thongKe() {
        return taiKhoanService.thongKe();
    }

    @GetMapping("/api/admin/tai-khoan/{id}")
    public Map<String, Object> chiTiet(@PathVariable Long id) {
        return taiKhoanService.chiTiet(id);
    }

    // POST /api/admin/tai-khoan   Body: {tenDangNhap, matKhau, vaiTro, hoTen, email, maSo, soDienThoai}
    @PostMapping("/api/admin/tai-khoan")
    public Map<String, Object> taoMoi(@RequestBody Map<String, Object> body) {
        return taiKhoanService.taoMoi(body);
    }

    @PutMapping("/api/admin/tai-khoan/{id}")
    public Map<String, Object> capNhat(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return taiKhoanService.capNhat(id, body);
    }

    // PUT /api/admin/tai-khoan/5/trang-thai   Body: {"trangThai": "BI_KHOA"}
    @PutMapping("/api/admin/tai-khoan/{id}/trang-thai")
    public Map<String, Object> doiTrangThai(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return taiKhoanService.doiTrangThai(id, DuLieu.chuoi(body, "trangThai"));
    }

    @PutMapping("/api/admin/tai-khoan/{id}/dat-lai-mat-khau")
    public Map<String, String> datLaiMatKhau(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        taiKhoanService.datLaiMatKhau(id, DuLieu.chuoi(body, "matKhauMoi"));
        return Map.of("message", "Đã đặt lại mật khẩu");
    }

    @DeleteMapping("/api/admin/tai-khoan/{id}")
    public Map<String, String> xoa(@PathVariable Long id) {
        taiKhoanService.xoa(id);
        return Map.of("message", "Đã xóa tài khoản");
    }

    // PUT /api/admin/doi-mat-khau   Body: {tenDangNhap, matKhauCu, matKhauMoi}
    @PutMapping("/api/admin/doi-mat-khau")
    public Map<String, String> doiMatKhau(@RequestBody Map<String, Object> body) {
        taiKhoanService.doiMatKhau(DuLieu.chuoi(body, "tenDangNhap"),
                DuLieu.chuoi(body, "matKhauCu"), DuLieu.chuoi(body, "matKhauMoi"));
        return Map.of("message", "Đổi mật khẩu thành công");
    }

    @GetMapping("/api/admin/can-bo")
    public List<Map<String, Object>> dsCanBo() {
        return taiKhoanService.dsCanBoHoatDong();
    }

    // ---------- Yêu cầu thay đổi thông tin ----------

    @GetMapping("/api/admin/yeu-cau")
    public List<Map<String, Object>> dsYeuCau(@RequestParam(defaultValue = "") String trangThai) {
        return yeuCauService.danhSach(trangThai);
    }

    @GetMapping("/api/admin/yeu-cau/{id}")
    public Map<String, Object> chiTietYeuCau(@PathVariable Long id) {
        return yeuCauService.chiTiet(id);
    }

    @PutMapping("/api/admin/yeu-cau/{id}/chap-nhan")
    public Map<String, String> chapNhan(@PathVariable Long id) {
        yeuCauService.chapNhan(id);
        return Map.of("message", "Thay đổi thông tin thành công");
    }

    @PutMapping("/api/admin/yeu-cau/{id}/tu-choi")
    public Map<String, String> tuChoi(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        yeuCauService.tuChoi(id, DuLieu.chuoi(body, "lyDo"));
        return Map.of("message", "Đã từ chối yêu cầu");
    }

    @GetMapping("/api/admin/phan-quyen/quyen")
    public List<Map<String, Object>> dsQuyen() {
        return phanQuyenService.dsQuyen();
    }

    @GetMapping("/api/admin/phan-quyen/tai-khoan/{id}")
    public Map<String, Object> quyenCuaTaiKhoan(@PathVariable Long id) {
        return phanQuyenService.quyenCuaTaiKhoan(id);
    }

    // PUT /api/admin/phan-quyen/tai-khoan/5/quyen   Body: {"dsMaQuyen": ["DUYET_THONG_BAO", "XEM_BAO_CAO"]}
    @PutMapping("/api/admin/phan-quyen/tai-khoan/{id}/quyen")
    public Map<String, Object> capQuyen(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> ds = new ArrayList<>();
        if (body.get("dsMaQuyen") instanceof List<?> list) {
            for (Object o : list) ds.add(String.valueOf(o));
        }
        return phanQuyenService.capQuyen(id, ds);
    }

    // PUT /api/admin/phan-quyen/tai-khoan/5/vai-tro   Body: {"vaiTro": "CAN_BO"}
    @PutMapping("/api/admin/phan-quyen/tai-khoan/{id}/vai-tro")
    public Map<String, Object> doiVaiTro(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return phanQuyenService.doiVaiTro(id, DuLieu.chuoi(body, "vaiTro"));
    }

    // GET /api/admin/phan-quyen/tra-cuu?maQuyen=QL_SU_KIEN&tuKhoa=
    @GetMapping("/api/admin/phan-quyen/tra-cuu")
    public Map<String, Object> traCuu(@RequestParam String maQuyen, @RequestParam(defaultValue = "") String tuKhoa) {
        return phanQuyenService.traCuu(maQuyen, tuKhoa);
    }

    @PostMapping("/api/yeu-cau-thay-doi/gui")
    public Map<String, Object> guiYeuCau(@RequestBody Map<String, Object> body) {
        return yeuCauService.guiYeuCau(body);
    }
}
