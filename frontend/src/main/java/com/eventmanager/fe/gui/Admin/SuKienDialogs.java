package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** Hộp thoại của màn hình Quản lý sự kiện. */
final class SuKienDialogs {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private SuKienDialogs() {
        $$$setupUI$$$();
    }

    // ======================= TẠO / CẬP NHẬT SỰ KIỆN =======================
    static void formSuKien(Component cha, Long id, String nguoiTao, Runnable xongThi) {
        if (id == null) {
            hienForm(cha, null, nguoiTao, xongThi);
        } else {
            Api.chayNen(cha, () -> Api.get("/api/admin/su-kien/" + id), kq -> hienForm(cha, Api.obj(kq), nguoiTao, xongThi));
        }
    }

    private static void hienForm(Component cha, JsonObject sk, String nguoiTao, Runnable xongThi) {
        boolean laSua = sk != null;
        HopThoai.FormDialog d = new HopThoai.FormDialog(cha, laSua ? "Cập nhật sự kiện" : "Tạo sự kiện mới",
                laSua ? "Mã sự kiện: " + Api.s(sk, "maSuKien") : "Sự kiện do Quản trị viên tạo được duyệt ngay", 600);

        SuKienDialogs view = new SuKienDialogs();
        JTextField txtTenSuKien = view.txtTenSuKien;
        JTextField txtDiaDiem = view.txtDiaDiem;
        JTextField txtThoiGianBatDau = view.txtThoiGianBatDau;
        JTextField txtThoiGianKetThuc = view.txtThoiGianKetThuc;
        JTextField txtSoLuongToiDa = view.txtSoLuongToiDa;
        JTextField txtDonViToChuc = view.txtDonViToChuc;
        JComboBox<String> cboTrangThai = view.cboTrangThai;
        JTextArea txtMoTa = view.txtMoTa;
        JLabel loi = view.loi;
        JButton huy = view.huy, luu = view.luu;
        view.nhomTrangThai.setVisible(laSua);
        luu.setText(laSua ? "Lưu" : "Tạo sự kiện");
        huy.addActionListener(e -> d.dispose());
        d.khoi(view.mainPanel);

        if (laSua) {
            txtTenSuKien.setText(Api.s(sk, "tenSuKien"));
            txtDiaDiem.setText(Api.s(sk, "diaDiem"));
            txtThoiGianBatDau.setText(Api.s(sk, "thoiGianBatDau"));
            txtThoiGianKetThuc.setText(Api.s(sk, "thoiGianKetThuc"));
            txtSoLuongToiDa.setText(String.valueOf(Api.n(sk, "soLuongToiDa")));
            txtDonViToChuc.setText(Api.s(sk, "donViToChuc"));
            cboTrangThai.setSelectedItem(Api.s(sk, "trangThai"));
            txtMoTa.setText(Api.s(sk, "moTa"));
        } else {
            // Gợi ý sẵn: 7 ngày nữa, 8h - 11h
            LocalDateTime bd = LocalDateTime.now().plusDays(7).withHour(8).withMinute(0);
            txtThoiGianBatDau.setText(bd.format(F));
            txtThoiGianKetThuc.setText(bd.plusHours(3).format(F));
            txtDonViToChuc.setText("BCH Liên chi đoàn CNS");
        }

        luu.addActionListener(e -> {
            JsonObject body = Api.body("tenSuKien", txtTenSuKien.getText().trim(), "diaDiem", txtDiaDiem.getText().trim(),
                    "thoiGianBatDau", txtThoiGianBatDau.getText().trim(), "thoiGianKetThuc", txtThoiGianKetThuc.getText().trim(),
                    "soLuongToiDa", txtSoLuongToiDa.getText().trim(), "donViToChuc", txtDonViToChuc.getText().trim(),
                    "moTa", txtMoTa.getText().trim());
            if (laSua) body.addProperty("trangThai", (String) cboTrangThai.getSelectedItem());
            luu.setEnabled(false);
            guiForm(d, luu, loi, () -> laSua ? Api.put("/api/admin/su-kien/" + Api.n(sk, "id"), body)
                            : Api.post("/api/admin/su-kien?nguoiTao=" + Api.e(nguoiTao), body),
                    () -> {
                        HopThoai.thanhCong(cha, laSua ? "Cập nhật sự kiện thành công" : "Tạo sự kiện thành công");
                        xongThi.run();
                    });
        });
        d.hien();
    }

