package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * MÀN HÌNH "TỔNG QUAN": nhìn nhanh số liệu + các việc đang chờ Admin xử lý.
 */
public class TongQuanPanel extends TrangAdmin {



    public TongQuanPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        $$$setupUI$$$();
        lblBieuTuongTongTaiKhoan.setIcon(Icons.of(Icons.USERS, 26, new Color(0x1565C0)));
        lblBieuTuongTongSuKien.setIcon(Icons.of(Icons.CALENDAR, 26, new Color(0x7952B3)));
        lblBieuTuongSuKienSapDienRa.setIcon(Icons.of(Icons.CLOCK, 26, new Color(0x159B64)));
        lblBieuTuongThongBaoDaDang.setIcon(Icons.of(Icons.BELL, 26, new Color(0xD79318)));
        vungNoiDung.add(mainPanel, BorderLayout.CENTER);
        btnXemTatCa.addActionListener(e -> dieuHuong.moTrang(DashboardAdmin.SU_KIEN));
        bangSuKienSapToi.anCot(0);
        bangSuKienSapToi.cotHaiDong(1);
        bangSuKienSapToi.doRongCot(1, 300);
        bangSuKienSapToi.doRongCot(2, 120);
        bangSuKienSapToi.doRongCot(3, 150);
        bangSuKienSapToi.getColumnModel().getColumn(3).setCellRenderer((t, v, s, f, r, c) -> {
            String[] p = (v == null ? "0/1" : v.toString()).split("/");
            double daDk = Double.parseDouble(p[0]), toiDa = Math.max(1, Double.parseDouble(p[1]));
            JPanel o = new JPanel(new BorderLayout(0, 4));
            o.setBorder(new EmptyBorder(8, 14, 8, 14));
            o.setBackground(bangSuKienSapToi.nenDong(r, s));
            o.add(UI.nhan((int) daDk + " / " + (int) toiDa, Theme.SMALL_BOLD, Theme.TEXT_2), BorderLayout.NORTH);
            o.add(UI.thanhTienDo(daDk / toiDa, Theme.PRIMARY), BorderLayout.CENTER);
            return o;
        });

    }

    /** 1 dòng "việc cần làm": icon + tên + số lượng, bấm để chuyển tới màn hình xử lý. */
    private static class ViecCanLam extends UI.RoundPanel {
        private final JLabel so = UI.nhan("—", Theme.font(Font.BOLD, 20), Theme.TEXT);

        ViecCanLam(String ten, String icon, Color mau, Color nen, Runnable khiBam) {
            super(new BorderLayout(12, 0), 12, Theme.TABLE_HEAD, Theme.BORDER);
            setBorder(new EmptyBorder(14, 14, 14, 14));
            JComponent tron = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = Theme.muot(g);
                    g2.setColor(nen);
                    g2.fillRoundRect(0, 0, 40, 40, 12, 12);
                    g2.dispose();
                    Icons.of(icon, 20, mau).paintIcon(this, g, 10, 10);
                }
            };
            tron.setPreferredSize(new Dimension(40, 40));
            add(tron, BorderLayout.WEST);
            JPanel chu = new JPanel(new GridLayout(2, 1));
            chu.setOpaque(false);
            chu.add(UI.nhan(ten, Theme.BODY_BOLD, Theme.TEXT));
            chu.add(UI.nhan("Bấm để xử lý", Theme.SMALL, Theme.MUTED));
            add(chu, BorderLayout.CENTER);
            add(so, BorderLayout.EAST);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    khiBam.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setNen(Theme.ROW_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setNen(Theme.TABLE_HEAD);
                }
            });
        }

        void setSo(long n) {
            so.setText(String.valueOf(n));
            so.setForeground(n > 0 ? Theme.DANGER : Theme.MUTED);
        }
    }

    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/tong-quan"), kq -> {
            JsonObject o = Api.obj(kq);
            lblTongTaiKhoan.setText(o.has("tongTaiKhoan") && !o.get("tongTaiKhoan").isJsonNull() ? o.get("tongTaiKhoan").getAsString() : "—");
            lblTongSuKien.setText(o.has("tongSuKien") && !o.get("tongSuKien").isJsonNull() ? o.get("tongSuKien").getAsString() : "—");
            lblSuKienSapDienRa.setText(o.has("suKienSapDienRa") && !o.get("suKienSapDienRa").isJsonNull() ? o.get("suKienSapDienRa").getAsString() : "—");
            lblThongBaoDaDang.setText(o.has("thongBaoDaDang") && !o.get("thongBaoDaDang").isJsonNull() ? o.get("thongBaoDaDang").getAsString() : "—");
            viecDuyetKeHoach.setSo(Api.n(o, "keHoachChoDuyet"));
            viecDuyetThongBao.setSo(Api.n(o, "thongBaoChoDuyet"));
            viecDuyetThongTin.setSo(Api.n(o, "yeuCauChoDuyet"));
            long tong = Api.n(o, "tongViecCanXuLy");
            lblTongViecCanXuLy.setText(tong == 0 ? "Không có việc nào đang chờ" : tong + " việc đang chờ bạn xử lý");
            bangSuKienSapToi.xoaHet();
            for (JsonElement el : Api.arr(o, "suKienSapToi")) {
                JsonObject s = el.getAsJsonObject();
                bangSuKienSapToi.themDong(Api.n(s, "id"), Api.s(s, "tenSuKien") + "\n" + Api.s(s, "diaDiem"),
                        Api.s(s, "thoiGianBatDau"), Api.n(s, "soDangKy") + "/" + Api.n(s, "soLuongToiDa"));
            }
        });
    }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel lblTongTaiKhoan;
    private javax.swing.JLabel lblTongSuKien;
    private javax.swing.JLabel lblSuKienSapDienRa;
    private javax.swing.JLabel lblThongBaoDaDang;
    private javax.swing.JButton btnXemTatCa;
    private com.eventmanager.fe.gui.Admin.common.BangDep bangSuKienSapToi;
    private javax.swing.JLabel lblTongViecCanXuLy;
    private com.eventmanager.fe.gui.Admin.TongQuanPanel.ViecCanLam viecDuyetKeHoach;
    private com.eventmanager.fe.gui.Admin.TongQuanPanel.ViecCanLam viecDuyetThongBao;
    private com.eventmanager.fe.gui.Admin.TongQuanPanel.ViecCanLam viecDuyetThongTin;

    private javax.swing.JPanel pnlTieuDe;
    private javax.swing.JPanel pnlNoiDung;
    private javax.swing.JPanel pnlThongKe;
    private javax.swing.JPanel pnlQuanLy;

    private JLabel lblBieuTuongTongTaiKhoan;
    private JLabel lblBieuTuongTongSuKien;
    private JLabel lblBieuTuongSuKienSapDienRa;
    private JLabel lblBieuTuongThongBaoDaDang;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xF5F7FA));
        pnlTieuDe = new javax.swing.JPanel();
        pnlTieuDe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlTieuDe.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlTieuDe, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        javax.swing.JLabel panela148 = new javax.swing.JLabel();
        panela148.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela148.setForeground(new java.awt.Color(0x0B2B50));
        panela148.setText("Tổng quan");
        pnlTieuDe.add(panela148, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela149 = new javax.swing.JLabel();
        panela149.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela149.setForeground(new java.awt.Color(0x627D98));
        panela149.setText("Tình hình hoạt động của Liên chi đoàn");
        pnlTieuDe.add(panela149, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlNoiDung = new javax.swing.JPanel();
        pnlNoiDung.setLayout(new java.awt.BorderLayout(12, 16));
        pnlNoiDung.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlNoiDung, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        pnlThongKe = new javax.swing.JPanel();
        pnlThongKe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new java.awt.Insets(0, 0, 0, 0), 12, 10, true, false));
        pnlThongKe.setBackground(new java.awt.Color(0xF5F7FA));
        pnlNoiDung.add(pnlThongKe, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel panela131 = new javax.swing.JPanel();
        panela131.setLayout(new java.awt.BorderLayout(12, 16));
        panela131.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela131.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela131, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongTongTaiKhoan = new javax.swing.JLabel();
        lblBieuTuongTongTaiKhoan.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongTongTaiKhoan.setForeground(new java.awt.Color(0x1565C0));
        lblBieuTuongTongTaiKhoan.setText("");
        lblBieuTuongTongTaiKhoan.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongTongTaiKhoan.setHorizontalAlignment(0);
        panela131.add(lblBieuTuongTongTaiKhoan, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela130 = new javax.swing.JPanel();
        panela130.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela130.setBackground(new java.awt.Color(0xFFFFFF));
        panela131.add(panela130, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela128 = new javax.swing.JLabel();
        panela128.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela128.setForeground(new java.awt.Color(0x627D98));
        panela128.setText("Tổng tài khoản");
        panela130.add(panela128, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTongTaiKhoan = new javax.swing.JLabel();
        lblTongTaiKhoan.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTongTaiKhoan.setForeground(new java.awt.Color(0x1565C0));
        lblTongTaiKhoan.setText("—");
        panela130.add(lblTongTaiKhoan, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela136 = new javax.swing.JPanel();
        panela136.setLayout(new java.awt.BorderLayout(12, 16));
        panela136.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela136.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela136, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongTongSuKien = new javax.swing.JLabel();
        lblBieuTuongTongSuKien.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongTongSuKien.setForeground(new java.awt.Color(0x7952B3));
        lblBieuTuongTongSuKien.setText("");
        lblBieuTuongTongSuKien.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongTongSuKien.setHorizontalAlignment(0);
        panela136.add(lblBieuTuongTongSuKien, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela135 = new javax.swing.JPanel();
        panela135.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela135.setBackground(new java.awt.Color(0xFFFFFF));
        panela136.add(panela135, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela133 = new javax.swing.JLabel();
        panela133.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela133.setForeground(new java.awt.Color(0x627D98));
        panela133.setText("Tổng sự kiện");
        panela135.add(panela133, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTongSuKien = new javax.swing.JLabel();
        lblTongSuKien.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTongSuKien.setForeground(new java.awt.Color(0x7952B3));
        lblTongSuKien.setText("—");
        panela135.add(lblTongSuKien, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela141 = new javax.swing.JPanel();
        panela141.setLayout(new java.awt.BorderLayout(12, 16));
        panela141.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela141.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela141, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongSuKienSapDienRa = new javax.swing.JLabel();
        lblBieuTuongSuKienSapDienRa.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongSuKienSapDienRa.setForeground(new java.awt.Color(0x159B64));
        lblBieuTuongSuKienSapDienRa.setText("");
        lblBieuTuongSuKienSapDienRa.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongSuKienSapDienRa.setHorizontalAlignment(0);
        panela141.add(lblBieuTuongSuKienSapDienRa, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela140 = new javax.swing.JPanel();
        panela140.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela140.setBackground(new java.awt.Color(0xFFFFFF));
        panela141.add(panela140, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela138 = new javax.swing.JLabel();
        panela138.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela138.setForeground(new java.awt.Color(0x627D98));
        panela138.setText("Sắp diễn ra");
        panela140.add(panela138, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblSuKienSapDienRa = new javax.swing.JLabel();
        lblSuKienSapDienRa.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblSuKienSapDienRa.setForeground(new java.awt.Color(0x159B64));
        lblSuKienSapDienRa.setText("—");
        panela140.add(lblSuKienSapDienRa, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela146 = new javax.swing.JPanel();
        panela146.setLayout(new java.awt.BorderLayout(12, 16));
        panela146.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela146.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela146, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongThongBaoDaDang = new javax.swing.JLabel();
        lblBieuTuongThongBaoDaDang.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongThongBaoDaDang.setForeground(new java.awt.Color(0xD79318));
        lblBieuTuongThongBaoDaDang.setText("");
        lblBieuTuongThongBaoDaDang.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongThongBaoDaDang.setHorizontalAlignment(0);
        panela146.add(lblBieuTuongThongBaoDaDang, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela145 = new javax.swing.JPanel();
        panela145.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela145.setBackground(new java.awt.Color(0xFFFFFF));
        panela146.add(panela145, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela143 = new javax.swing.JLabel();
        panela143.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela143.setForeground(new java.awt.Color(0x627D98));
        panela143.setText("Thông báo đã đăng");
        panela145.add(panela143, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblThongBaoDaDang = new javax.swing.JLabel();
        lblThongBaoDaDang.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblThongBaoDaDang.setForeground(new java.awt.Color(0xD79318));
        lblThongBaoDaDang.setText("—");
        panela145.add(lblThongBaoDaDang, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlQuanLy = new javax.swing.JPanel();
        pnlQuanLy.setLayout(new java.awt.BorderLayout(12, 16));
        pnlQuanLy.setBackground(new java.awt.Color(0xF5F7FA));
        pnlNoiDung.add(pnlQuanLy, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela120 = new javax.swing.JPanel();
        panela120.setLayout(new java.awt.BorderLayout(12, 16));
        panela120.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panela120.setBackground(new java.awt.Color(0xFFFFFF));
        pnlQuanLy.add(panela120, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela117 = new javax.swing.JPanel();
        panela117.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela117.setBackground(new java.awt.Color(0xFFFFFF));
        panela120.add(panela117, java.awt.BorderLayout.NORTH);
        javax.swing.JLabel panela115 = new javax.swing.JLabel();
        panela115.setFont(new java.awt.Font("Segoe UI", 1, 17));
        panela115.setForeground(new java.awt.Color(0x243B53));
        panela115.setText("Sự kiện sắp diễn ra");
        panela117.add(panela115, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        btnXemTatCa = new javax.swing.JButton();
        btnXemTatCa.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnXemTatCa.setBackground(new java.awt.Color(0xFFFFFF));
        btnXemTatCa.setForeground(new java.awt.Color(0x243B53));
        btnXemTatCa.setFocusPainted(false);
        btnXemTatCa.setPreferredSize(new java.awt.Dimension(130, 30));
        btnXemTatCa.setText("Xem tất cả");
        panela117.add(btnXemTatCa, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 4, 0, 0, 0, null, null, null));
        javax.swing.JScrollPane panela119 = new javax.swing.JScrollPane();
        panela120.add(panela119, java.awt.BorderLayout.CENTER);
        panela119.setViewportView(bangSuKienSapToi);
        javax.swing.JPanel panela125 = new javax.swing.JPanel();
        panela125.setLayout(new java.awt.BorderLayout(12, 16));
        panela125.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panela125.setBackground(new java.awt.Color(0xFFFFFF));
        panela125.setPreferredSize(new java.awt.Dimension(320, 300));
        pnlQuanLy.add(panela125, java.awt.BorderLayout.EAST);
        javax.swing.JPanel panela123 = new javax.swing.JPanel();
        panela123.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela123.setBackground(new java.awt.Color(0xFFFFFF));
        panela125.add(panela123, java.awt.BorderLayout.NORTH);
        javax.swing.JLabel panela121 = new javax.swing.JLabel();
        panela121.setFont(new java.awt.Font("Segoe UI", 1, 17));
        panela121.setForeground(new java.awt.Color(0x243B53));
        panela121.setText("Việc cần xử lý");
        panela123.add(panela121, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTongViecCanXuLy = new javax.swing.JLabel();
        lblTongViecCanXuLy.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblTongViecCanXuLy.setForeground(new java.awt.Color(0x243B53));
        lblTongViecCanXuLy.setText(" ");
        panela123.add(lblTongViecCanXuLy, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela124 = new javax.swing.JPanel();
        panela124.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela124.setBackground(new java.awt.Color(0xFFFFFF));
        panela125.add(panela124, java.awt.BorderLayout.CENTER);
        panela124.add(viecDuyetKeHoach, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        panela124.add(viecDuyetThongBao, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        panela124.add(viecDuyetThongTin, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 3, null, null, null));
    }

    private void createUIComponents() {
        bangSuKienSapToi = new BangDep("id", "Sự kiện", "Bắt đầu", "Đăng ký");
        viecDuyetKeHoach = new ViecCanLam("Kế hoạch sự kiện chờ duyệt", Icons.FILE, Theme.WARNING, Theme.WARNING_SOFT, () -> dieuHuong.moTrang(DashboardAdmin.SU_KIEN));
        viecDuyetThongBao = new ViecCanLam("Thông báo chờ duyệt", Icons.BELL, Theme.PRIMARY, Theme.PRIMARY_SOFT, () -> dieuHuong.moTrang(DashboardAdmin.THONG_BAO));
        viecDuyetThongTin = new ViecCanLam("Yêu cầu đổi thông tin", Icons.USER, Theme.PURPLE, Theme.PURPLE_SOFT, () -> dieuHuong.moTrang(DashboardAdmin.TAI_KHOAN));
    }
}
