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
        $$$setupUI$$$();
    }

    // ======================= TẠO / SỬA TÀI KHOẢN =======================
    static void formTaiKhoan(Component cha, JsonObject tk, Runnable xongThi) {
        boolean laSua = tk != null;
        HopThoai.FormDialog d = new HopThoai.FormDialog(cha, laSua ? "Sửa thông tin tài khoản" : "Tạo tài khoản mới",
                laSua ? "Tên đăng nhập và vai trò không đổi ở đây (đổi vai trò tại mục Phân quyền)"
                        : "Các trường có dấu * là bắt buộc", 560);

        TaiKhoanDialogs view = new TaiKhoanDialogs();
        JTextField txtTenDangNhap = view.txtTenDangNhap;
        JTextField txtHoTen = view.txtHoTen;
        JTextField txtEmail = view.txtEmail;
        JTextField txtMaSo = view.txtMaSo;
        JTextField txtSoDienThoai = view.txtSoDienThoai;
        JPasswordField txtMatKhau = view.txtMatKhau;
        JPasswordField txtNhapLaiMatKhau = view.txtNhapLaiMatKhau;
        JComboBox<String> cboVaiTro = view.cboVaiTro;
        JLabel loi = view.loi;
        JButton huy = view.huy, luu = view.luu;
        view.nhomMatKhau.setVisible(!laSua);
        txtTenDangNhap.setEditable(!laSua);
        cboVaiTro.setEnabled(!laSua);
        if (laSua) {
            txtTenDangNhap.setText(Api.s(tk, "tenDangNhap"));
            cboVaiTro.setSelectedIndex(Math.max(0, java.util.Arrays.asList(VAI_TRO_MA).indexOf(Api.s(tk, "vaiTro"))));
            txtHoTen.setText(Api.s(tk, "hoTen"));
            txtEmail.setText(Api.s(tk, "email"));
            txtMaSo.setText(Api.s(tk, "maSo"));
            txtSoDienThoai.setText(Api.s(tk, "soDienThoai"));
        }
        luu.setText(laSua ? "Lưu thay đổi" : "Tạo tài khoản");
        huy.addActionListener(e -> d.dispose());
        d.khoi(view.mainPanel);
        luu.addActionListener(e -> {
            // Kiểm tra nhanh ở FE cho người dùng thấy lỗi ngay; BE vẫn kiểm tra lại lần nữa
            String mk = new String(txtMatKhau.getPassword());
            if (!laSua) {
                if (txtTenDangNhap.getText().trim().isEmpty()) { loi.setText("Vui lòng nhập tên đăng nhập"); return; }
                if (mk.length() < 6) { loi.setText("Mật khẩu phải có ít nhất 6 ký tự"); return; }
                if (!mk.equals(new String(txtNhapLaiMatKhau.getPassword()))) { loi.setText("Hai lần nhập mật khẩu không khớp"); return; }
            }
            if (txtHoTen.getText().trim().isEmpty()) { loi.setText("Vui lòng nhập họ và tên"); return; }
            if (txtEmail.getText().trim().isEmpty()) { loi.setText("Vui lòng nhập email"); return; }

            JsonObject body = Api.body("hoTen", txtHoTen.getText().trim(), "email", txtEmail.getText().trim(),
                    "maSo", txtMaSo.getText().trim(), "soDienThoai", txtSoDienThoai.getText().trim());
            if (!laSua) {
                body.addProperty("tenDangNhap", txtTenDangNhap.getText().trim());
                body.addProperty("matKhau", mk);
                body.addProperty("vaiTro", VAI_TRO_MA[cboVaiTro.getSelectedIndex()]);
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

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTextField txtTenDangNhap;
    private javax.swing.JComboBox<String> cboVaiTro;
    private javax.swing.JPanel nhomMatKhau;
    private javax.swing.JPasswordField txtMatKhau;
    private javax.swing.JPasswordField txtNhapLaiMatKhau;
    private javax.swing.JTextField txtHoTen;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtMaSo;
    private javax.swing.JTextField txtSoDienThoai;
    private javax.swing.JLabel loi;
    private javax.swing.JButton huy;
    private javax.swing.JButton luu;

    private javax.swing.JPanel pnlNhapLieu;
    private javax.swing.JPanel pnlNutChucNang;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNhapLieu = new javax.swing.JPanel();
        pnlNhapLieu.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlNhapLieu.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(pnlNhapLieu, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        javax.swing.JPanel panela284 = new javax.swing.JPanel();
        panela284.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela284.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNhapLieu.add(panela284, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela273 = new javax.swing.JPanel();
        panela273.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela273.setBackground(new java.awt.Color(0xFFFFFF));
        panela284.add(panela273, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela272 = new javax.swing.JLabel();
        panela272.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela272.setForeground(new java.awt.Color(0x243B53));
        panela272.setText("Tên đăng nhập *");
        panela273.add(panela272, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtTenDangNhap.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTenDangNhap.setToolTipText("Ví dụ: nguyenvana");
        txtTenDangNhap.setPreferredSize(new java.awt.Dimension(180, 32));
        txtTenDangNhap.setMinimumSize(new java.awt.Dimension(50, 32));
        panela273.add(txtTenDangNhap, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela276 = new javax.swing.JPanel();
        panela276.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela276.setBackground(new java.awt.Color(0xFFFFFF));
        panela284.add(panela276, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela275 = new javax.swing.JLabel();
        panela275.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela275.setForeground(new java.awt.Color(0x243B53));
        panela275.setText("Vai trò *");
        panela276.add(panela275, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        cboVaiTro = new javax.swing.JComboBox();
        cboVaiTro.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboVaiTro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Sinh viên", "Cán bộ sự kiện", "Quản trị viên"}));
        cboVaiTro.setPreferredSize(new java.awt.Dimension(175, 32));
        cboVaiTro.setMinimumSize(new java.awt.Dimension(110, 32));
        panela276.add(cboVaiTro, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        nhomMatKhau = new javax.swing.JPanel();
        nhomMatKhau.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        nhomMatKhau.setBackground(new java.awt.Color(0xFFFFFF));
        panela284.add(nhomMatKhau, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela279 = new javax.swing.JPanel();
        panela279.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela279.setBackground(new java.awt.Color(0xFFFFFF));
        nhomMatKhau.add(panela279, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela278 = new javax.swing.JLabel();
        panela278.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela278.setForeground(new java.awt.Color(0x243B53));
        panela278.setText("Mật khẩu *");
        panela279.add(panela278, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtMatKhau = new javax.swing.JPasswordField();
        txtMatKhau.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtMatKhau.setToolTipText("Mật khẩu có ít nhất 6 ký tự");
        txtMatKhau.setPreferredSize(new java.awt.Dimension(220, 32));
        panela279.add(txtMatKhau, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela282 = new javax.swing.JPanel();
        panela282.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela282.setBackground(new java.awt.Color(0xFFFFFF));
        nhomMatKhau.add(panela282, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela281 = new javax.swing.JLabel();
        panela281.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela281.setForeground(new java.awt.Color(0x243B53));
        panela281.setText("Nhập lại mật khẩu *");
        panela282.add(panela281, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtNhapLaiMatKhau = new javax.swing.JPasswordField();
        txtNhapLaiMatKhau.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtNhapLaiMatKhau.setToolTipText("Nhập lại mật khẩu vừa tạo");
        txtNhapLaiMatKhau.setPreferredSize(new java.awt.Dimension(220, 32));
        panela282.add(txtNhapLaiMatKhau, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela297 = new javax.swing.JPanel();
        panela297.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(4, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela297.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNhapLieu.add(panela297, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela287 = new javax.swing.JPanel();
        panela287.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela287.setBackground(new java.awt.Color(0xFFFFFF));
        panela297.add(panela287, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela286 = new javax.swing.JLabel();
        panela286.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela286.setForeground(new java.awt.Color(0x243B53));
        panela286.setText("Họ và tên *");
        panela287.add(panela286, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtHoTen.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtHoTen.setToolTipText("Ví dụ: Nguyễn Văn An");
        txtHoTen.setPreferredSize(new java.awt.Dimension(180, 32));
        txtHoTen.setMinimumSize(new java.awt.Dimension(50, 32));
        panela287.add(txtHoTen, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela290 = new javax.swing.JPanel();
        panela290.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela290.setBackground(new java.awt.Color(0xFFFFFF));
        panela297.add(panela290, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela289 = new javax.swing.JLabel();
        panela289.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela289.setForeground(new java.awt.Color(0x243B53));
        panela289.setText("Email *");
        panela290.add(panela289, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtEmail.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtEmail.setToolTipText("Ví dụ: ten@epu.edu.vn");
        txtEmail.setPreferredSize(new java.awt.Dimension(180, 32));
        txtEmail.setMinimumSize(new java.awt.Dimension(50, 32));
        panela290.add(txtEmail, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela293 = new javax.swing.JPanel();
        panela293.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela293.setBackground(new java.awt.Color(0xFFFFFF));
        panela297.add(panela293, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela292 = new javax.swing.JLabel();
        panela292.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela292.setForeground(new java.awt.Color(0x243B53));
        panela292.setText("Mã SV / Mã NV");
        panela293.add(panela292, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtMaSo.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtMaSo.setToolTipText("Mã sinh viên hoặc mã nhân viên");
        txtMaSo.setPreferredSize(new java.awt.Dimension(180, 32));
        txtMaSo.setMinimumSize(new java.awt.Dimension(50, 32));
        panela293.add(txtMaSo, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JPanel panela296 = new javax.swing.JPanel();
        panela296.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela296.setBackground(new java.awt.Color(0xFFFFFF));
        panela297.add(panela296, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela295 = new javax.swing.JLabel();
        panela295.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela295.setForeground(new java.awt.Color(0x243B53));
        panela295.setText("Số điện thoại");
        panela296.add(panela295, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        txtSoDienThoai.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtSoDienThoai.setToolTipText("Ví dụ: 0912345678");
        txtSoDienThoai.setPreferredSize(new java.awt.Dimension(180, 32));
        txtSoDienThoai.setMinimumSize(new java.awt.Dimension(50, 32));
        panela296.add(txtSoDienThoai, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        pnlNutChucNang = new javax.swing.JPanel();
        pnlNutChucNang.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlNutChucNang.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(pnlNutChucNang, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        loi = new javax.swing.JLabel();
        loi.setFont(new java.awt.Font("Segoe UI", 0, 12));
        loi.setForeground(new java.awt.Color(0xDC3545));
        loi.setText(" ");
        pnlNutChucNang.add(loi, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela302 = new javax.swing.JPanel();
        panela302.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela302.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNutChucNang.add(panela302, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        huy = new javax.swing.JButton();
        huy.setFont(new java.awt.Font("Segoe UI", 0, 13));
        huy.setBackground(new java.awt.Color(0xFFFFFF));
        huy.setForeground(new java.awt.Color(0x243B53));
        huy.setFocusPainted(false);
        huy.setPreferredSize(new java.awt.Dimension(130, 30));
        huy.setText("Hủy");
        panela302.add(huy, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, null, null, null));
        luu = new javax.swing.JButton();
        luu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        luu.setBackground(new java.awt.Color(0x1565C0));
        luu.setForeground(new java.awt.Color(0xFFFFFF));
        luu.setFocusPainted(false);
        luu.setContentAreaFilled(false);
        luu.setOpaque(true);
        luu.setBorderPainted(false);
        luu.setPreferredSize(new java.awt.Dimension(130, 30));
        luu.setText("Tạo tài khoản");
        panela302.add(luu, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 4, 0, 0, 0, null, null, null));
    }

    private void createUIComponents() {
        txtSoDienThoai = UI.oNhap("Ví dụ: 0912345678");
        txtMaSo = UI.oNhap("Mã sinh viên hoặc mã nhân viên");
        txtEmail = UI.oNhap("Ví dụ: ten@epu.edu.vn");
        txtHoTen = UI.oNhap("Ví dụ: Nguyễn Văn An");
        txtTenDangNhap = UI.oNhap("Ví dụ: nguyenvana");
    }
}
