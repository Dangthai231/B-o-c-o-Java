package com.eventmanager.backend.admin.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

/** Trả lỗi JSON cho API quản trị và API gửi yêu cầu thay đổi thông tin. */
@RestControllerAdvice(basePackages = "com.eventmanager.backend.controller")
public class AdminExceptionHandler {

    @ExceptionHandler(LoiNghiepVu.class)
    public ResponseEntity<Map<String, String>> xuLyLoiNghiepVu(LoiNghiepVu e) {
        return ResponseEntity.status(e.getMaHttp()).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> xuLyLoiKhac(Exception e, HttpServletRequest request) throws Exception {
        Object mapping = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (!(mapping instanceof String path)
                || (!path.startsWith("/api/admin/") && !path.equals("/api/yeu-cau-thay-doi/gui"))) {
            throw e;
        }
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("message", "Lỗi hệ thống: " + e.getMessage()));
    }
}
