package com.eventmanager.backend.admin.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Bắt lỗi cho TẤT CẢ controller trong gói admin, đổi thành JSON thống nhất: {"message": "..."}.
 * Chỉ áp dụng cho gói admin nên không ảnh hưởng code các thành viên khác.
 */
@RestControllerAdvice(basePackages = "com.eventmanager.backend.admin")
public class AdminExceptionHandler {

    @ExceptionHandler(LoiNghiepVu.class)
    public ResponseEntity<Map<String, String>> xuLyLoiNghiepVu(LoiNghiepVu e) {
        return ResponseEntity.status(e.getMaHttp()).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> xuLyLoiKhac(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
    }
}
