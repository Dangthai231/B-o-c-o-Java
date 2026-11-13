package com.eventmanager.fe.gui.Admin.common;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Khung trang Admin và trạng thái của các tác vụ nền. */
public abstract class TrangAdmin extends JPanel {
    protected final DieuHuong dieuHuong;
    private int soTacVu;
    private String loiGanNhat;
    private String ketQua = "Đã tải dữ liệu";

    protected TrangAdmin(DieuHuong dieuHuong) {
        super(new BorderLayout());
        this.dieuHuong = dieuHuong;
        $$$setupUI$$$();
        setBackground(new Color(0xF5F7FA));
        setBorder(new EmptyBorder(18, 22, 14, 22));
        add(khungTrang, BorderLayout.CENTER);
    }

    public void batDauTai() {
        if (soTacVu++ == 0) {
            loiGanNhat = null;
            ketQua = "Đã tải dữ liệu";
        }
        lblTrangThai.setForeground(Theme.MUTED);
        lblTrangThai.setText("Đang tải dữ liệu…");
    }

    public void ketThucTai(String loi) {
        if (loi != null) loiGanNhat = loi;
        soTacVu = Math.max(0, soTacVu - 1);
        if (loiGanNhat != null) {
            lblTrangThai.setForeground(Theme.DANGER);
            lblTrangThai.setText(loiGanNhat);
            lblTrangThai.setToolTipText(loiGanNhat);
        } else if (soTacVu == 0) {
            lblTrangThai.setForeground(Theme.MUTED);
            lblTrangThai.setText(ketQua);
            lblTrangThai.setToolTipText(null);
        }
    }

    protected void hienSoKetQua(int so) {
        ketQua = so == 0 ? "Không có dữ liệu hoặc không tìm thấy kết quả" : "Hiển thị " + so + " kết quả";
        if (soTacVu == 0) lblTrangThai.setText(ketQua);
    }

    public abstract void taiLai();
    public interface DieuHuong {
        void moTrang(String ma);
        void timTaiKhoan(String tuKhoa);
        void phanQuyenCho(long taiKhoanId);
        void capNhatChuong();
        String tenDangNhap();
    }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel khungTrang;
    protected javax.swing.JPanel vungNoiDung;
    private javax.swing.JLabel lblTrangThai;

    private void $$$setupUI$$$() {
        khungTrang = new javax.swing.JPanel();
        khungTrang.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        khungTrang.setBackground(new java.awt.Color(0xF5F7FA));
        vungNoiDung = new javax.swing.JPanel();
        vungNoiDung.setLayout(new java.awt.BorderLayout(12, 16));
        vungNoiDung.setBackground(new java.awt.Color(0xF5F7FA));
        khungTrang.add(vungNoiDung, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        lblTrangThai = new javax.swing.JLabel();
        lblTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblTrangThai.setForeground(new java.awt.Color(0x627D98));
        lblTrangThai.setText("Sẵn sàng");
        khungTrang.add(lblTrangThai, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 0, null, null, null));
    }
}
