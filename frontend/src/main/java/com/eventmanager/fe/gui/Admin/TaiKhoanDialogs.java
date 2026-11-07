package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Các hộp thoại của màn hình Quản lý tài khoản:
 *  - formTaiKhoan : Tạo mới / Sửa thông tin tài khoản (frmTaotaikhoan)
 *  - chiTiet      : Xem chi tiết 1 tài khoản
 *  - datLaiMatKhau: Đặt lại mật khẩu
 *  - duyetYeuCau  : So sánh thông tin hiện tại - đề xuất, Chấp nhận / Từ chối (frmDuyetThayDoi)
 */
final class TaiKhoanDialogs {

    private static final String[] VAI_TRO_HIEN = {"Sinh viên", "Cán bộ sự kiện", "Quản trị viên"};
    private static final String[] VAI_TRO_MA = {"HOC_SINH", "CAN_BO", "ADMIN"};

    private TaiKhoanDialogs() {
    }

    // ======================= TẠO / SỬA TÀI KHOẢN =======================
    static void formTaiKhoan(Component cha, JsonObject tk, Runnable xongThi) {
        boolean laSua = tk != null;
        HopThoai.FormDialog d = new HopThoai.FormDialog(cha, laSua ? "Sửa thông tin tài khoản" : "Tạo tài khoản mới",
                laSua ? "Tên đăng nhập và vai trò không đổi ở đây (đổi vai trò tại mục Phân quyền)"
                        : "Các trường có dấu * là bắt buộc", 560);

        UI.TextFieldDep oTenDn = UI.oNhap("VD: nguyenvana");
        JPasswordField oMk = UI.oMatKhau();
        JPasswordField oMk2 = UI.oMatKhau();
        JComboBox<String> cbVaiTro = UI.hopChon(VAI_TRO_HIEN, null);
        UI.TextFieldDep oHoTen = UI.oNhap("VD: Nguyễn Văn A");
        UI.TextFieldDep oEmail = UI.oNhap("VD: ten@epu.edu.vn");
        UI.TextFieldDep oMaSo = UI.oNhap("VD: D19CNPM3 hoặc NV005");
        UI.TextFieldDep oSdt = UI.oNhap("10 số, bắt đầu bằng 0");

        if (laSua) {
            d.truong("Tên đăng nhập", HopThoai.oChiXem(Api.s(tk, "tenDangNhap")), false);
            d.truong("Vai trò", HopThoai.oChiXem(Api.s(tk, "tenVaiTro")), false);
            oHoTen.setText(Api.s(tk, "hoTen"));
            oEmail.setText(Api.s(tk, "email"));
            oMaSo.setText(Api.s(tk, "maSo"));
            oSdt.setText(Api.s(tk, "soDienThoai"));
        } else {
            d.truong("Tên đăng nhập *", oTenDn, false);
            d.truong("Vai trò *", cbVaiTro, false);
            d.truong("Mật khẩu *", oMk, false);
            d.truong("Nhập lại mật khẩu *", oMk2, false);
        }
        d.truong("Họ và tên *", oHoTen, true);
        d.truong("Email *", oEmail, false);
        d.truong("Mã SV / Mã NV", oMaSo, false);
        d.truong("Số điện thoại", oSdt, false);

        JLabel loi = UI.nhan(" ", Theme.SMALL, Theme.DANGER);
        d.khoi(loi);

        UI.FlatButton huy = UI.nut("Hủy", UI.Kieu.VIEN);
        huy.addActionListener(e -> d.dispose());
        UI.FlatButton luu = UI.nut(laSua ? "Lưu thay đổi" : "Tạo tài khoản", laSua ? Icons.CHECK : Icons.USER_PLUS, UI.Kieu.CHINH);
        luu.addActionListener(e -> {
            // Kiểm tra nhanh ở FE cho người dùng thấy lỗi ngay; BE vẫn kiểm tra lại lần nữa
            String mk = new String(oMk.getPassword());
            if (!laSua) {
                if (oTenDn.getText().trim().isEmpty()) { loi.setText("Vui lòng nhập tên đăng nhập"); return; }
                if (mk.length() < 6) { loi.setText("Mật khẩu phải có ít nhất 6 ký tự"); return; }
                if (!mk.equals(new String(oMk2.getPassword()))) { loi.setText("Hai lần nhập mật khẩu không khớp"); return; }
            }
            if (oHoTen.getText().trim().isEmpty()) { loi.setText("Vui lòng nhập họ và tên"); return; }
            if (oEmail.getText().trim().isEmpty()) { loi.setText("Vui lòng nhập email"); return; }

            JsonObject body = Api.body("hoTen", oHoTen.getText().trim(), "email", oEmail.getText().trim(),
                    "maSo", oMaSo.getText().trim(), "soDienThoai", oSdt.getText().trim());
            if (!laSua) {
                body.addProperty("tenDangNhap", oTenDn.getText().trim());
                body.addProperty("matKhau", mk);
                body.addProperty("vaiTro", VAI_TRO_MA[cbVaiTro.getSelectedIndex()]);
            }
            luu.setEnabled(false);
            loi.setText(" ");
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    try {
                        if (laSua) Api.put("/api/admin/tai-khoan/" + Api.n(tk, "id"), body);
                        else Api.post("/api/admin/tai-khoan", body);
                        return null;
                    } catch (Exception ex) {
                        return ex.getMessage();
                    }
                }

                @Override
                protected void done() {
                    luu.setEnabled(true);
                    try {
                        String thongBaoLoi = get();
                        if (thongBaoLoi != null) {
                            loi.setText(thongBaoLoi); // lỗi hiện ngay trong form, không mất dữ liệu đã nhập
                            return;
                        }
                    } catch (Exception ex) {
                        loi.setText(ex.getMessage());
                        return;
                    }
                    d.dispose();
                    HopThoai.thanhCong(cha, laSua ? "Cập nhật thành công" : "Tạo tài khoản thành công");
                    xongThi.run();
                }
            }.execute();
        });
        d.nut(huy);
        d.nut(luu);
        d.getRootPane().setDefaultButton(luu);
        d.hien();
    }

    // ======================= XEM CHI TIẾT =======================
    static void chiTiet(Component cha, JsonObject tk, TrangAdmin.DieuHuong dieuHuong) {
        HopThoai.FormDialog d = new HopThoai.FormDialog(cha, "Chi tiết tài khoản", null, 520);

        // Khối đầu: avatar + tên + 2 nhãn
        JPanel dau = new JPanel(new BorderLayout(14, 0));
        dau.setOpaque(false);
        dau.setBorder(new EmptyBorder(0, 0, 6, 0));
        dau.add(new JLabel(UI.avatar(UI.vietTat(Api.s(tk, "hoTen")), 54, Theme.PRIMARY)), BorderLayout.WEST);
        JPanel chu = new JPanel();
        chu.setOpaque(false);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        chu.add(UI.nhan(Api.s(tk, "hoTen"), Theme.H2, Theme.TEXT));
        chu.add(Box.createVerticalStrut(6));
        UI.Badge vt = new UI.Badge();
        vt.setText(Api.s(tk, "tenVaiTro"));
        UI.Badge tt = new UI.Badge();
        tt.setText("BI_KHOA".equals(Api.s(tk, "trangThai")) ? "Bị khóa" : "Hoạt động");
        JPanel nhan = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        nhan.setOpaque(false);
        nhan.add(vt);
        nhan.add(Box.createHorizontalStrut(8));
        nhan.add(tt);
        nhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        chu.add(nhan);
        dau.add(chu, BorderLayout.CENTER);
        d.khoi(dau);

        d.truong("Tên đăng nhập", HopThoai.oChiXem(Api.s(tk, "tenDangNhap")), false);
        d.truong("Mã SV / Mã NV", HopThoai.oChiXem(Api.s(tk, "maSo")), false);
        d.truong("Email", HopThoai.oChiXem(Api.s(tk, "email")), false);
        d.truong("Số điện thoại", HopThoai.oChiXem(Api.s(tk, "soDienThoai")), false);
        d.truong("Ngày tạo", HopThoai.oChiXem(Api.s(tk, "ngayTao")), false);

        UI.FlatButton phanQuyen = UI.nut("Xem quyền", Icons.SHIELD, UI.Kieu.VIEN);
        phanQuyen.addActionListener(e -> {
            d.dispose();
            dieuHuong.phanQuyenCho(Api.n(tk, "id"));
        });
        UI.FlatButton dong = UI.nut("Đóng", UI.Kieu.CHINH);
        dong.addActionListener(e -> d.dispose());
        d.nut(phanQuyen);
        d.nut(dong);
        d.hien();
    }

    // ======================= ĐẶT LẠI MẬT KHẨU =======================
    static void datLaiMatKhau(Component cha, JsonObject tk) {
        HopThoai.FormDialog d = new HopThoai.FormDialog(cha, "Đặt lại mật khẩu",
                "Tài khoản: " + Api.s(tk, "tenDangNhap") + " - " + Api.s(tk, "hoTen"), 440);
        JPasswordField m1 = UI.oMatKhau();
        JPasswordField m2 = UI.oMatKhau();
        d.truong("Mật khẩu mới *", m1, true);
        d.truong("Nhập lại mật khẩu mới *", m2, true);
        JLabel loi = UI.nhan(" ", Theme.SMALL, Theme.DANGER);
        d.khoi(loi);
        UI.FlatButton huy = UI.nut("Hủy", UI.Kieu.VIEN);
        huy.addActionListener(e -> d.dispose());
        UI.FlatButton ok = UI.nut("Đặt lại", Icons.KEY, UI.Kieu.CHINH);
        ok.addActionListener(e -> {
            String a = new String(m1.getPassword());
            if (a.length() < 6) { loi.setText("Mật khẩu phải có ít nhất 6 ký tự"); return; }
            if (!a.equals(new String(m2.getPassword()))) { loi.setText("Hai lần nhập không khớp"); return; }
            Api.chayNen(d, () -> Api.put("/api/admin/tai-khoan/" + Api.n(tk, "id") + "/dat-lai-mat-khau",
                    Api.body("matKhauMoi", a)), kq -> {
                d.dispose();
                HopThoai.thanhCong(cha, "Đã đặt lại mật khẩu");
            });
        });
        d.nut(huy);
        d.nut(ok);
        d.hien();
    }

    // ======================= DUYỆT YÊU CẦU THAY ĐỔI THÔNG TIN =======================
    static void duyetYeuCau(Component cha, long id, Runnable xongThi) {
        Api.chayNen(cha, () -> Api.get("/api/admin/yeu-cau/" + id), kq -> hienDuyetYeuCau(cha, Api.obj(kq), xongThi));
    }

    private static void hienDuyetYeuCau(Component cha, JsonObject yc, Runnable xongThi) {
        boolean choDuyet = "Chờ duyệt".equals(Api.s(yc, "trangThai"));
        HopThoai.FormDialog d = new HopThoai.FormDialog(cha, "Yêu cầu thay đổi thông tin",
                Api.s(yc, "hoTen") + " (" + Api.s(yc, "tenDangNhap") + ") - gửi lúc " + Api.s(yc, "ngayGui"), 620);

        // Bảng so sánh: Trường | Hiện tại | Đề xuất
        BangDep ss = new BangDep("Thông tin", "Hiện tại", "Đề xuất thay đổi");
        for (JsonElement el : Api.arr(yc, "soSanh")) {
            JsonObject o = el.getAsJsonObject();
            ss.themDong(Api.s(o, "truong"), Api.s(o, "hienTai").isEmpty() ? "(trống)" : Api.s(o, "hienTai"),
                    Api.s(o, "deXuat"));
        }
        ss.setRowHeight(40);
        ss.getColumnModel().getColumn(2).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = UI.nhan(v == null ? "" : v.toString(), Theme.BODY_BOLD, Theme.SUCCESS);
            l.setOpaque(true);
            l.setBackground(new Color(0xF3FBF6));
            l.setBorder(new EmptyBorder(0, 14, 0, 8));
            return l;
        });
        JScrollPane sp = ss.trongKhungCuon();
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        sp.setPreferredSize(new Dimension(560, 42 + 40 * Math.max(1, ss.getRowCount()) + 2));
        d.truong("So sánh thông tin", sp, true);

        JTextArea lyDo = UI.vungChu(2);
        lyDo.setText(Api.s(yc, "lyDo").isEmpty() ? "(Không ghi lý do)" : Api.s(yc, "lyDo"));
        lyDo.setEditable(false);
        lyDo.setBackground(Theme.TABLE_HEAD);
        d.truong("Lý do / minh chứng của người gửi", UI.cuonVungChu(lyDo), true);

        if (!choDuyet) {
            UI.Badge b = new UI.Badge();
            b.setText(Api.s(yc, "trangThai"));
            JPanel kq = UI.hang(8, b, UI.nhan("Xử lý lúc " + Api.s(yc, "ngayXuLy")
                    + (Api.s(yc, "lyDoTuChoi").isEmpty() ? "" : " - Lý do: " + Api.s(yc, "lyDoTuChoi")), Theme.SMALL, Theme.TEXT_2));
            d.truong("Kết quả", kq, true);
        }

        UI.FlatButton dong = UI.nut("Đóng", UI.Kieu.VIEN);
        dong.addActionListener(e -> d.dispose());
        d.nut(dong);
        if (choDuyet) {
            long id = Api.n(yc, "id");
            UI.FlatButton tuChoi = UI.nut("Từ chối", Icons.X, UI.Kieu.NGUY_HIEM);
            tuChoi.addActionListener(e -> {
                String ld = HopThoai.nhapLyDo(d, "Từ chối yêu cầu", "VD: minh chứng chưa hợp lệ");
                if (ld == null) return;
                Api.chayNen(d, () -> Api.put("/api/admin/yeu-cau/" + id + "/tu-choi", Api.body("lyDo", ld)), r -> {
                    d.dispose();
                    HopThoai.thanhCong(cha, "Đã từ chối yêu cầu");
                    xongThi.run();
                });
            });
            UI.FlatButton chapNhan = UI.nut("Chấp nhận", Icons.CHECK, UI.Kieu.THANH_CONG);
            chapNhan.addActionListener(e -> Api.chayNen(d, () -> Api.put("/api/admin/yeu-cau/" + id + "/chap-nhan", null), r -> {
                d.dispose();
                HopThoai.thanhCong(cha, "Thay đổi thông tin thành công");
                xongThi.run();
            }));
            d.nut(tuChoi);
            d.nut(chapNhan);
        }
        d.hien();
    }
}
