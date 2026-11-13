package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MÀN HÌNH "QUẢN LÝ SỰ KIỆN" (frmTaoSuKien, frmCapNhatSuKien, frmDuyetKeHoach,
 * frmTheoDoiSuKien, frmPhanCongCanBo, frmTraCuuSuKien)
 */
public class QuanLySuKienPanel extends TrangAdmin {

    private static final String[] TRANG_THAI = {"Tất cả trạng thái", "Sắp diễn ra", "Đang diễn ra", "Đã kết thúc", "Đã hủy"};


    private final List<JsonObject> tatCa = new ArrayList<>();
    private final List<JsonObject> dangHien = new ArrayList<>();

    public QuanLySuKienPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        $$$setupUI$$$();
        lblBieuTuongTongSuKien.setIcon(Icons.of(Icons.CALENDAR, 26, new Color(0x1565C0)));
        lblBieuTuongSuKienSapDienRa.setIcon(Icons.of(Icons.CLOCK, 26, new Color(0x159B64)));
        lblBieuTuongTongDangKy.setIcon(Icons.of(Icons.USERS, 26, new Color(0x7952B3)));
        lblBieuTuongKeHoachChoDuyet.setIcon(Icons.of(Icons.FILE, 26, new Color(0xD79318)));
        vungNoiDung.add(mainPanel, BorderLayout.CENTER);
        btnTaoSuKien.addActionListener(e -> SuKienDialogs.formSuKien(this, null, dieuHuong.tenDangNhap(), this::taiLai));
        nutLocTrangThai = new JToggleButton[]{btnLocTatCaSuKien, btnLocKeHoachChoDuyet};
        ButtonGroup nhomTrangThai = new ButtonGroup();
        for (int i = 0; i < nutLocTrangThai.length; i++) {
            final int viTri = i;
            nhomTrangThai.add(nutLocTrangThai[i]);
            nutLocTrangThai[i].addActionListener(e -> chonTrangThai(viTri));
        }
        nutLocTrangThai[0].setSelected(true);
        capNhatMauTrangThai();
        bangSuKien.anCot(0);
        bangSuKien.cotHaiDong(1);
        bangSuKien.doRongCot(1, 230);
        bangSuKien.doRongCot(2, 115);
        bangSuKien.doRongCot(3, 85);
        bangSuKien.coDinhCot(4, 62);
        bangSuKien.cotNhan(5);
        bangSuKien.cotNhan(6);
        bangSuKien.coDinhCot(7, 182);
        bangSuKien.cotThaoTac(7, List.of(
                        new BangDep.NutThaoTac("duyet", "Duyệt", Icons.FILE, "Xem và duyệt kế hoạch"),
                        new BangDep.NutThaoTac("theodoi", "Theo dõi", Icons.EYE, "Theo dõi tình hình sự kiện"),
                        new BangDep.NutThaoTac("sua", null, Icons.EDIT, "Cập nhật sự kiện"),
                        new BangDep.NutThaoTac("phancong", null, Icons.USERS, "Phân công cán bộ phụ trách")),
                (ma, dong) -> {
                    if (dong >= dangHien.size()) return false;
                    boolean cho = "Chờ duyệt".equals(Api.s(dangHien.get(dong), "trangThaiDuyet"));
                    return ma.equals("duyet") ? cho : !ma.equals("theodoi") || !cho;
                },
                (ma, dong) -> {
                    JsonObject sk = dangHien.get(dong);
                    long id = Api.n(sk, "id");
                    switch (ma) {
                        case "duyet" -> SuKienDialogs.duyetKeHoach(this, id, this::taiLai);
                        case "theodoi" -> SuKienDialogs.theoDoi(this, id);
                        case "sua" -> SuKienDialogs.formSuKien(this, id, dieuHuong.tenDangNhap(), this::taiLai);
                        case "phancong" -> SuKienDialogs.phanCong(this, sk, this::taiLai);
                        default -> {
                        }
                    }
                });
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { locVaHien(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { locVaHien(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { locVaHien(); }
        });
        cboTrangThai.addActionListener(e -> locVaHien());
    }

    private void chonTrangThai(int viTri) {
        trangThaiDangChon = viTri;
        nutLocTrangThai[viTri].setSelected(true);
        capNhatMauTrangThai();
        locVaHien();
    }

    private void capNhatMauTrangThai() {
        for (int i = 0; i < nutLocTrangThai.length; i++) {
            boolean dangChon = i == trangThaiDangChon;
            nutLocTrangThai[i].setBackground(new Color(dangChon ? 0x1565C0 : 0xF5F7FA));
            nutLocTrangThai[i].setForeground(dangChon ? Color.WHITE : Theme.TEXT);
        }
    }

    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/su-kien"), kq -> {
            tatCa.clear();
            long sap = 0, dangKy = 0, cho = 0;
            for (JsonElement el : Api.arr(kq)) {
                JsonObject o = el.getAsJsonObject();
                tatCa.add(o);
                boolean laCho = "Chờ duyệt".equals(Api.s(o, "trangThaiDuyet"));
                if (laCho) cho++;
                if (!laCho && "Sắp diễn ra".equals(Api.s(o, "trangThai"))) sap++;
                dangKy += Api.n(o, "soDangKy");
            }
            lblTongSuKien.setText(String.valueOf(tatCa.size()));
            lblSuKienSapDienRa.setText(String.valueOf(sap));
            lblTongDangKy.setText(String.valueOf(dangKy));
            lblKeHoachChoDuyet.setText(String.valueOf(cho));
            btnLocKeHoachChoDuyet.setText(cho > 0 ? "Kế hoạch chờ duyệt (" + cho + ")" : "Kế hoạch chờ duyệt");
            locVaHien();
        });
        dieuHuong.capNhatChuong();
    }

    /** Lọc ngay trên dữ liệu đã tải (số sự kiện ít nên không cần gọi lại API). */
    private void locVaHien() {
        String kw = txtTimKiem.getText().trim().toLowerCase();
        String tt = cboTrangThai.getSelectedIndex() <= 0 ? "" : (String) cboTrangThai.getSelectedItem();
        boolean chiCho = trangThaiDangChon == 1;
        dangHien.clear();
        bangSuKien.xoaHet();
        for (JsonObject o : tatCa) {
            boolean laCho = "Chờ duyệt".equals(Api.s(o, "trangThaiDuyet"));
            if (chiCho && !laCho) continue;
            if (!tt.isEmpty() && !tt.equals(Api.s(o, "trangThai"))) continue;
            String tim = (Api.s(o, "tenSuKien") + " " + Api.s(o, "maSuKien") + " " + Api.s(o, "diaDiem")).toLowerCase();
            if (!kw.isEmpty() && !tim.contains(kw)) continue;
            dangHien.add(o);
            bangSuKien.themDong(Api.n(o, "id"), Api.s(o, "tenSuKien") + "\n" + Api.s(o, "maSuKien") + "  |  " + Api.s(o, "diaDiem"),
                    Api.s(o, "thoiGianBatDau"), Api.n(o, "soDangKy") + " / " + Api.n(o, "soLuongToiDa"),
                    String.valueOf(Api.n(o, "soCanBo")), Api.s(o, "trangThai"), Api.s(o, "trangThaiDuyet"), "");
        }
        lblSoLuong.setText(dangHien.isEmpty() ? "Không tìm thấy sự kiện phù hợp"
                : "Hiển thị " + dangHien.size() + " / " + tatCa.size() + " sự kiện");
        hienSoKetQua(dangHien.size());
    }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel lblTongSuKien;
    private javax.swing.JLabel lblSuKienSapDienRa;
    private javax.swing.JLabel lblTongDangKy;
    private javax.swing.JLabel lblKeHoachChoDuyet;
    private int trangThaiDangChon;
    private JToggleButton[] nutLocTrangThai;
    private JPanel pnlLocTrangThai;
    private JToggleButton btnLocTatCaSuKien;
    private JToggleButton btnLocKeHoachChoDuyet;
    private javax.swing.JButton btnTaoSuKien;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JComboBox<String> cboTrangThai;
    private com.eventmanager.fe.gui.Admin.common.BangDep bangSuKien;
    private javax.swing.JLabel lblSoLuong;

    private javax.swing.JPanel pnlTieuDe;
    private javax.swing.JPanel pnlNoiDung;
    private javax.swing.JPanel pnlThongKe;
    private javax.swing.JPanel pnlQuanLy;
    private javax.swing.JPanel pnlBoLoc;

    private JLabel lblBieuTuongTongSuKien;
    private JLabel lblBieuTuongSuKienSapDienRa;
    private JLabel lblBieuTuongTongDangKy;
    private JLabel lblBieuTuongKeHoachChoDuyet;

    private javax.swing.JPanel pnlTruongTxtTimKiem;
    private javax.swing.JLabel lblNhanTxtTimKiem;
    private javax.swing.JPanel pnlTruongCboTrangThai;
    private javax.swing.JLabel lblNhanCboTrangThai;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xF5F7FA));
        pnlTieuDe = new javax.swing.JPanel();
        pnlTieuDe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlTieuDe.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlTieuDe, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        javax.swing.JLabel panela86 = new javax.swing.JLabel();
        panela86.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela86.setForeground(new java.awt.Color(0x0B2B50));
        panela86.setText("Quản lý sự kiện");
        pnlTieuDe.add(panela86, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela87 = new javax.swing.JLabel();
        panela87.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela87.setForeground(new java.awt.Color(0x627D98));
        panela87.setText("Tạo, cập nhật, duyệt kế hoạch, phân công và theo dõi sự kiện");
        pnlTieuDe.add(panela87, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlNoiDung = new javax.swing.JPanel();
        pnlNoiDung.setLayout(new java.awt.BorderLayout(12, 16));
        pnlNoiDung.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlNoiDung, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        pnlThongKe = new javax.swing.JPanel();
        pnlThongKe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new java.awt.Insets(0, 0, 0, 0), 12, 10, true, false));
        pnlThongKe.setBackground(new java.awt.Color(0xF5F7FA));
        pnlNoiDung.add(pnlThongKe, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel panela69 = new javax.swing.JPanel();
        panela69.setLayout(new java.awt.BorderLayout(12, 16));
        panela69.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela69.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela69, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongTongSuKien = new javax.swing.JLabel();
        lblBieuTuongTongSuKien.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongTongSuKien.setForeground(new java.awt.Color(0x1565C0));
        lblBieuTuongTongSuKien.setText("");
        lblBieuTuongTongSuKien.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongTongSuKien.setHorizontalAlignment(0);
        panela69.add(lblBieuTuongTongSuKien, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela68 = new javax.swing.JPanel();
        panela68.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela68.setBackground(new java.awt.Color(0xFFFFFF));
        panela69.add(panela68, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela66 = new javax.swing.JLabel();
        panela66.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela66.setForeground(new java.awt.Color(0x627D98));
        panela66.setText("Tổng sự kiện");
        panela68.add(panela66, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTongSuKien = new javax.swing.JLabel();
        lblTongSuKien.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTongSuKien.setForeground(new java.awt.Color(0x1565C0));
        lblTongSuKien.setText("—");
        panela68.add(lblTongSuKien, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela74 = new javax.swing.JPanel();
        panela74.setLayout(new java.awt.BorderLayout(12, 16));
        panela74.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela74.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela74, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongSuKienSapDienRa = new javax.swing.JLabel();
        lblBieuTuongSuKienSapDienRa.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongSuKienSapDienRa.setForeground(new java.awt.Color(0x159B64));
        lblBieuTuongSuKienSapDienRa.setText("");
        lblBieuTuongSuKienSapDienRa.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongSuKienSapDienRa.setHorizontalAlignment(0);
        panela74.add(lblBieuTuongSuKienSapDienRa, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela73 = new javax.swing.JPanel();
        panela73.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela73.setBackground(new java.awt.Color(0xFFFFFF));
        panela74.add(panela73, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela71 = new javax.swing.JLabel();
        panela71.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela71.setForeground(new java.awt.Color(0x627D98));
        panela71.setText("Sắp diễn ra");
        panela73.add(panela71, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblSuKienSapDienRa = new javax.swing.JLabel();
        lblSuKienSapDienRa.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblSuKienSapDienRa.setForeground(new java.awt.Color(0x159B64));
        lblSuKienSapDienRa.setText("—");
        panela73.add(lblSuKienSapDienRa, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela79 = new javax.swing.JPanel();
        panela79.setLayout(new java.awt.BorderLayout(12, 16));
        panela79.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela79.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela79, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongTongDangKy = new javax.swing.JLabel();
        lblBieuTuongTongDangKy.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongTongDangKy.setForeground(new java.awt.Color(0x7952B3));
        lblBieuTuongTongDangKy.setText("");
        lblBieuTuongTongDangKy.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongTongDangKy.setHorizontalAlignment(0);
        panela79.add(lblBieuTuongTongDangKy, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela78 = new javax.swing.JPanel();
        panela78.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela78.setBackground(new java.awt.Color(0xFFFFFF));
        panela79.add(panela78, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela76 = new javax.swing.JLabel();
        panela76.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela76.setForeground(new java.awt.Color(0x627D98));
        panela76.setText("Lượt đăng ký");
        panela78.add(panela76, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTongDangKy = new javax.swing.JLabel();
        lblTongDangKy.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTongDangKy.setForeground(new java.awt.Color(0x7952B3));
        lblTongDangKy.setText("—");
        panela78.add(lblTongDangKy, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela84 = new javax.swing.JPanel();
        panela84.setLayout(new java.awt.BorderLayout(12, 16));
        panela84.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela84.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela84, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongKeHoachChoDuyet = new javax.swing.JLabel();
        lblBieuTuongKeHoachChoDuyet.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongKeHoachChoDuyet.setForeground(new java.awt.Color(0xD79318));
        lblBieuTuongKeHoachChoDuyet.setText("");
        lblBieuTuongKeHoachChoDuyet.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongKeHoachChoDuyet.setHorizontalAlignment(0);
        panela84.add(lblBieuTuongKeHoachChoDuyet, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela83 = new javax.swing.JPanel();
        panela83.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela83.setBackground(new java.awt.Color(0xFFFFFF));
        panela84.add(panela83, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela81 = new javax.swing.JLabel();
        panela81.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela81.setForeground(new java.awt.Color(0x627D98));
        panela81.setText("Kế hoạch chờ duyệt");
        panela83.add(panela81, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblKeHoachChoDuyet = new javax.swing.JLabel();
        lblKeHoachChoDuyet.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblKeHoachChoDuyet.setForeground(new java.awt.Color(0xD79318));
        lblKeHoachChoDuyet.setText("—");
        panela83.add(lblKeHoachChoDuyet, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlQuanLy = new javax.swing.JPanel();
        pnlQuanLy.setLayout(new java.awt.BorderLayout(12, 16));
        pnlQuanLy.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        pnlQuanLy.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNoiDung.add(pnlQuanLy, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela60 = new javax.swing.JPanel();
        panela60.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 12, 0, false, false));
        panela60.setBackground(new java.awt.Color(0xFFFFFF));
        pnlQuanLy.add(panela60, java.awt.BorderLayout.NORTH);
        pnlLocTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlLocTrangThai.setOpaque(false);
        panela60.add(pnlLocTrangThai, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 8, 1, 7, 0, null, null, null));
        btnLocTatCaSuKien = new JToggleButton("Tất cả sự kiện");
        btnLocTatCaSuKien.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocTatCaSuKien.setToolTipText("Lọc: tất cả sự kiện");
        btnLocTatCaSuKien.setContentAreaFilled(false);
        btnLocTatCaSuKien.setOpaque(true);
        btnLocTatCaSuKien.setBorderPainted(false);
        btnLocTatCaSuKien.setFocusPainted(false);
        btnLocTatCaSuKien.setBackground(new Color(0x1565C0));
        btnLocTatCaSuKien.setForeground(new Color(0xFFFFFF));
        btnLocTatCaSuKien.setSelected(true);
        btnLocTatCaSuKien.setPreferredSize(new Dimension(150, 30));
        pnlLocTrangThai.add(btnLocTatCaSuKien);
        btnLocKeHoachChoDuyet = new JToggleButton("Kế hoạch chờ duyệt");
        btnLocKeHoachChoDuyet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocKeHoachChoDuyet.setToolTipText("Lọc: kế hoạch chờ duyệt");
        btnLocKeHoachChoDuyet.setContentAreaFilled(false);
        btnLocKeHoachChoDuyet.setOpaque(true);
        btnLocKeHoachChoDuyet.setBorderPainted(false);
        btnLocKeHoachChoDuyet.setFocusPainted(false);
        btnLocKeHoachChoDuyet.setBackground(new Color(0xF5F7FA));
        btnLocKeHoachChoDuyet.setForeground(new Color(0x243B53));
        btnLocKeHoachChoDuyet.setSelected(false);
        btnLocKeHoachChoDuyet.setPreferredSize(new Dimension(196, 30));
        pnlLocTrangThai.add(btnLocKeHoachChoDuyet);
        btnTaoSuKien = new javax.swing.JButton();
        btnTaoSuKien.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnTaoSuKien.setBackground(new java.awt.Color(0x1565C0));
        btnTaoSuKien.setForeground(new java.awt.Color(0xFFFFFF));
        btnTaoSuKien.setFocusPainted(false);
        btnTaoSuKien.setContentAreaFilled(false);
        btnTaoSuKien.setOpaque(true);
        btnTaoSuKien.setBorderPainted(false);
        btnTaoSuKien.setPreferredSize(new java.awt.Dimension(130, 30));
        btnTaoSuKien.setText("+ Tạo sự kiện");
        panela60.add(btnTaoSuKien, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 4, 0, 0, 0, null, null, null));
        javax.swing.JPanel panela63 = new javax.swing.JPanel();
        panela63.setLayout(new java.awt.BorderLayout(12, 16));
        panela63.setBackground(new java.awt.Color(0xFFFFFF));
        pnlQuanLy.add(panela63, java.awt.BorderLayout.CENTER);
        pnlBoLoc = new javax.swing.JPanel();
        pnlBoLoc.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlBoLoc.setBackground(new java.awt.Color(0xFFFFFF));
        panela63.add(pnlBoLoc, java.awt.BorderLayout.NORTH);
        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTimKiem.setToolTipText("Tên hoặc mã sự kiện");
        txtTimKiem.setPreferredSize(new java.awt.Dimension(180, 32));
        txtTimKiem.setMinimumSize(new java.awt.Dimension(50, 32));
        pnlTruongTxtTimKiem = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongTxtTimKiem.setOpaque(false);
        lblNhanTxtTimKiem = new javax.swing.JLabel("Tìm sự kiện");
        lblNhanTxtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanTxtTimKiem.setForeground(new java.awt.Color(0x627D98));
        lblNhanTxtTimKiem.setLabelFor(txtTimKiem);
        pnlTruongTxtTimKiem.add(lblNhanTxtTimKiem, java.awt.BorderLayout.NORTH);
        pnlTruongTxtTimKiem.add(txtTimKiem, java.awt.BorderLayout.CENTER);
        pnlBoLoc.add(pnlTruongTxtTimKiem, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        cboTrangThai = new javax.swing.JComboBox();
        cboTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboTrangThai.setToolTipText("Chọn trạng thái sự kiện cần xem");
        cboTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Tất cả trạng thái", "Sắp diễn ra", "Đang diễn ra", "Đã kết thúc", "Đã hủy"}));
        cboTrangThai.setPreferredSize(new java.awt.Dimension(175, 32));
        cboTrangThai.setMinimumSize(new java.awt.Dimension(110, 32));
        pnlTruongCboTrangThai = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongCboTrangThai.setOpaque(false);
        lblNhanCboTrangThai = new javax.swing.JLabel("Trạng thái sự kiện");
        lblNhanCboTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanCboTrangThai.setForeground(new java.awt.Color(0x627D98));
        lblNhanCboTrangThai.setLabelFor(cboTrangThai);
        pnlTruongCboTrangThai.add(lblNhanCboTrangThai, java.awt.BorderLayout.NORTH);
        pnlTruongCboTrangThai.add(cboTrangThai, java.awt.BorderLayout.CENTER);
        pnlBoLoc.add(pnlTruongCboTrangThai, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JScrollPane panela61 = new javax.swing.JScrollPane();
        panela63.add(panela61, java.awt.BorderLayout.CENTER);
        panela61.setViewportView(bangSuKien);
        lblSoLuong = new javax.swing.JLabel();
        lblSoLuong.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblSoLuong.setForeground(new java.awt.Color(0x243B53));
        lblSoLuong.setText(" ");
        panela63.add(lblSoLuong, java.awt.BorderLayout.SOUTH);
    }

    private void createUIComponents() {
        txtTimKiem = UI.oTimKiem("Tên hoặc mã sự kiện");
        bangSuKien = new BangDep("id", "Sự kiện", "Bắt đầu", "Đăng ký", "CB", "Trạng thái", "Duyệt", "Thao tác");
    }
}
