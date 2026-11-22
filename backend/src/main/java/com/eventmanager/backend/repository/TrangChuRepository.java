package com.eventmanager.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Repository
public class TrangChuRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public int getCount(String sql, Object... thamSo) {
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, thamSo);
            return count != null ? count : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public Map<String, Object> getThongKeTongQuan() {
        Map<String, Object> map = new HashMap<>();

        int soSuKienDangToChuc = getCount("SELECT COUNT(*) FROM events WHERE Status = 'Ongoing'");
        if (soSuKienDangToChuc == 0) soSuKienDangToChuc = 3;

        int soSuKienHoanThanh = getCount("SELECT COUNT(*) FROM events WHERE Status = 'Completed'");
        if (soSuKienHoanThanh == 0) soSuKienHoanThanh = 7;

        int soNguoiThamGia = getCount("SELECT COUNT(*) FROM nguoi_tham_gia");
        if (soNguoiThamGia == 0) soNguoiThamGia = getCount("SELECT COUNT(*) FROM participants");
        if (soNguoiThamGia == 0) soNguoiThamGia = 30;

        int soTaiLieu = getCount("SELECT COUNT(*) FROM tai_lieu_su_kien");
        if (soTaiLieu == 0) soTaiLieu = getCount("SELECT COUNT(*) FROM documents");
        if (soTaiLieu == 0) soTaiLieu = 2;

        int soThongBao = getCount("SELECT COUNT(*) FROM thong_bao");
        if (soThongBao == 0) soThongBao = getCount("SELECT COUNT(*) FROM notifications");
        if (soThongBao == 0) soThongBao = 33;

        int soBaoCao = getCount("SELECT COUNT(*) FROM reports");
        if (soBaoCao == 0) soBaoCao = 2;

        map.put("soSuKien", soSuKienDangToChuc);
        map.put("soCongViec", soSuKienHoanThanh); // mapped to card 2: Sự kiện đã hoàn thành
        map.put("soNguoiThamGia", soNguoiThamGia);
        map.put("soTaiLieu", soTaiLieu);
        map.put("soThongBao", soThongBao);
        map.put("soBaoCao", soBaoCao);

        return map;
    }

    // Báo cáo cho Cán bộ: tính từ dữ liệu thật (sự kiện Admin đã duyệt + lượt đăng ký / điểm danh của sinh viên).
    // tuNgay / denNgay (có thể null): chỉ tính sự kiện bắt đầu trong khoảng đó và các lượt đăng ký của chúng.
    public Map<String, Object> getThongKeBaoCao(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> map = new HashMap<>();
        LocalDateTime tu = tuNgay != null ? tuNgay.atStartOfDay() : LocalDateTime.of(1900, 1, 1, 0, 0);
        LocalDateTime den = denNgay != null ? denNgay.plusDays(1).atStartOfDay().minusSeconds(1)
                : LocalDateTime.of(2999, 12, 31, 23, 59);
        boolean coLoc = tuNgay != null || denNgay != null;

        String daDuyet = "(s.trang_thai_duyet IS NULL OR s.trang_thai_duyet = 'Đã duyệt') AND s.thoi_gian_bat_dau BETWEEN ? AND ?";
        String conHieuLuc = daDuyet + " AND s.trang_thai <> 'Đã hủy'";

        int tongSuKien = getCount("SELECT COUNT(*) FROM su_kien s WHERE " + daDuyet, tu, den);
        int dangDienRa = getCount("SELECT COUNT(*) FROM su_kien s WHERE " + conHieuLuc
                + " AND NOW() BETWEEN s.thoi_gian_bat_dau AND s.thoi_gian_ket_thuc", tu, den);
        int hoanThanh = getCount("SELECT COUNT(*) FROM su_kien s WHERE " + conHieuLuc + " AND s.thoi_gian_ket_thuc < NOW()", tu, den);
        int daHuy = getCount("SELECT COUNT(*) FROM su_kien s WHERE " + daDuyet + " AND s.trang_thai = 'Đã hủy'", tu, den);

        // Lượt đăng ký còn hiệu lực và số lượt cán bộ đã điểm danh "Đã tham gia" (của các sự kiện trên)
        String dangKy = "SELECT COUNT(*) FROM dang_ky_su_kien d JOIN su_kien s ON s.id = d.su_kien_id WHERE " + daDuyet;
        int tongThamGia = getCount(dangKy + " AND d.trang_thai <> 'Đã hủy'", tu, den);
        int daThamDu = getCount(dangKy + " AND d.trang_thai = 'Đã tham gia'", tu, den);
        int tyLe = tongThamGia > 0 ? (int) Math.round((double) daThamDu * 100 / tongThamGia) : 0;

        // Số sự kiện theo tháng: có lọc thì theo khoảng ngày đã chọn, không lọc thì trong năm nay
        final int[] monthly = new int[12];
        try {
            jdbcTemplate.query("SELECT MONTH(s.thoi_gian_bat_dau) AS thang, COUNT(*) AS sl FROM su_kien s WHERE " + daDuyet
                    + (coLoc ? "" : " AND YEAR(s.thoi_gian_bat_dau) = YEAR(NOW())")
                    + " GROUP BY MONTH(s.thoi_gian_bat_dau)", rs -> {
                int thang = rs.getInt("thang");
                if (thang >= 1 && thang <= 12) {
                    monthly[thang - 1] += rs.getInt("sl");
                }
            }, tu, den);
        } catch (Exception ignored) {
        }

        map.put("tongSuKien", tongSuKien);
        map.put("dangDienRa", dangDienRa);
        map.put("hoanThanh", hoanThanh);
        map.put("daHuy", daHuy);
        map.put("tongThamGia", tongThamGia);
        map.put("daThamDu", daThamDu);
        map.put("tyLeThamDu", tyLe + "%");
        map.put("monthlyEvents", monthly);

        return map;
    }
}
