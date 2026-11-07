package com.eventmanager.fe.gui.Admin.common;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Lớp cha của mọi màn hình Admin: nền xám nhạt, lề đều.
 * Mỗi màn hình tự viết taiLai() = gọi API lấy dữ liệu mới nhất từ DB.
 */
public abstract class TrangAdmin extends JPanel {

    protected final DieuHuong dieuHuong;

    protected TrangAdmin(DieuHuong dieuHuong) {
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

        void timTaiKhoan(String tuKhoa);

        void phanQuyenCho(long taiKhoanId);

        void capNhatChuong();

        String tenDangNhap();
    }
}
