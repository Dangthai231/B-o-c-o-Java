package com.eventmanager.backend.controller;

import com.eventmanager.backend.entity.TaiKhoan;
import com.eventmanager.backend.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private TaiKhoanRepository repo;

    @PostMapping("/login")
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
}
