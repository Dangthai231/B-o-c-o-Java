package com.eventmanager.backend.admin.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Các hàm nhỏ dùng chung: đọc dữ liệu từ body JSON, định dạng ngày giờ, kiểm tra rỗng.
 */
public final class DuLieu {

    public static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Định dạng FE gửi lên khi nhập ngày giờ: 15/11/2026 08:00
    private static final DateTimeFormatter NHAP_NGAY_GIO = DateTimeFormatter.ofPattern("d/M/yyyy H:mm");

    private DuLieu() {
    }

    /** Lấy chuỗi từ body (đã trim). Không có thì trả "". */
    public static String chuoi(Map<String, Object> body, String key) {
        if (body == null) return "";
        Object v = body.get(key);
        return v == null ? "" : v.toString().trim();
    }

    public static boolean rong(String s) {
        return s == null || s.isBlank();
    }

    /** null -> "" để FE không phải kiểm tra null. */
    public static String khongNull(String s) {
        return s == null ? "" : s;
    }

    public static String dinhDang(LocalDateTime t) {
        return t == null ? "" : t.format(NGAY_GIO);
    }

    public static String dinhDangNgay(LocalDateTime t) {
        return t == null ? "" : t.format(NGAY);
    }

    /** Đọc "15/11/2026 08:00" -> LocalDateTime. Sai định dạng thì báo lỗi rõ ràng. */
    public static LocalDateTime docNgayGio(String s, String tenTruong) {
        if (rong(s)) {
            throw new LoiNghiepVu(tenTruong + " không được để trống");
        }
        try {
            return LocalDateTime.parse(s.trim(), NHAP_NGAY_GIO);
        } catch (DateTimeParseException e) {
            throw new LoiNghiepVu(tenTruong + " phải có dạng dd/MM/yyyy HH:mm (VD: 15/11/2026 08:00)");
        }
    }

    public static int docSo(String s, String tenTruong) {
        try {
            return Integer.parseInt(s.trim());
        } catch (RuntimeException e) {
            throw new LoiNghiepVu(tenTruong + " phải là số nguyên");
        }
    }
}
