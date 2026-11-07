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

        UI.TextFieldDep oTen = UI.oNhap("VD: Ngày hội hiến máu tình nguyện");
        UI.TextFieldDep oDiaDiem = UI.oNhap("VD: Hội trường tầng 5 nhà M");
        UI.TextFieldDep oBatDau = UI.oNhap("dd/MM/yyyy HH:mm");
        UI.TextFieldDep oKetThuc = UI.oNhap("dd/MM/yyyy HH:mm");
        UI.TextFieldDep oSoLuong = UI.oNhap("VD: 200");
        UI.TextFieldDep oDonVi = UI.oNhap("VD: BCH Liên chi đoàn CNTT");
        JComboBox<String> cbTrangThai = UI.hopChon(new String[]{"Sắp diễn ra", "Đang diễn ra", "Đã kết thúc", "Đã hủy"}, null);
        JTextArea oMoTa = UI.vungChu(4);

        if (laSua) {
            oTen.setText(Api.s(sk, "tenSuKien"));
            oDiaDiem.setText(Api.s(sk, "diaDiem"));
            oBatDau.setText(Api.s(sk, "thoiGianBatDau"));
            oKetThuc.setText(Api.s(sk, "thoiGianKetThuc"));
            oSoLuong.setText(String.valueOf(Api.n(sk, "soLuongToiDa")));
            oDonVi.setText(Api.s(sk, "donViToChuc"));
            cbTrangThai.setSelectedItem(Api.s(sk, "trangThai"));
            oMoTa.setText(Api.s(sk, "moTa"));
        } else {
            // Gợi ý sẵn: 7 ngày nữa, 8h - 11h
            LocalDateTime bd = LocalDateTime.now().plusDays(7).withHour(8).withMinute(0);
            oBatDau.setText(bd.format(F));
            oKetThuc.setText(bd.plusHours(3).format(F));
            oDonVi.setText("BCH Liên chi đoàn CNTT");
        }

        d.truong("Tên sự kiện *", oTen, true);
        d.truong("Thời gian bắt đầu *", oBatDau, false);
        d.truong("Thời gian kết thúc *", oKetThuc, false);
        d.truong("Địa điểm *", oDiaDiem, false);
        d.truong("Số lượng tối đa *", oSoLuong, false);
        d.truong("Đơn vị tổ chức", oDonVi, false);
        if (laSua) d.truong("Trạng thái", cbTrangThai, false);
        d.truong("Mô tả", UI.cuonVungChu(oMoTa), true);
        JLabel loi = UI.nhan(" ", Theme.SMALL, Theme.DANGER);
        d.khoi(loi);

        UI.FlatButton huy = UI.nut("Hủy", UI.Kieu.VIEN);
        huy.addActionListener(e -> d.dispose());
        UI.FlatButton luu = UI.nut(laSua ? "Lưu" : "Tạo sự kiện", Icons.CHECK, UI.Kieu.CHINH);
        luu.addActionListener(e -> {
            JsonObject body = Api.body("tenSuKien", oTen.getText().trim(), "diaDiem", oDiaDiem.getText().trim(),
                    "thoiGianBatDau", oBatDau.getText().trim(), "thoiGianKetThuc", oKetThuc.getText().trim(),
                    "soLuongToiDa", oSoLuong.getText().trim(), "donViToChuc", oDonVi.getText().trim(),
                    "moTa", oMoTa.getText().trim());
            if (laSua) body.addProperty("trangThai", (String) cbTrangThai.getSelectedItem());
            luu.setEnabled(false);
            guiForm(d, luu, loi, () -> laSua ? Api.put("/api/admin/su-kien/" + Api.n(sk, "id"), body)
                            : Api.post("/api/admin/su-kien?nguoiTao=" + Api.e(nguoiTao), body),
                    () -> {
                        HopThoai.thanhCong(cha, laSua ? "Cập nhật sự kiện thành công" : "Tạo sự kiện thành công");
                        xongThi.run();
                    });
        });
        d.nut(huy);
        d.nut(luu);
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
}
