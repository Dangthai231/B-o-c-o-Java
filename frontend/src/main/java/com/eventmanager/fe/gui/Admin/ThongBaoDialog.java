package com.eventmanager.fe.gui.Admin;
import javax.swing.*;
import com.eventmanager.fe.gui.Admin.common.UI;
/** Các trường của hộp thoại thông báo được thiết kế trong ThongBaoDialog.form. */
final class ThongBaoDialog {
    ThongBaoDialog() { $$$setupUI$$$(); }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    javax.swing.JPanel mainPanel;
    javax.swing.JTextField txtTieuDe;
    javax.swing.JComboBox<String> cboDoiTuongNhan;
    javax.swing.JTextArea txtNoiDung;
    javax.swing.JLabel loi;
    javax.swing.JButton huy;
    javax.swing.JButton luu;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        mainPanel.setBackground(new java.awt.Color(0xFFFFFF));
        javax.swing.JPanel panela341 = new javax.swing.JPanel();
        panela341.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela341.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(panela341, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela340 = new javax.swing.JLabel();
        panela340.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela340.setForeground(new java.awt.Color(0x243B53));
        panela340.setText("Tiêu đề *");
        panela341.add(panela340, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtTieuDe.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTieuDe.setToolTipText("Nhập tiêu đề thông báo");
        txtTieuDe.setPreferredSize(new java.awt.Dimension(180, 32));
        txtTieuDe.setMinimumSize(new java.awt.Dimension(50, 32));
        panela341.add(txtTieuDe, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela344 = new javax.swing.JPanel();
        panela344.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela344.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(panela344, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela343 = new javax.swing.JLabel();
        panela343.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela343.setForeground(new java.awt.Color(0x243B53));
        panela343.setText("Gửi tới *");
        panela344.add(panela343, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        cboDoiTuongNhan = new javax.swing.JComboBox();
        cboDoiTuongNhan.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboDoiTuongNhan.setToolTipText("Chọn nhóm người nhận thông báo");
        cboDoiTuongNhan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Tất cả người tham gia", "Sinh viên", "Ban tổ chức", "Cán bộ sự kiện", "Đại biểu", "Khách mời"}));
        cboDoiTuongNhan.setPreferredSize(new java.awt.Dimension(175, 32));
        cboDoiTuongNhan.setMinimumSize(new java.awt.Dimension(110, 32));
        panela344.add(cboDoiTuongNhan, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela348 = new javax.swing.JPanel();
        panela348.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela348.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(panela348, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela347 = new javax.swing.JLabel();
        panela347.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela347.setForeground(new java.awt.Color(0x243B53));
        panela347.setText("Nội dung *");
        panela348.add(panela347, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JScrollPane panela346 = new javax.swing.JScrollPane();
        panela348.add(panela346, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtNoiDung = new javax.swing.JTextArea();
        txtNoiDung.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtNoiDung.setRows(7);
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        panela346.setViewportView(txtNoiDung);
        loi = new javax.swing.JLabel();
        loi.setFont(new java.awt.Font("Segoe UI", 0, 12));
        loi.setForeground(new java.awt.Color(0xDC3545));
        loi.setText(" ");
        mainPanel.add(loi, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela352 = new javax.swing.JPanel();
        panela352.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela352.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(panela352, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        huy = new javax.swing.JButton();
        huy.setFont(new java.awt.Font("Segoe UI", 0, 13));
        huy.setBackground(new java.awt.Color(0xFFFFFF));
        huy.setForeground(new java.awt.Color(0x243B53));
        huy.setFocusPainted(false);
        huy.setPreferredSize(new java.awt.Dimension(130, 30));
        huy.setText("Hủy");
        panela352.add(huy, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, null, null, null));
        luu = new javax.swing.JButton();
        luu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        luu.setBackground(new java.awt.Color(0x1565C0));
        luu.setForeground(new java.awt.Color(0xFFFFFF));
        luu.setFocusPainted(false);
        luu.setContentAreaFilled(false);
        luu.setOpaque(true);
        luu.setBorderPainted(false);
        luu.setPreferredSize(new java.awt.Dimension(130, 30));
        luu.setText("Lưu thông báo");
        panela352.add(luu, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 4, 0, 0, 0, null, null, null));
    }

    private void createUIComponents() {
        txtTieuDe = UI.oNhap("Nhập tiêu đề thông báo");
    }
}
