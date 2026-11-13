package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MÀN HÌNH "BÁO CÁO": số liệu tổng hợp + 2 biểu đồ cột tự vẽ (không cần thư viện ngoài).
 */
public class BaoCaoPanel extends TrangAdmin {


    public BaoCaoPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        $$$setupUI$$$();
        lblBieuTuongTongDangKy.setIcon(Icons.of(Icons.USERS, 26, new Color(0x1565C0)));
        lblBieuTuongSoNguoiThamGia.setIcon(Icons.of(Icons.CHECK_CIRCLE, 26, new Color(0x159B64)));
        lblBieuTuongSoNguoiVangMat.setIcon(Icons.of(Icons.X, 26, new Color(0xDC3545)));
        lblBieuTuongDiemDanhGiaTrungBinh.setIcon(Icons.of(Icons.CHART, 26, new Color(0xD79318)));
        vungNoiDung.add(mainPanel, BorderLayout.CENTER);
        btnLamMoi.addActionListener(e -> taiLai());
    }



    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/bao-cao"), kq -> {
            JsonObject o = Api.obj(kq);
            lblTongDangKy.setText(o.has("tongDangKy") && !o.get("tongDangKy").isJsonNull() ? o.get("tongDangKy").getAsString() : "—");
            lblSoNguoiThamGia.setText(o.has("soThamGia") && !o.get("soThamGia").isJsonNull() ? o.get("soThamGia").getAsString() : "—");
            lblSoNguoiVangMat.setText(o.has("soVangMat") && !o.get("soVangMat").isJsonNull() ? o.get("soVangMat").getAsString() : "—");
            double diem = Api.d(o, "diemDanhGiaTb");
            lblDiemDanhGiaTrungBinh.setText(diem == 0 ? "–" : diem + " / 5");
            bieuDoTheoThang.datDuLieu(Api.arr(o, "suKienTheoThang"));
            bieuDoDangKyNhieuNhat.datDuLieu(Api.arr(o, "topDangKy"));
            JsonObject vt = Api.obj(o, "taiKhoanTheoVaiTro");
            List<String> s = new ArrayList<>();
            vt.entrySet().forEach(e -> s.add(e.getKey() + ": " + e.getValue().getAsLong()));
            lblThongKeTheoVaiTro.setText("Tài khoản theo vai trò  -  " + String.join("   ", s));
        });
    }



    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JLabel lblTongDangKy;
    private javax.swing.JLabel lblSoNguoiThamGia;
    private javax.swing.JLabel lblSoNguoiVangMat;
    private javax.swing.JLabel lblDiemDanhGiaTrungBinh;
    private BieuDoCot bieuDoTheoThang;
    private BieuDoCot bieuDoDangKyNhieuNhat;
    private javax.swing.JLabel lblThongKeTheoVaiTro;

    private javax.swing.JPanel pnlTieuDe;
    private javax.swing.JPanel pnlNoiDung;
    private javax.swing.JPanel pnlThongKe;
    private javax.swing.JPanel pnlQuanLy;

    private JLabel lblBieuTuongTongDangKy;
    private JLabel lblBieuTuongSoNguoiThamGia;
    private JLabel lblBieuTuongSoNguoiVangMat;
    private JLabel lblBieuTuongDiemDanhGiaTrungBinh;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xF5F7FA));
        pnlTieuDe = new javax.swing.JPanel();
        pnlTieuDe.setLayout(new java.awt.BorderLayout(12, 16));
        pnlTieuDe.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlTieuDe, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        javax.swing.JPanel panela189 = new javax.swing.JPanel();
        panela189.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela189.setBackground(new java.awt.Color(0xF5F7FA));
        pnlTieuDe.add(panela189, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela187 = new javax.swing.JLabel();
        panela187.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela187.setForeground(new java.awt.Color(0x0B2B50));
        panela187.setText("Báo cáo");
        panela189.add(panela187, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela188 = new javax.swing.JLabel();
        panela188.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela188.setForeground(new java.awt.Color(0x627D98));
        panela188.setText("Thống kê tình hình tổ chức và tham gia sự kiện");
        panela189.add(panela188, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        btnLamMoi = new javax.swing.JButton();
        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnLamMoi.setBackground(new java.awt.Color(0xFFFFFF));
        btnLamMoi.setForeground(new java.awt.Color(0x243B53));
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setPreferredSize(new java.awt.Dimension(130, 36));
        btnLamMoi.setText("Làm mới");
        pnlTieuDe.add(btnLamMoi, java.awt.BorderLayout.EAST);
        pnlNoiDung = new javax.swing.JPanel();
        pnlNoiDung.setLayout(new java.awt.BorderLayout(12, 16));
        pnlNoiDung.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlNoiDung, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        pnlThongKe = new javax.swing.JPanel();
        pnlThongKe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new java.awt.Insets(0, 0, 0, 0), 12, 10, true, false));
        pnlThongKe.setBackground(new java.awt.Color(0xF5F7FA));
        pnlNoiDung.add(pnlThongKe, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel panela169 = new javax.swing.JPanel();
        panela169.setLayout(new java.awt.BorderLayout(12, 16));
        panela169.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela169.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela169, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongTongDangKy = new javax.swing.JLabel();
        lblBieuTuongTongDangKy.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongTongDangKy.setForeground(new java.awt.Color(0x1565C0));
        lblBieuTuongTongDangKy.setText("");
        lblBieuTuongTongDangKy.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongTongDangKy.setHorizontalAlignment(0);
        panela169.add(lblBieuTuongTongDangKy, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela168 = new javax.swing.JPanel();
        panela168.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela168.setBackground(new java.awt.Color(0xFFFFFF));
        panela169.add(panela168, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela166 = new javax.swing.JLabel();
        panela166.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela166.setForeground(new java.awt.Color(0x627D98));
        panela166.setText("Lượt đăng ký");
        panela168.add(panela166, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTongDangKy = new javax.swing.JLabel();
        lblTongDangKy.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTongDangKy.setForeground(new java.awt.Color(0x1565C0));
        lblTongDangKy.setText("—");
        panela168.add(lblTongDangKy, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela174 = new javax.swing.JPanel();
        panela174.setLayout(new java.awt.BorderLayout(12, 16));
        panela174.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela174.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela174, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongSoNguoiThamGia = new javax.swing.JLabel();
        lblBieuTuongSoNguoiThamGia.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongSoNguoiThamGia.setForeground(new java.awt.Color(0x159B64));
        lblBieuTuongSoNguoiThamGia.setText("");
        lblBieuTuongSoNguoiThamGia.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongSoNguoiThamGia.setHorizontalAlignment(0);
        panela174.add(lblBieuTuongSoNguoiThamGia, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela173 = new javax.swing.JPanel();
        panela173.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela173.setBackground(new java.awt.Color(0xFFFFFF));
        panela174.add(panela173, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela171 = new javax.swing.JLabel();
        panela171.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela171.setForeground(new java.awt.Color(0x627D98));
        panela171.setText("Đã tham gia");
        panela173.add(panela171, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblSoNguoiThamGia = new javax.swing.JLabel();
        lblSoNguoiThamGia.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblSoNguoiThamGia.setForeground(new java.awt.Color(0x159B64));
        lblSoNguoiThamGia.setText("—");
        panela173.add(lblSoNguoiThamGia, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela179 = new javax.swing.JPanel();
        panela179.setLayout(new java.awt.BorderLayout(12, 16));
        panela179.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela179.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela179, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongSoNguoiVangMat = new javax.swing.JLabel();
        lblBieuTuongSoNguoiVangMat.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongSoNguoiVangMat.setForeground(new java.awt.Color(0xDC3545));
        lblBieuTuongSoNguoiVangMat.setText("");
        lblBieuTuongSoNguoiVangMat.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongSoNguoiVangMat.setHorizontalAlignment(0);
        panela179.add(lblBieuTuongSoNguoiVangMat, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela178 = new javax.swing.JPanel();
        panela178.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela178.setBackground(new java.awt.Color(0xFFFFFF));
        panela179.add(panela178, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela176 = new javax.swing.JLabel();
        panela176.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela176.setForeground(new java.awt.Color(0x627D98));
        panela176.setText("Vắng mặt");
        panela178.add(panela176, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblSoNguoiVangMat = new javax.swing.JLabel();
        lblSoNguoiVangMat.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblSoNguoiVangMat.setForeground(new java.awt.Color(0xDC3545));
        lblSoNguoiVangMat.setText("—");
        panela178.add(lblSoNguoiVangMat, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela184 = new javax.swing.JPanel();
        panela184.setLayout(new java.awt.BorderLayout(12, 16));
        panela184.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela184.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela184, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongDiemDanhGiaTrungBinh = new javax.swing.JLabel();
        lblBieuTuongDiemDanhGiaTrungBinh.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongDiemDanhGiaTrungBinh.setForeground(new java.awt.Color(0xD79318));
        lblBieuTuongDiemDanhGiaTrungBinh.setText("");
        lblBieuTuongDiemDanhGiaTrungBinh.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongDiemDanhGiaTrungBinh.setHorizontalAlignment(0);
        panela184.add(lblBieuTuongDiemDanhGiaTrungBinh, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela183 = new javax.swing.JPanel();
        panela183.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela183.setBackground(new java.awt.Color(0xFFFFFF));
        panela184.add(panela183, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela181 = new javax.swing.JLabel();
        panela181.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela181.setForeground(new java.awt.Color(0x627D98));
        panela181.setText("Điểm đánh giá TB");
        panela183.add(panela181, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblDiemDanhGiaTrungBinh = new javax.swing.JLabel();
        lblDiemDanhGiaTrungBinh.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblDiemDanhGiaTrungBinh.setForeground(new java.awt.Color(0xD79318));
        lblDiemDanhGiaTrungBinh.setText("—");
        panela183.add(lblDiemDanhGiaTrungBinh, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlQuanLy = new javax.swing.JPanel();
        pnlQuanLy.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlQuanLy.setBackground(new java.awt.Color(0xF5F7FA));
        pnlNoiDung.add(pnlQuanLy, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela157 = new javax.swing.JPanel();
        panela157.setLayout(new java.awt.BorderLayout(12, 16));
        panela157.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panela157.setBackground(new java.awt.Color(0xFFFFFF));
        pnlQuanLy.add(panela157, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela155 = new javax.swing.JPanel();
        panela155.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela155.setBackground(new java.awt.Color(0xFFFFFF));
        panela157.add(panela155, java.awt.BorderLayout.NORTH);
        javax.swing.JLabel panela153 = new javax.swing.JLabel();
        panela153.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela153.setForeground(new java.awt.Color(0x0B2B50));
        panela153.setText("Số sự kiện theo tháng");
        panela155.add(panela153, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela154 = new javax.swing.JLabel();
        panela154.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela154.setForeground(new java.awt.Color(0x627D98));
        panela154.setText("6 tháng gần nhất, tính theo ngày bắt đầu");
        panela155.add(panela154, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        panela157.add(bieuDoTheoThang, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela163 = new javax.swing.JPanel();
        panela163.setLayout(new java.awt.BorderLayout(12, 16));
        panela163.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 18, 18, 18));
        panela163.setBackground(new java.awt.Color(0xFFFFFF));
        pnlQuanLy.add(panela163, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela160 = new javax.swing.JPanel();
        panela160.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela160.setBackground(new java.awt.Color(0xFFFFFF));
        panela163.add(panela160, java.awt.BorderLayout.NORTH);
        javax.swing.JLabel panela158 = new javax.swing.JLabel();
        panela158.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela158.setForeground(new java.awt.Color(0x0B2B50));
        panela158.setText("Sự kiện nhiều lượt đăng ký nhất");
        panela160.add(panela158, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela159 = new javax.swing.JLabel();
        panela159.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela159.setForeground(new java.awt.Color(0x627D98));
        panela159.setText("Top 5, so với số chỗ tối đa");
        panela160.add(panela159, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        panela163.add(bieuDoDangKyNhieuNhat, java.awt.BorderLayout.CENTER);
        lblThongKeTheoVaiTro = new javax.swing.JLabel();
        lblThongKeTheoVaiTro.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblThongKeTheoVaiTro.setForeground(new java.awt.Color(0x243B53));
        lblThongKeTheoVaiTro.setText(" ");
        panela163.add(lblThongKeTheoVaiTro, java.awt.BorderLayout.SOUTH);
    }

    private void createUIComponents() {
        bieuDoTheoThang = new BieuDoCot(false);
        bieuDoDangKyNhieuNhat = new BieuDoCot(true);
    }
}

/** Biểu đồ cột. ngang = true: cột nằm ngang (dùng cho tên dài), có vạch số chỗ tối đa. */
class BieuDoCot extends JComponent {
    private final boolean ngang;
    private final List<String> nhan = new ArrayList<>();
    private final List<Long> giaTri = new ArrayList<>();
    private final List<Long> toiDa = new ArrayList<>();

    BieuDoCot(boolean ngang) {
        this.ngang = ngang;
        setPreferredSize(new Dimension(300, 240));
    }

    void datDuLieu(com.google.gson.JsonArray ds) {
        nhan.clear();
        giaTri.clear();
        toiDa.clear();
        for (JsonElement el : ds) {
            JsonObject o = el.getAsJsonObject();
            nhan.add(Api.s(o, "nhan"));
            giaTri.add(Api.n(o, "giaTri"));
            toiDa.add(Api.n(o, "toiDa"));
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = Theme.muot(g);
        int w = getWidth(), h = getHeight();
        if (nhan.isEmpty()) {
            g2.setColor(Theme.MUTED);
            g2.setFont(Theme.BODY);
            g2.drawString("Chưa có dữ liệu", w / 2 - 45, h / 2);
            g2.dispose();
            return;
        }
        long max = 1;
        for (int i = 0; i < giaTri.size(); i++) max = Math.max(max, Math.max(giaTri.get(i), ngang ? toiDa.get(i) : 0));
        g2.setFont(Theme.SMALL);
        FontMetrics fm = g2.getFontMetrics();
        if (ngang) {
            int hangCao = Math.min(44, h / nhan.size());
            for (int i = 0; i < nhan.size(); i++) {
                int y = i * hangCao;
                String ten = nhan.get(i);
                while (fm.stringWidth(ten) > w - 70 && ten.length() > 3) ten = ten.substring(0, ten.length() - 4) + "...";
                g2.setColor(Theme.TEXT_2);
                g2.drawString(ten, 0, y + fm.getAscent());
                int yb = y + fm.getHeight() + 3;
                int rongMax = w - 60;
                g2.setColor(Theme.GRAY_SOFT);
                g2.fillRoundRect(0, yb, rongMax, 10, 10, 10);
                g2.setColor(Theme.PRIMARY);
                int rong = (int) (rongMax * giaTri.get(i) / (double) Math.max(1, toiDa.get(i) > 0 ? toiDa.get(i) : max));
                g2.fillRoundRect(0, yb, Math.max(rong, 4), 10, 10, 10);
                g2.setColor(Theme.TEXT);
                g2.setFont(Theme.SMALL_BOLD);
                g2.drawString(giaTri.get(i) + "/" + toiDa.get(i), rongMax + 8, yb + 10);
                g2.setFont(Theme.SMALL);
            }
        } else {
            int day = h - 24;
            int soCot = nhan.size();
            int khoang = w / soCot;
            int rongCot = Math.min(42, khoang - 20);
            // đường kẻ ngang mờ
            g2.setColor(new Color(0xEEF1F5));
            for (int k = 0; k <= 4; k++) {
                int y = 10 + (day - 10) * k / 4;
                g2.drawLine(0, y, w, y);
            }
            for (int i = 0; i < soCot; i++) {
                int cao = (int) ((day - 24) * giaTri.get(i) / (double) max);
                int x = i * khoang + (khoang - rongCot) / 2;
                g2.setColor(i == soCot - 1 ? Theme.PRIMARY : new Color(0x8DB4EE));
                g2.fillRoundRect(x, day - cao, rongCot, Math.max(cao, 3), 8, 8);
                g2.setColor(Theme.TEXT);
                g2.setFont(Theme.SMALL_BOLD);
                String so = String.valueOf(giaTri.get(i));
                g2.drawString(so, x + (rongCot - g2.getFontMetrics().stringWidth(so)) / 2, day - cao - 6);
                g2.setFont(Theme.SMALL);
                g2.setColor(Theme.MUTED);
                g2.drawString(nhan.get(i), i * khoang + (khoang - fm.stringWidth(nhan.get(i))) / 2, h - 4);
            }
        }
        g2.dispose();
    }
}
