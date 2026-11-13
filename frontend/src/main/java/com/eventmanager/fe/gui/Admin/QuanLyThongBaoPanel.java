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
 * MÀN HÌNH "QUẢN LÝ THÔNG BÁO" (frmTaoThongBao, frmDuyetTB, frmDangTB, frmCapNhatTB, frmQuanLyTB)
 *
 * Mỗi tab là một bước trong vòng đời thông báo:
 *   Chờ duyệt  ->  Đã duyệt  ->  Đã đăng  ->  Đã gỡ
 *        \-> Từ chối (sửa lại sẽ quay về Chờ duyệt)
 */
public class QuanLyThongBaoPanel extends TrangAdmin {

    private static final String[] TAB = {"Chờ duyệt", "Đã duyệt", "Đã đăng", "Từ chối", "Đã gỡ", "Tất cả"};
    private static final String[] DOI_TUONG = {"Tất cả người tham gia", "Sinh viên", "Ban tổ chức", "Cán bộ sự kiện",
            "Đại biểu", "Khách mời"};

    private final List<JsonObject> dangHien = new ArrayList<>();
    private final javax.swing.Timer henGio;
    private int lanTaiDanhSach;

    public QuanLyThongBaoPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        $$$setupUI$$$();
        vungNoiDung.add(mainPanel, BorderLayout.CENTER);
        btnTaoThongBao.addActionListener(e -> formThongBao(null));
        nutLocTrangThai = new JToggleButton[]{btnLocChoDuyet, btnLocDaDuyet, btnLocDaDang, btnLocTuChoi, btnLocDaGo, btnLocTatCa};
        ButtonGroup nhomTrangThai = new ButtonGroup();
        for (int i = 0; i < nutLocTrangThai.length; i++) {
            final int viTri = i;
            nhomTrangThai.add(nutLocTrangThai[i]);
            nutLocTrangThai[i].addActionListener(e -> chonTrangThai(viTri));
        }
        nutLocTrangThai[0].setSelected(true);
        capNhatMauTrangThai();
        bangThongBao.anCot(0);
        bangThongBao.cotHaiDong(1);
        bangThongBao.doRongCot(1, 330);
        bangThongBao.doRongCot(2, 110);
        bangThongBao.doRongCot(3, 100);
        bangThongBao.cotNhan(4);
        bangThongBao.coDinhCot(5, 186);
        bangThongBao.cotThaoTac(5, List.of(
                        new BangDep.NutThaoTac("xem", "Chi tiết", Icons.EYE, "Xem nội dung"),
                        new BangDep.NutThaoTac("sua", null, Icons.EDIT, "Cập nhật thông báo"),
                        new BangDep.NutThaoTac("duyet", null, Icons.CHECK, "Duyệt thông báo"),
                        new BangDep.NutThaoTac("dang", null, Icons.SEND, "Đăng thông báo"),
                        new BangDep.NutThaoTac("go", null, Icons.TRASH, "Gỡ thông báo")),
                (ma, dong) -> dong < dangHien.size() && coNut(ma, Api.s(dangHien.get(dong), "trangThai")),
                (ma, dong) -> {
                    JsonObject tb = dangHien.get(dong);
                    switch (ma) {
                        case "xem" -> chiTiet(tb);
                        case "sua" -> formThongBao(tb);
                        default -> doiTrangThai(tb, ma);
                    }
                });
        henGio = new javax.swing.Timer(300, e -> taiDanhSach());
        henGio.setRepeats(false);
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
        });
    }

    /** Nút nào được hiện tùy theo trạng thái thông báo. */
    private static boolean coNut(String ma, String tt) {
        return switch (ma) {
            case "xem" -> true;
            case "sua" -> !"Đã gỡ".equals(tt);
            case "duyet" -> "Chờ duyệt".equals(tt);
            case "dang" -> "Đã duyệt".equals(tt);
            case "go" -> "Đã đăng".equals(tt);
            default -> false;
        };
    }

    private void chonTrangThai(int viTri) {
        trangThaiDangChon = viTri;
        nutLocTrangThai[viTri].setSelected(true);
        capNhatMauTrangThai();
        taiDanhSach();
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
        Api.chayNen(this, () -> Api.get("/api/admin/thong-bao/dem"), kq -> {
            JsonObject o = Api.obj(kq);
            long tong = 0;
            for (int i = 0; i < TAB.length - 1; i++) {
                long so = Api.n(o, TAB[i]);
                tong += so;
                nutLocTrangThai[i].setText(TAB[i] + " (" + so + ")");
            }
            nutLocTrangThai[TAB.length - 1].setText("Tất cả (" + tong + ")");
        });
        taiDanhSach();
        dieuHuong.capNhatChuong();
    }

    private void taiDanhSach() {
        final int lanTai = ++lanTaiDanhSach;
        String tt = trangThaiDangChon == TAB.length - 1 ? "" : TAB[trangThaiDangChon];
        String url = "/api/admin/thong-bao?trangThai=" + Api.e(tt) + "&tuKhoa=" + Api.e(txtTimKiem.getText().trim());
        Api.chayNen(this, () -> Api.get(url), kq -> {
            if (lanTai != lanTaiDanhSach) return;
            dangHien.clear();
            bangThongBao.xoaHet();
            for (JsonElement el : Api.arr(kq)) {
                JsonObject o = el.getAsJsonObject();
                dangHien.add(o);
                bangThongBao.themDong(Api.n(o, "id"), Api.s(o, "tieuDe") + "\n" + Api.s(o, "maThongBao") + "  |  Gửi tới: "
                                + Api.s(o, "doiTuong"), Api.s(o, "nguoiTao"), Api.s(o, "ngayGui"), Api.s(o, "trangThai"), "");
            }
            hienSoKetQua(dangHien.size());
        });
    }

    // ======================= DUYỆT / ĐĂNG / GỠ =======================
    private void doiTrangThai(JsonObject tb, String hanhDong) {
        long id = Api.n(tb, "id");
        String ten = Api.s(tb, "tieuDe");
        String[] ch = switch (hanhDong) {
            case "duyet" -> new String[]{"Duyệt thông báo", "Duyệt thông báo <b>" + ten + "</b>?", "Duyệt", "Duyệt thông báo thành công"};
            case "dang" -> new String[]{"Đăng thông báo", "Đăng thông báo <b>" + ten + "</b> tới <b>" + Api.s(tb, "doiTuong") + "</b>?", "Đăng", "Đăng thông báo thành công"};
            default -> new String[]{"Gỡ thông báo", "Gỡ thông báo <b>" + ten + "</b>? Người nhận sẽ không còn thấy thông báo này.", "Gỡ", "Gỡ thành công"};
        };
        if (!HopThoai.xacNhan(this, ch[0], ch[1], ch[2], "go".equals(hanhDong) ? UI.Kieu.NGUY_HIEM : UI.Kieu.CHINH)) return;
        Api.chayThaoTac(this, () -> Api.put("/api/admin/thong-bao/" + id + "/" + hanhDong, null), r -> {
            HopThoai.thanhCong(this, ch[3]);
            taiLai();
        });
    }

    // ======================= XEM CHI TIẾT =======================
    private void chiTiet(JsonObject tb) {
        String tt = Api.s(tb, "trangThai");
        long id = Api.n(tb, "id");
        HopThoai.FormDialog d = new HopThoai.FormDialog(this, Api.s(tb, "tieuDe"),
                Api.s(tb, "maThongBao") + "  |  Người tạo: " + Api.s(tb, "nguoiTao") + "  |  Ngày gửi: " + Api.s(tb, "ngayGui"), 580);
        UI.Badge b = new UI.Badge();
        b.setText(tt);
        d.khoi(UI.hang(10, b, UI.nhan("Gửi tới: " + Api.s(tb, "doiTuong")
                + (Api.s(tb, "ngayDang").isEmpty() ? "" : "  |  Đăng lúc " + Api.s(tb, "ngayDang")), Theme.SMALL, Theme.TEXT_2)));
        JTextArea nd = UI.vungChu(7);
        nd.setText(Api.s(tb, "noiDung"));
        nd.setEditable(false);
        d.truong("Nội dung", UI.cuonVungChu(nd), true);
        if (!Api.s(tb, "lyDoTuChoi").isEmpty()) {
            d.truong("Lý do từ chối", HopThoai.oChiXem(Api.s(tb, "lyDoTuChoi")), true);
        }

        UI.FlatButton dong = UI.nut("Đóng", UI.Kieu.VIEN);
        dong.addActionListener(e -> d.dispose());
        d.nut(dong);
        if ("Chờ duyệt".equals(tt)) {
            UI.FlatButton tuChoi = UI.nut("Từ chối", Icons.X, UI.Kieu.NGUY_HIEM);
            tuChoi.addActionListener(e -> {
                String ld = HopThoai.nhapLyDo(d, "Từ chối thông báo", "VD: nội dung chưa rõ ràng");
                if (ld == null) return;
                Api.chayNen(d, () -> Api.put("/api/admin/thong-bao/" + id + "/tu-choi", Api.body("lyDo", ld)), r -> {
                    d.dispose();
                    HopThoai.thanhCong(this, "Đã từ chối thông báo");
                    taiLai();
                });
            });
            d.nut(tuChoi);
        }
        String hd = "Chờ duyệt".equals(tt) ? "duyet" : "Đã duyệt".equals(tt) ? "dang" : "Đã đăng".equals(tt) ? "go" : null;
        if (hd != null) {
            UI.FlatButton chinh = UI.nut(hd.equals("duyet") ? "Duyệt" : hd.equals("dang") ? "Đăng thông báo" : "Gỡ thông báo",
                    hd.equals("duyet") ? Icons.CHECK : hd.equals("dang") ? Icons.SEND : Icons.TRASH,
                    hd.equals("go") ? UI.Kieu.NGUY_HIEM : UI.Kieu.THANH_CONG);
            chinh.addActionListener(e -> {
                d.dispose();
                doiTrangThai(tb, hd);
            });
            d.nut(chinh);
        }
        d.hien();
    }

    // ======================= TẠO / CẬP NHẬT =======================
    private void formThongBao(JsonObject tb) {
        boolean laSua = tb != null;
        HopThoai.FormDialog d = new HopThoai.FormDialog(this, laSua ? "Cập nhật thông báo" : "Tạo thông báo",
                laSua ? Api.s(tb, "maThongBao") + " - " + Api.s(tb, "trangThai")
                        : "Thông báo mới sẽ nằm ở mục Chờ duyệt", 580);
        ThongBaoDialog view = new ThongBaoDialog();
        JTextField txtTieuDe = view.txtTieuDe;
        JComboBox<String> cboDoiTuongNhan = view.cboDoiTuongNhan;
        JTextArea txtNoiDung = view.txtNoiDung;
        JLabel loi = view.loi;
        JButton huy = view.huy, luu = view.luu;
        luu.setText(laSua ? "Cập nhật" : "Tạo thông báo");
        huy.addActionListener(e -> d.dispose());
        d.khoi(view.mainPanel);
        if (laSua) {
            txtTieuDe.setText(Api.s(tb, "tieuDe"));
            String dt = Api.s(tb, "doiTuong");
            boolean co = false;
            for (String s : DOI_TUONG) co |= s.equals(dt);
            if (!co && !dt.isEmpty()) cboDoiTuongNhan.addItem(dt);
            cboDoiTuongNhan.setSelectedItem(dt);
            txtNoiDung.setText(Api.s(tb, "noiDung"));
        }
        luu.addActionListener(e -> {
            JsonObject body = Api.body("tieuDe", txtTieuDe.getText().trim(), "noiDung", txtNoiDung.getText().trim(),
                    "doiTuong", String.valueOf(cboDoiTuongNhan.getSelectedItem()));
            luu.setEnabled(false);
            SuKienDialogs.guiForm(d, luu, loi,
                    () -> laSua ? Api.put("/api/admin/thong-bao/" + Api.n(tb, "id"), body)
                            : Api.post("/api/admin/thong-bao?nguoiTao=" + Api.e(dieuHuong.tenDangNhap()), body),
                    () -> {
                        HopThoai.thanhCong(this, laSua ? "Cập nhật thành công" : "Tạo thông báo thành công");
                        if (!laSua) chonTrangThai(0);
                        taiLai();
                    });
        });
        d.hien();
    }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private int trangThaiDangChon;
    private JToggleButton[] nutLocTrangThai;
    private JPanel pnlLocTrangThai;
    private JToggleButton btnLocChoDuyet;
    private JToggleButton btnLocDaDuyet;
    private JToggleButton btnLocDaDang;
    private JToggleButton btnLocTuChoi;
    private JToggleButton btnLocDaGo;
    private JToggleButton btnLocTatCa;
    private javax.swing.JButton btnTaoThongBao;
    private javax.swing.JTextField txtTimKiem;
    private com.eventmanager.fe.gui.Admin.common.BangDep bangThongBao;

    private javax.swing.JPanel pnlTieuDe;
    private javax.swing.JPanel pnlNoiDung;
    private javax.swing.JPanel pnlBoLoc;

    private javax.swing.JPanel pnlTruongTxtTimKiem;
    private javax.swing.JLabel lblNhanTxtTimKiem;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xF5F7FA));
        pnlTieuDe = new javax.swing.JPanel();
        pnlTieuDe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlTieuDe.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlTieuDe, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        javax.swing.JLabel panela107 = new javax.swing.JLabel();
        panela107.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela107.setForeground(new java.awt.Color(0x0B2B50));
        panela107.setText("Quản lý thông báo");
        pnlTieuDe.add(panela107, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela108 = new javax.swing.JLabel();
        panela108.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela108.setForeground(new java.awt.Color(0x627D98));
        panela108.setText("Tạo, duyệt, đăng và quản lý các thông báo gửi tới đoàn viên");
        pnlTieuDe.add(panela108, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlNoiDung = new javax.swing.JPanel();
        pnlNoiDung.setLayout(new java.awt.BorderLayout(12, 16));
        pnlNoiDung.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        pnlNoiDung.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(pnlNoiDung, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        javax.swing.JPanel panela103 = new javax.swing.JPanel();
        panela103.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), 12, 0, false, false));
        panela103.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNoiDung.add(panela103, java.awt.BorderLayout.NORTH);
        pnlLocTrangThai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlLocTrangThai.setOpaque(false);
        panela103.add(pnlLocTrangThai, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 8, 1, 7, 0, null, null, null));
        btnLocChoDuyet = new JToggleButton("Chờ duyệt");
        btnLocChoDuyet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocChoDuyet.setToolTipText("Lọc: chờ duyệt");
        btnLocChoDuyet.setContentAreaFilled(false);
        btnLocChoDuyet.setOpaque(true);
        btnLocChoDuyet.setBorderPainted(false);
        btnLocChoDuyet.setFocusPainted(false);
        btnLocChoDuyet.setBackground(new Color(0x1565C0));
        btnLocChoDuyet.setForeground(new Color(0xFFFFFF));
        btnLocChoDuyet.setSelected(true);
        btnLocChoDuyet.setPreferredSize(new Dimension(104, 30));
        pnlLocTrangThai.add(btnLocChoDuyet);
        btnLocDaDuyet = new JToggleButton("Đã duyệt");
        btnLocDaDuyet.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocDaDuyet.setToolTipText("Lọc: đã duyệt");
        btnLocDaDuyet.setContentAreaFilled(false);
        btnLocDaDuyet.setOpaque(true);
        btnLocDaDuyet.setBorderPainted(false);
        btnLocDaDuyet.setFocusPainted(false);
        btnLocDaDuyet.setBackground(new Color(0xF5F7FA));
        btnLocDaDuyet.setForeground(new Color(0x243B53));
        btnLocDaDuyet.setSelected(false);
        btnLocDaDuyet.setPreferredSize(new Dimension(100, 30));
        pnlLocTrangThai.add(btnLocDaDuyet);
        btnLocDaDang = new JToggleButton("Đã đăng");
        btnLocDaDang.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocDaDang.setToolTipText("Lọc: đã đăng");
        btnLocDaDang.setContentAreaFilled(false);
        btnLocDaDang.setOpaque(true);
        btnLocDaDang.setBorderPainted(false);
        btnLocDaDang.setFocusPainted(false);
        btnLocDaDang.setBackground(new Color(0xF5F7FA));
        btnLocDaDang.setForeground(new Color(0x243B53));
        btnLocDaDang.setSelected(false);
        btnLocDaDang.setPreferredSize(new Dimension(100, 30));
        pnlLocTrangThai.add(btnLocDaDang);
        btnLocTuChoi = new JToggleButton("Từ chối");
        btnLocTuChoi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocTuChoi.setToolTipText("Lọc: từ chối");
        btnLocTuChoi.setContentAreaFilled(false);
        btnLocTuChoi.setOpaque(true);
        btnLocTuChoi.setBorderPainted(false);
        btnLocTuChoi.setFocusPainted(false);
        btnLocTuChoi.setBackground(new Color(0xF5F7FA));
        btnLocTuChoi.setForeground(new Color(0x243B53));
        btnLocTuChoi.setSelected(false);
        btnLocTuChoi.setPreferredSize(new Dimension(90, 30));
        pnlLocTrangThai.add(btnLocTuChoi);
        btnLocDaGo = new JToggleButton("Đã gỡ");
        btnLocDaGo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocDaGo.setToolTipText("Lọc: đã gỡ");
        btnLocDaGo.setContentAreaFilled(false);
        btnLocDaGo.setOpaque(true);
        btnLocDaGo.setBorderPainted(false);
        btnLocDaGo.setFocusPainted(false);
        btnLocDaGo.setBackground(new Color(0xF5F7FA));
        btnLocDaGo.setForeground(new Color(0x243B53));
        btnLocDaGo.setSelected(false);
        btnLocDaGo.setPreferredSize(new Dimension(90, 30));
        pnlLocTrangThai.add(btnLocDaGo);
        btnLocTatCa = new JToggleButton("Tất cả");
        btnLocTatCa.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLocTatCa.setToolTipText("Lọc: tất cả");
        btnLocTatCa.setContentAreaFilled(false);
        btnLocTatCa.setOpaque(true);
        btnLocTatCa.setBorderPainted(false);
        btnLocTatCa.setFocusPainted(false);
        btnLocTatCa.setBackground(new Color(0xF5F7FA));
        btnLocTatCa.setForeground(new Color(0x243B53));
        btnLocTatCa.setSelected(false);
        btnLocTatCa.setPreferredSize(new Dimension(96, 30));
        pnlLocTrangThai.add(btnLocTatCa);
        btnTaoThongBao = new javax.swing.JButton();
        btnTaoThongBao.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnTaoThongBao.setBackground(new java.awt.Color(0x1565C0));
        btnTaoThongBao.setForeground(new java.awt.Color(0xFFFFFF));
        btnTaoThongBao.setFocusPainted(false);
        btnTaoThongBao.setContentAreaFilled(false);
        btnTaoThongBao.setOpaque(true);
        btnTaoThongBao.setBorderPainted(false);
        btnTaoThongBao.setPreferredSize(new java.awt.Dimension(130, 30));
        btnTaoThongBao.setText("+ Tạo thông báo");
        panela103.add(btnTaoThongBao, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 4, 0, 0, 0, null, null, null));
        javax.swing.JPanel panela105 = new javax.swing.JPanel();
        panela105.setLayout(new java.awt.BorderLayout(12, 16));
        panela105.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNoiDung.add(panela105, java.awt.BorderLayout.CENTER);
        pnlBoLoc = new javax.swing.JPanel();
        pnlBoLoc.setLayout(new java.awt.BorderLayout(12, 16));
        pnlBoLoc.setBackground(new java.awt.Color(0xFFFFFF));
        panela105.add(pnlBoLoc, java.awt.BorderLayout.NORTH);
        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTimKiem.setToolTipText("Tiêu đề, mã thông báo");
        txtTimKiem.setPreferredSize(new java.awt.Dimension(180, 32));
        txtTimKiem.setMinimumSize(new java.awt.Dimension(50, 32));
        pnlTruongTxtTimKiem = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongTxtTimKiem.setOpaque(false);
        lblNhanTxtTimKiem = new javax.swing.JLabel("Tìm thông báo");
        lblNhanTxtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanTxtTimKiem.setForeground(new java.awt.Color(0x627D98));
        lblNhanTxtTimKiem.setLabelFor(txtTimKiem);
        pnlTruongTxtTimKiem.add(lblNhanTxtTimKiem, java.awt.BorderLayout.NORTH);
        pnlTruongTxtTimKiem.add(txtTimKiem, java.awt.BorderLayout.CENTER);
        pnlBoLoc.add(pnlTruongTxtTimKiem, java.awt.BorderLayout.WEST);
        javax.swing.JScrollPane panela104 = new javax.swing.JScrollPane();
        panela105.add(panela104, java.awt.BorderLayout.CENTER);
        panela104.setViewportView(bangThongBao);
    }

    private void createUIComponents() {
        txtTimKiem = UI.oTimKiem("Tiêu đề, mã thông báo");
        bangThongBao = new BangDep("id", "Thông báo", "Người tạo", "Ngày gửi", "Trạng thái", "Thao tác");
    }
}