    /** Gửi form; lỗi từ BE hiện ngay trong form (không đóng form, không mất dữ liệu đã nhập). */
    static void guiForm(JDialog d, JButton nut, JLabel loi, Api.Viec<?> viec, Runnable xong) {
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() {
                try {
                    viec.chay();
                    return null;
                } catch (Exception ex) {
                    return ex.getMessage();
                }
            }

            @Override
            protected void done() {
                nut.setEnabled(true);
                String l;
                try {
                    l = get();
                } catch (Exception ex) {
                    l = ex.getMessage();
                }
                if (l != null) {
                    loi.setText(l);
                    return;
                }
                d.dispose();
                xong.run();
            }
        }.execute();
    }

    // ======================= DUYỆT KẾ HOẠCH =======================
    static void duyetKeHoach(Component cha, long id, Runnable xongThi) {
        Api.chayNen(cha, () -> Api.get("/api/admin/su-kien/" + id), kq -> {
            JsonObject sk = Api.obj(kq);
            HopThoai.FormDialog d = new HopThoai.FormDialog(cha, "Duyệt kế hoạch sự kiện",
                    "Người lập: " + Api.s(sk, "nguoiTao") + "  |  Mã: " + Api.s(sk, "maSuKien"), 600);
            d.truong("Tên sự kiện", HopThoai.oChiXem(Api.s(sk, "tenSuKien")), true);
            d.truong("Bắt đầu", HopThoai.oChiXem(Api.s(sk, "thoiGianBatDau")), false);
            d.truong("Kết thúc", HopThoai.oChiXem(Api.s(sk, "thoiGianKetThuc")), false);
            d.truong("Địa điểm", HopThoai.oChiXem(Api.s(sk, "diaDiem")), false);
            d.truong("Số lượng tối đa", HopThoai.oChiXem(String.valueOf(Api.n(sk, "soLuongToiDa"))), false);
            JTextArea mt = UI.vungChu(4);
            mt.setText(Api.s(sk, "moTa"));
            mt.setEditable(false);
            mt.setBackground(Theme.TABLE_HEAD);
            d.truong("Nội dung kế hoạch", UI.cuonVungChu(mt), true);

            UI.FlatButton dong = UI.nut("Đóng", UI.Kieu.VIEN);
            dong.addActionListener(e -> d.dispose());
            UI.FlatButton tuChoi = UI.nut("Từ chối", Icons.X, UI.Kieu.NGUY_HIEM);
            tuChoi.addActionListener(e -> {
                String ld = HopThoai.nhapLyDo(d, "Từ chối kế hoạch", "VD: trùng lịch thi, thiếu kinh phí");
                if (ld == null) return;
                Api.chayNen(d, () -> Api.put("/api/admin/su-kien/" + id + "/tu-choi", Api.body("lyDo", ld)), r -> {
                    d.dispose();
                    HopThoai.thanhCong(cha, "Đã từ chối kế hoạch");
                    xongThi.run();
                });
            });
            UI.FlatButton duyet = UI.nut("Duyệt kế hoạch", Icons.CHECK, UI.Kieu.THANH_CONG);
            duyet.addActionListener(e -> Api.chayNen(d, () -> Api.put("/api/admin/su-kien/" + id + "/duyet", null), r -> {
                d.dispose();
                HopThoai.thanhCong(cha, "Duyệt kế hoạch thành công");
                xongThi.run();
            }));
            d.nut(dong);
            d.nut(tuChoi);
            d.nut(duyet);
            d.hien();
        });
    }

    // ======================= THEO DÕI SỰ KIỆN =======================
    static void theoDoi(Component cha, long id) {
        Api.chayNen(cha, () -> Api.get("/api/admin/su-kien/" + id + "/theo-doi"), kq -> {
            JsonObject o = Api.obj(kq);
            HopThoai.FormDialog d = new HopThoai.FormDialog(cha, Api.s(o, "tenSuKien"),
                    Api.s(o, "maSuKien") + "  |  " + Api.s(o, "thoiGianBatDau") + " - " + Api.s(o, "thoiGianKetThuc")
                            + "  |  " + Api.s(o, "diaDiem"), 640);

            // Dòng trạng thái
            UI.Badge b1 = new UI.Badge();
            b1.setText(Api.s(o, "trangThai"));
            d.khoi(UI.hang(10, b1, UI.nhan(Api.s(o, "tienDoThoiGian"), Theme.BODY_BOLD, Theme.TEXT_2)));

            // Tiến độ đăng ký
            long dk = Api.n(o, "soDangKy"), toiDa = Math.max(1, Api.n(o, "soLuongToiDa"));
            JPanel tienDo = new JPanel(new BorderLayout(0, 6));
            tienDo.setOpaque(false);
            tienDo.add(UI.nhan(dk + " / " + Api.n(o, "soLuongToiDa") + " chỗ  (" + Math.round(dk * 100.0 / toiDa) + "%)",
                    Theme.BODY_BOLD, Theme.TEXT), BorderLayout.NORTH);
            tienDo.add(UI.thanhTienDo(dk / (double) toiDa, dk >= toiDa ? Theme.DANGER : Theme.PRIMARY), BorderLayout.CENTER);
            d.truong("Tiến độ đăng ký", tienDo, true);

            // 4 ô số nhỏ
            JPanel o4 = new JPanel(new GridLayout(1, 4, 10, 0));
            o4.setOpaque(false);
            o4.add(oSo("Đã tham gia", String.valueOf(Api.n(o, "soThamGia")), Theme.SUCCESS));
            o4.add(oSo("Vắng mặt", String.valueOf(Api.n(o, "soVangMat")), Theme.DANGER));
            o4.add(oSo("Đã hủy đăng ký", String.valueOf(Api.n(o, "soHuy")), Theme.MUTED));
            o4.add(oSo("Đánh giá TB", Api.n(o, "soDanhGia") == 0 ? "–"
                    : Api.d(o, "diemTrungBinh") + " ★ (" + Api.n(o, "soDanhGia") + ")", Theme.WARNING));
            d.khoi(o4);

            // Cán bộ phụ trách
            BangDep bpc = new BangDep("Cán bộ", "Nhiệm vụ", "Ngày phân công");
            bpc.cotHaiDong(0);
            for (JsonElement el : Api.arr(o, "phanCong")) {
                JsonObject p = el.getAsJsonObject();
                bpc.themDong(Api.s(p, "hoTen") + "\n" + Api.s(p, "email"), Api.s(p, "vaiTroPhuTrach"), Api.s(p, "ngayPhanCong"));
            }
            JScrollPane sp = bpc.trongKhungCuon();
            sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
            sp.setPreferredSize(new Dimension(580, 42 + 46 * Math.max(1, bpc.getRowCount()) + 2));
            d.truong("Cán bộ phụ trách (" + bpc.getRowCount() + ")", bpc.getRowCount() == 0
                    ? UI.nhan("Chưa phân công cán bộ nào", Theme.SMALL, Theme.MUTED) : sp, true);

            UI.FlatButton dong = UI.nut("Đóng", UI.Kieu.CHINH);
            dong.addActionListener(e -> d.dispose());
            d.nut(dong);
            d.hien();
        });
    }

    private static JComponent oSo(String nhan, String so, Color mau) {
        UI.RoundPanel p = new UI.RoundPanel(new GridLayout(2, 1, 0, 2), 10, Theme.TABLE_HEAD, Theme.BORDER);
        p.setBorder(new EmptyBorder(10, 12, 10, 12));
        p.add(UI.nhan(nhan, Theme.SMALL, Theme.MUTED));
        p.add(UI.nhan(so, Theme.font(Font.BOLD, 17), mau));
        return p;
    }

    // ======================= PHÂN CÔNG CÁN BỘ =======================
    static void phanCong(Component cha, JsonObject sk, Runnable xongThi) {
        long id = Api.n(sk, "id");
        HopThoai.FormDialog d = new HopThoai.FormDialog(cha, "Phân công cán bộ phụ trách", Api.s(sk, "tenSuKien"), 640);

        JComboBox<String> cbCanBo = UI.hopChon(new String[]{"Đang tải..."}, Icons.USER);
        List<String> maCanBo = new ArrayList<>();
        JComboBox<String> cbNhiemVu = UI.hopChon(new String[]{"Trưởng ban tổ chức", "Hậu cần", "Truyền thông",
                "Lễ tân - Đón tiếp", "Kỹ thuật - Âm thanh", "Điểm danh"}, null);
        cbNhiemVu.setEditable(true);
        cbCanBo.setPreferredSize(new Dimension(250, 38));
        cbNhiemVu.setPreferredSize(new Dimension(200, 38));
        UI.FlatButton them = UI.nut("Phân công", Icons.PLUS, UI.Kieu.CHINH);
        d.truong("Thêm cán bộ", UI.hang(8, cbCanBo, cbNhiemVu, them), true);

        BangDep b = new BangDep("id", "Cán bộ", "Nhiệm vụ", "Ngày phân công", "");
        b.anCot(0);
        b.cotHaiDong(1);
        b.coDinhCot(4, 90);
        JScrollPane sp = b.trongKhungCuon();
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.setPreferredSize(new Dimension(580, 240));
        d.truong("Đã phân công", sp, true);

        Runnable taiDs = () -> Api.chayNen(d, () -> Api.get("/api/admin/su-kien/" + id + "/phan-cong"), kq -> {
            b.xoaHet();
            for (JsonElement el : Api.arr(kq)) {
                JsonObject p = el.getAsJsonObject();
                b.themDong(Api.n(p, "id"), Api.s(p, "hoTen") + "\n" + Api.s(p, "tenDangNhap"),
                        Api.s(p, "vaiTroPhuTrach"), Api.s(p, "ngayPhanCong"), "");
            }
        });
        b.cotThaoTac(4, List.of(new BangDep.NutThaoTac("huy", null, Icons.TRASH, "Hủy phân công")), null, (ma, dong) -> {
            long pcId = ((Number) b.getModelBang().getValueAt(dong, 0)).longValue();
            if (!HopThoai.xacNhan(d, "Hủy phân công", "Bỏ cán bộ này khỏi sự kiện?", "Hủy phân công", UI.Kieu.NGUY_HIEM)) return;
            Api.chayNen(d, () -> Api.delete("/api/admin/su-kien/phan-cong/" + pcId), r -> {
                taiDs.run();
                xongThi.run();
            });
        });

        them.addActionListener(e -> {
            int i = cbCanBo.getSelectedIndex();
            if (i < 0 || i >= maCanBo.size()) return;
            Object nv = cbNhiemVu.getEditor().getItem();
            Api.chayNen(d, () -> Api.post("/api/admin/su-kien/" + id + "/phan-cong",
                    Api.body("tenDangNhap", maCanBo.get(i), "vaiTroPhuTrach", nv == null ? "" : nv.toString().trim())), r -> {
                HopThoai.thanhCong(cha, "Phân công thành công");
                taiDs.run();
                xongThi.run();
            });
        });

        Api.chayNen(d, () -> Api.get("/api/admin/can-bo"), kq -> {
            cbCanBo.removeAllItems();
            JsonArray ds = Api.arr(kq);
            for (JsonElement el : ds) {
                JsonObject cb = el.getAsJsonObject();
                maCanBo.add(Api.s(cb, "tenDangNhap"));
                cbCanBo.addItem(Api.s(cb, "hoTen") + " (" + Api.s(cb, "tenDangNhap") + ")");
            }
        });
        taiDs.run();

        UI.FlatButton dong = UI.nut("Xong", UI.Kieu.CHINH);
        dong.addActionListener(e -> d.dispose());
        d.nut(dong);
        d.hien();
    }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField txtTenSuKien;
    private javax.swing.JTextField txtThoiGianBatDau;
    private javax.swing.JTextField txtThoiGianKetThuc;
    private javax.swing.JTextField txtDiaDiem;
    private javax.swing.JTextField txtSoLuongToiDa;
    private javax.swing.JTextField txtDonViToChuc;
    private javax.swing.JPanel nhomTrangThai;
    private javax.swing.JComboBox<String> cboTrangThai;
    private javax.swing.JTextArea txtMoTa;
    private javax.swing.JLabel loi;
    private javax.swing.JButton huy;
    private javax.swing.JButton luu;

    private javax.swing.JPanel pnlThongTin;
    private javax.swing.JPanel pnlNhapLieu;
    private javax.swing.JPanel pnlNutChucNang;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongTin = new javax.swing.JPanel();
        pnlThongTin.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlThongTin.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(pnlThongTin, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        javax.swing.JPanel panela317 = new javax.swing.JPanel();
        panela317.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela317.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongTin.add(panela317, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela307 = new javax.swing.JPanel();
        panela307.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela307.setBackground(new java.awt.Color(0xFFFFFF));
        panela317.add(panela307, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela306 = new javax.swing.JLabel();
        panela306.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela306.setForeground(new java.awt.Color(0x243B53));
        panela306.setText("Tên sự kiện *");
        panela307.add(panela306, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtTenSuKien.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTenSuKien.setToolTipText("Nhập tên sự kiện");
        txtTenSuKien.setPreferredSize(new java.awt.Dimension(180, 32));
        txtTenSuKien.setMinimumSize(new java.awt.Dimension(50, 32));
        panela307.add(txtTenSuKien, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela310 = new javax.swing.JPanel();
        panela310.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela310.setBackground(new java.awt.Color(0xFFFFFF));
        panela317.add(panela310, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela309 = new javax.swing.JLabel();
        panela309.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela309.setForeground(new java.awt.Color(0x243B53));
        panela309.setText("Thời gian bắt đầu *");
        panela310.add(panela309, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtThoiGianBatDau.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtThoiGianBatDau.setToolTipText("dd/MM/yyyy HH:mm");
        txtThoiGianBatDau.setPreferredSize(new java.awt.Dimension(180, 32));
        txtThoiGianBatDau.setMinimumSize(new java.awt.Dimension(50, 32));
        panela310.add(txtThoiGianBatDau, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela313 = new javax.swing.JPanel();
        panela313.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela313.setBackground(new java.awt.Color(0xFFFFFF));
        panela317.add(panela313, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela312 = new javax.swing.JLabel();
        panela312.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela312.setForeground(new java.awt.Color(0x243B53));
        panela312.setText("Thời gian kết thúc *");
        panela313.add(panela312, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtThoiGianKetThuc.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtThoiGianKetThuc.setToolTipText("dd/MM/yyyy HH:mm");
        txtThoiGianKetThuc.setPreferredSize(new java.awt.Dimension(180, 32));
        txtThoiGianKetThuc.setMinimumSize(new java.awt.Dimension(50, 32));
        panela313.add(txtThoiGianKetThuc, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela316 = new javax.swing.JPanel();
        panela316.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela316.setBackground(new java.awt.Color(0xFFFFFF));
        panela317.add(panela316, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela315 = new javax.swing.JLabel();
        panela315.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela315.setForeground(new java.awt.Color(0x243B53));
        panela315.setText("Địa điểm *");
        panela316.add(panela315, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtDiaDiem.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtDiaDiem.setToolTipText("Ví dụ: Hội trường A");
        txtDiaDiem.setPreferredSize(new java.awt.Dimension(180, 32));
        txtDiaDiem.setMinimumSize(new java.awt.Dimension(50, 32));
        panela316.add(txtDiaDiem, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela327 = new javax.swing.JPanel();
        panela327.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela327.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongTin.add(panela327, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela320 = new javax.swing.JPanel();
        panela320.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela320.setBackground(new java.awt.Color(0xFFFFFF));
        panela327.add(panela320, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela319 = new javax.swing.JLabel();
        panela319.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela319.setForeground(new java.awt.Color(0x243B53));
        panela319.setText("Số lượng tối đa *");
        panela320.add(panela319, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtSoLuongToiDa.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtSoLuongToiDa.setToolTipText("Ví dụ: 200");
        txtSoLuongToiDa.setPreferredSize(new java.awt.Dimension(180, 32));
        txtSoLuongToiDa.setMinimumSize(new java.awt.Dimension(50, 32));
        panela320.add(txtSoLuongToiDa, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela323 = new javax.swing.JPanel();
        panela323.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela323.setBackground(new java.awt.Color(0xFFFFFF));
        panela327.add(panela323, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela322 = new javax.swing.JLabel();
        panela322.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela322.setForeground(new java.awt.Color(0x243B53));
        panela322.setText("Đơn vị tổ chức");
        panela323.add(panela322, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtDonViToChuc.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtDonViToChuc.setToolTipText("Tên đơn vị tổ chức");
        txtDonViToChuc.setPreferredSize(new java.awt.Dimension(180, 32));
        txtDonViToChuc.setMinimumSize(new java.awt.Dimension(50, 32));
        panela323.add(txtDonViToChuc, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        nhomTrangThai = new javax.swing.JPanel();
        nhomTrangThai.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        nhomTrangThai.setBackground(new java.awt.Color(0xFFFFFF));
        panela327.add(nhomTrangThai, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela325 = new javax.swing.JLabel();
        panela325.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela325.setForeground(new java.awt.Color(0x243B53));
        panela325.setText("Trạng thái");
        nhomTrangThai.add(panela325, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        cboTrangThai = new javax.swing.JComboBox();
        cboTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Sắp diễn ra", "Đang diễn ra", "Đã kết thúc", "Đã hủy"}));
        cboTrangThai.setPreferredSize(new java.awt.Dimension(175, 32));
        cboTrangThai.setMinimumSize(new java.awt.Dimension(110, 32));
        nhomTrangThai.add(cboTrangThai, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        pnlNhapLieu = new javax.swing.JPanel();
        pnlNhapLieu.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlNhapLieu.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(pnlNhapLieu, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        javax.swing.JLabel panela331 = new javax.swing.JLabel();
        panela331.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela331.setForeground(new java.awt.Color(0x243B53));
        panela331.setText("Mô tả");
        pnlNhapLieu.add(panela331, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JScrollPane panela330 = new javax.swing.JScrollPane();
        pnlNhapLieu.add(panela330, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtMoTa = new javax.swing.JTextArea();
        txtMoTa.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtMoTa.setRows(4);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        panela330.setViewportView(txtMoTa);
        pnlNutChucNang = new javax.swing.JPanel();
        pnlNutChucNang.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlNutChucNang.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(pnlNutChucNang, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        loi = new javax.swing.JLabel();
        loi.setFont(new java.awt.Font("Segoe UI", 0, 12));
        loi.setForeground(new java.awt.Color(0xDC3545));
        loi.setText(" ");
        pnlNutChucNang.add(loi, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela336 = new javax.swing.JPanel();
        panela336.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela336.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNutChucNang.add(panela336, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        huy = new javax.swing.JButton();
        huy.setFont(new java.awt.Font("Segoe UI", 0, 13));
        huy.setBackground(new java.awt.Color(0xFFFFFF));
        huy.setForeground(new java.awt.Color(0x243B53));
        huy.setFocusPainted(false);
        huy.setPreferredSize(new java.awt.Dimension(130, 30));
        huy.setText("Hủy");
        panela336.add(huy, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, null, null, null));
        luu = new javax.swing.JButton();
        luu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        luu.setBackground(new java.awt.Color(0x1565C0));
        luu.setForeground(new java.awt.Color(0xFFFFFF));
        luu.setFocusPainted(false);
        luu.setContentAreaFilled(false);
        luu.setOpaque(true);
        luu.setBorderPainted(false);
        luu.setPreferredSize(new java.awt.Dimension(130, 30));
        luu.setText("Tạo sự kiện");
        panela336.add(luu, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 4, 0, 0, 0, null, null, null));
    }

    private void createUIComponents() {
        txtDonViToChuc = UI.oNhap("Tên đơn vị tổ chức");
        txtSoLuongToiDa = UI.oNhap("Ví dụ: 200");
        txtDiaDiem = UI.oNhap("Ví dụ: Hội trường A");
        txtThoiGianKetThuc = UI.oNhap("dd/MM/yyyy HH:mm");
        txtThoiGianBatDau = UI.oNhap("dd/MM/yyyy HH:mm");
        txtTenSuKien = UI.oNhap("Nhập tên sự kiện");
    }
}
