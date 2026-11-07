package com.eventmanager.backend.admin.common;

import java.util.List;
import java.util.Map;

/**
 * Gom tất cả "giá trị cố định" của phần Admin vào một chỗ.
 * Nhờ vậy không phải gõ lại chuỗi "Chờ duyệt", "HOAT_DONG"... ở nhiều nơi (dễ gõ sai).
 */
public final class HangSo {

    private HangSo() {
    }

    // ---------- Vai trò hệ thống (khớp với Login đang dùng) ----------
    public static final String ADMIN = "ADMIN";
    public static final String CAN_BO = "CAN_BO";
    public static final String HOC_SINH = "HOC_SINH";
    public static final List<String> DS_VAI_TRO = List.of(ADMIN, CAN_BO, HOC_SINH);

    private static final Map<String, String> TEN_VAI_TRO = Map.of(
            ADMIN, "Quản trị viên",
            CAN_BO, "Cán bộ sự kiện",
            HOC_SINH, "Sinh viên");

    public static String tenVaiTro(String vaiTro) {
        return TEN_VAI_TRO.getOrDefault(vaiTro, vaiTro);
    }

    // ---------- Trạng thái tài khoản ----------
    public static final String TK_HOAT_DONG = "HOAT_DONG";
    public static final String TK_BI_KHOA = "BI_KHOA";

    // ---------- Trạng thái duyệt (dùng chung: yêu cầu thay đổi, kế hoạch sự kiện, thông báo) ----------
    public static final String CHO_DUYET = "Chờ duyệt";
    public static final String DA_DUYET = "Đã duyệt";
    public static final String TU_CHOI = "Từ chối";

    // ---------- Trạng thái thông báo ----------
    public static final String TB_DA_DANG = "Đã đăng";
    public static final String TB_DA_GO = "Đã gỡ";
    public static final String TB_DA_GUI = "Đã gửi";   // giá trị phần Cán bộ đang dùng -> Admin coi là "Chờ duyệt"
    public static final String TB_NHAP = "Nháp";

    // ---------- Trạng thái diễn ra của sự kiện (khớp với phần Sinh viên) ----------
    public static final String SK_SAP_DIEN_RA = "Sắp diễn ra";
    public static final String SK_DANG_DIEN_RA = "Đang diễn ra";
    public static final String SK_DA_KET_THUC = "Đã kết thúc";
    public static final String SK_DA_HUY = "Đã hủy";
    public static final List<String> DS_TRANG_THAI_SU_KIEN =
            List.of(SK_SAP_DIEN_RA, SK_DANG_DIEN_RA, SK_DA_KET_THUC, SK_DA_HUY);

    // ---------- Biểu thức kiểm tra định dạng ----------
    public static final String REGEX_EMAIL = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";
    public static final String REGEX_SDT = "^0\\d{9}$";
    public static final String REGEX_TEN_DANG_NHAP = "^[a-zA-Z0-9._]{3,30}$";
}
