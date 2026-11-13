package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.api.ApiClient;
import com.eventmanager.fe.gui.Admin.common.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** MÀN HÌNH "CÀI ĐẶT": đổi mật khẩu Quản trị viên + thông tin hệ thống. */
public class CaiDatPanel extends TrangAdmin {


    public CaiDatPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        $$$setupUI$$$();
        vungNoiDung.add(mainPanel, BorderLayout.CENTER);
        lblTaiKhoan.setText(dieuHuong.tenDangNhap());
        lblDiaChiApi.setText(ApiClient.BASE_URL);
        luu.addActionListener(e -> doiMatKhau());
    }









    private void doiMatKhau() {
        String cu = new String(txtMatKhauHienTai.getPassword());
        String moi = new String(txtMatKhauMoi.getPassword());
        if (cu.isEmpty()) { loi.setText("Vui lòng nhập mật khẩu hiện tại"); return; }
        if (moi.length() < 6) { loi.setText("Mật khẩu mới phải có ít nhất 6 ký tự"); return; }
        if (!moi.equals(new String(txtNhapLaiMatKhauMoi.getPassword()))) { loi.setText("Hai lần nhập mật khẩu mới không khớp"); return; }
        loi.setText(" ");
        luu.setEnabled(false);
        new SwingWorker<String, Void>() {
            protected String doInBackground() {
                try {
                    Api.put("/api/admin/doi-mat-khau", Api.body("tenDangNhap", dieuHuong.tenDangNhap(), "matKhauCu", cu, "matKhauMoi", moi));
                    return null;
                } catch (Exception ex) { return ex.getMessage(); }
            }
            protected void done() {
                luu.setEnabled(true);
                try {
                    String error = get();
                    if (error != null) { loi.setText(error); return; }
                    txtMatKhauHienTai.setText(""); txtMatKhauMoi.setText(""); txtNhapLaiMatKhauMoi.setText("");
                    HopThoai.thanhCong(CaiDatPanel.this, "Đổi mật khẩu thành công");
                } catch (Exception ex) { loi.setText(ex.getMessage()); }
            }
        }.execute();
    }

    @Override
    public void taiLai() {
        loi.setText(" ");
    }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPasswordField txtMatKhauHienTai;
    private javax.swing.JPasswordField txtMatKhauMoi;
    private javax.swing.JPasswordField txtNhapLaiMatKhauMoi;
    private javax.swing.JLabel loi;
    private javax.swing.JButton luu;
    private javax.swing.JLabel lblTaiKhoan;
    private javax.swing.JLabel lblDiaChiApi;

    private javax.swing.JPanel pnlTieuDe;
    private javax.swing.JPanel pnlNoiDung;

    private void $$$setupUI$$$() {
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xF5F7FA));
        pnlTieuDe = new javax.swing.JPanel();
        pnlTieuDe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlTieuDe.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlTieuDe, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        javax.swing.JLabel panela214 = new javax.swing.JLabel();
        panela214.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela214.setForeground(new java.awt.Color(0x0B2B50));
        panela214.setText("Cài đặt");
        pnlTieuDe.add(panela214, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela215 = new javax.swing.JLabel();
        panela215.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela215.setForeground(new java.awt.Color(0x627D98));
        panela215.setText("Bảo mật tài khoản và thông tin hệ thống");
        pnlTieuDe.add(panela215, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlNoiDung = new javax.swing.JPanel();
        pnlNoiDung.setLayout(new java.awt.BorderLayout(12, 16));
        pnlNoiDung.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlNoiDung, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        javax.swing.JPanel panela212 = new javax.swing.JPanel();
        panela212.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela212.setBackground(new java.awt.Color(0xF5F7FA));
        pnlNoiDung.add(panela212, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel panela203 = new javax.swing.JPanel();
        panela203.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(10, 1, new java.awt.Insets(20, 20, 20, 20), 12, 10, false, false));
        panela203.setBackground(new java.awt.Color(0xFFFFFF));
        panela212.add(panela203, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela193 = new javax.swing.JLabel();
        panela193.setFont(new java.awt.Font("Segoe UI", 1, 18));
        panela193.setForeground(new java.awt.Color(0x243B53));
        panela193.setText("Đổi mật khẩu");
        panela203.add(panela193, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela194 = new javax.swing.JLabel();
        panela194.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela194.setForeground(new java.awt.Color(0x243B53));
        panela194.setText("Mật khẩu có ít nhất 6 ký tự");
        panela203.add(panela194, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela195 = new javax.swing.JLabel();
        panela195.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela195.setForeground(new java.awt.Color(0x243B53));
        panela195.setText("Mật khẩu hiện tại");
        panela203.add(panela195, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtMatKhauHienTai = new javax.swing.JPasswordField();
        txtMatKhauHienTai.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtMatKhauHienTai.setToolTipText("Nhập mật khẩu đang sử dụng");
        txtMatKhauHienTai.setPreferredSize(new java.awt.Dimension(220, 32));
        panela203.add(txtMatKhauHienTai, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JLabel panela197 = new javax.swing.JLabel();
        panela197.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela197.setForeground(new java.awt.Color(0x243B53));
        panela197.setText("Mật khẩu mới");
        panela203.add(panela197, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtMatKhauMoi = new javax.swing.JPasswordField();
        txtMatKhauMoi.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtMatKhauMoi.setToolTipText("Mật khẩu mới có ít nhất 6 ký tự");
        txtMatKhauMoi.setPreferredSize(new java.awt.Dimension(220, 32));
        panela203.add(txtMatKhauMoi, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JLabel panela199 = new javax.swing.JLabel();
        panela199.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela199.setForeground(new java.awt.Color(0x243B53));
        panela199.setText("Nhập lại mật khẩu mới");
        panela203.add(panela199, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtNhapLaiMatKhauMoi = new javax.swing.JPasswordField();
        txtNhapLaiMatKhauMoi.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtNhapLaiMatKhauMoi.setToolTipText("Nhập lại mật khẩu mới");
        txtNhapLaiMatKhauMoi.setPreferredSize(new java.awt.Dimension(220, 32));
        panela203.add(txtNhapLaiMatKhauMoi, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        loi = new javax.swing.JLabel();
        loi.setFont(new java.awt.Font("Segoe UI", 0, 12));
        loi.setForeground(new java.awt.Color(0xDC3545));
        loi.setText(" ");
        panela203.add(loi, new com.intellij.uiDesigner.core.GridConstraints(8, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        luu = new javax.swing.JButton();
        luu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        luu.setBackground(new java.awt.Color(0x1565C0));
        luu.setForeground(new java.awt.Color(0xFFFFFF));
        luu.setFocusPainted(false);
        luu.setContentAreaFilled(false);
        luu.setOpaque(true);
        luu.setBorderPainted(false);
        luu.setPreferredSize(new java.awt.Dimension(130, 30));
        luu.setText("Cập nhật mật khẩu");
        panela203.add(luu, new com.intellij.uiDesigner.core.GridConstraints(9, 0, 1, 1, 4, 0, 0, 0, null, null, null));
        javax.swing.JPanel panela211 = new javax.swing.JPanel();
        panela211.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(7, 1, new java.awt.Insets(20, 20, 20, 20), 12, 10, false, false));
        panela211.setBackground(new java.awt.Color(0xFFFFFF));
        panela212.add(panela211, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela204 = new javax.swing.JLabel();
        panela204.setFont(new java.awt.Font("Segoe UI", 1, 18));
        panela204.setForeground(new java.awt.Color(0x243B53));
        panela204.setText("Thông tin hệ thống");
        panela211.add(panela204, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela205 = new javax.swing.JLabel();
        panela205.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela205.setForeground(new java.awt.Color(0x243B53));
        panela205.setText("Tài khoản đăng nhập");
        panela211.add(panela205, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTaiKhoan = new javax.swing.JLabel();
        lblTaiKhoan.setFont(new java.awt.Font("Segoe UI", 1, 13));
        lblTaiKhoan.setForeground(new java.awt.Color(0x243B53));
        lblTaiKhoan.setText("");
        panela211.add(lblTaiKhoan, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela207 = new javax.swing.JLabel();
        panela207.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela207.setForeground(new java.awt.Color(0x243B53));
        panela207.setText("Vai trò: Quản trị viên");
        panela211.add(panela207, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela208 = new javax.swing.JLabel();
        panela208.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela208.setForeground(new java.awt.Color(0x243B53));
        panela208.setText("Máy chủ API");
        panela211.add(panela208, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblDiaChiApi = new javax.swing.JLabel();
        lblDiaChiApi.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDiaChiApi.setForeground(new java.awt.Color(0x243B53));
        lblDiaChiApi.setText("");
        panela211.add(lblDiaChiApi, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela210 = new javax.swing.JLabel();
        panela210.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela210.setForeground(new java.awt.Color(0x243B53));
        panela210.setText("BCH Liên chi đoàn CNS – ĐH Điện lực");
        panela211.add(panela210, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, 0, 3, 7, 3, null, null, null));
    }
}
