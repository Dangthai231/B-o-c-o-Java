package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.Theme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Lớp cha của mọi màn hình Sinh viên: nền xám nhạt, lề đều (giống TrangAdmin của nhóm).
 * Mỗi màn hình tự viết taiLai() = gọi API lấy dữ liệu mới nhất từ DB.
 */
public abstract class TrangSinhVien extends JPanel {

    protected final DieuHuong dieuHuong;

    protected TrangSinhVien(DieuHuong dieuHuong) {
        super(new BorderLayout(0, 18));
        this.dieuHuong = dieuHuong;
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(22, 26, 22, 26));
    }

    /** Được gọi mỗi khi người dùng mở màn hình này. */
    public abstract void taiLai();

    /** Cho các màn hình gọi nhau / gọi khung chính. */
    public interface DieuHuong {
        void moTrang(String ma);

        /** Mở Tra cứu sự kiện với sẵn từ khóa (ô tìm kiếm trên thanh trên cùng). */
        void timSuKien(String tuKhoa);

        /** Mở trang Đánh giá, chọn sẵn sự kiện. */
        void danhGia(long suKienId);

        String tenDangNhap();
    }
}