package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Use case "Quản lý thông tin cá nhân": xem thông tin cá nhân, cập nhật thông tin.
 * Trái: thẻ hồ sơ (avatar, họ tên, mã SV, số sự kiện). Phải: form sửa thông tin.
 */
public class ThongTinCaNhan extends TrangSinhVien {

    private final Runnable sauKhiLuu; // báo khung chính đổi tên hiển thị trên thanh trên cùng

    private final JLabel lblAvatar = new JLabel();
    private final JLabel lblHoTen = UI.nhan(" ", Theme.H2, Theme.TEXT);
    private final JLabel lblMaSv = UI.nhan(" ", Theme.BODY, Theme.MUTED);
    private final JLabel lblThamGia = UI.nhan("–", Theme.NUMBER, Theme.SUCCESS);
    private final JLabel lblDanhGia = UI.nhan("–", Theme.NUMBER, Theme.WARNING);

    private final UI.TextFieldDep txtTenDangNhap = UI.oNhap(null);
    private final UI.TextFieldDep txtMaSv = UI.oNhap("VD: 2281010001");
    private final UI.TextFieldDep txtHoTen = UI.oNhap("Họ và tên đầy đủ");
    private final JDateChooser dcNgaySinh = HocSinhUI.oChonNgay();
    private final UI.TextFieldDep txtLop = UI.oNhap("VD: D17CNPM1");
    private final UI.TextFieldDep txtEmail = UI.oNhap("VD: ten@epu.edu.vn");
    private final UI.TextFieldDep txtSdt = UI.oNhap("10 chữ số, bắt đầu bằng 0");
    private final JLabel lblLoi = UI.nhan(" ", Theme.SMALL, Theme.DANGER);

    public ThongTinCaNhan(DieuHuong dieuHuong, Runnable sauKhiLuu) {
        super(dieuHuong);
        this.sauKhiLuu = sauKhiLuu;
        add(UI.tieuDeTrang("Thông tin cá nhân", "Xem và cập nhật thông tin liên lạc của bạn"), BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(18, 0));
        giua.setOpaque(false);
        giua.add(taoTheHoSo(), BorderLayout.WEST);
        JPanel bocForm = new JPanel(new BorderLayout());
        bocForm.setOpaque(false);
        bocForm.add(taoForm(), BorderLayout.NORTH);
        giua.add(bocForm, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);
    }

    private JComponent taoTheHoSo() {
        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{lblAvatar, lblHoTen, lblMaSv}) c.setAlignmentX(CENTER_ALIGNMENT);
        noiDung.add(Box.createVerticalStrut(8));
        noiDung.add(lblAvatar);
        noiDung.add(Box.createVerticalStrut(12));
        noiDung.add(lblHoTen);
        noiDung.add(Box.createVerticalStrut(4));
        noiDung.add(lblMaSv);
        noiDung.add(Box.createVerticalStrut(10));
        JPanel nhan = UI.hang(0, new HocSinhUI.Nhan("Sinh viên"));
        ((FlowLayout) nhan.getLayout()).setAlignment(FlowLayout.CENTER);
        nhan.setAlignmentX(CENTER_ALIGNMENT);
        noiDung.add(nhan);
        noiDung.add(Box.createVerticalStrut(18));

        // 2 ô số liệu nhỏ
        JPanel soLieu = new JPanel(new GridLayout(1, 2, 10, 0));
        soLieu.setOpaque(false);
        soLieu.add(oSo(lblThamGia, "Sự kiện đã tham gia"));
        soLieu.add(oSo(lblDanhGia, "Đánh giá đã gửi"));
        soLieu.setAlignmentX(CENTER_ALIGNMENT);
        noiDung.add(soLieu);

        UI.RoundPanel the = UI.card(new BorderLayout(), 20);
        the.add(noiDung, BorderLayout.NORTH);
        the.setPreferredSize(new Dimension(300, 0));
        return the;
    }

    private static JComponent oSo(JLabel so, String nhan) {
        UI.RoundPanel o = new UI.RoundPanel(new BorderLayout(0, 2), 12, Theme.TABLE_HEAD, Theme.BORDER);
        o.setBorder(new EmptyBorder(10, 8, 10, 8));
        so.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel l = UI.nhan(nhan, Theme.SMALL, Theme.MUTED);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        o.add(so, BorderLayout.CENTER);
        o.add(l, BorderLayout.SOUTH);
        return o;
    }

    private JComponent taoForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        txtTenDangNhap.setEditable(false); // không cho sửa tên đăng nhập
        Object[][] truong = {
                {"Tên đăng nhập", txtTenDangNhap}, {"Mã sinh viên", txtMaSv},
                {"Họ và tên *", txtHoTen}, {"Ngày sinh", dcNgaySinh},
                {"Lớp", txtLop}, {"Số điện thoại", txtSdt},
                {"Email", txtEmail},
        };
        for (int i = 0; i < truong.length; i++) {
            boolean caHang = i == truong.length - 1;
            JPanel cum = new JPanel(new BorderLayout(0, 6));
            cum.setOpaque(false);
            cum.add(UI.nhan((String) truong[i][0], Theme.SMALL_BOLD, Theme.TEXT_2), BorderLayout.NORTH);
            JComponent o = (JComponent) truong[i][1];
            o.setPreferredSize(new Dimension(200, 38));
            cum.add(o, BorderLayout.CENTER);
            GridBagConstraints g = new GridBagConstraints();
            g.gridx = caHang ? 0 : i % 2;
            g.gridy = i / 2;
            g.gridwidth = caHang ? 2 : 1;
            g.weightx = 1;
            g.fill = GridBagConstraints.HORIZONTAL;
            g.insets = new Insets(0, g.gridx == 1 ? 8 : 0, 16, g.gridx == 0 && !caHang ? 8 : 0);
            form.add(cum, g);
        }

        UI.FlatButton btnHoanTac = UI.nut("Hoàn tác", Icons.REFRESH, UI.Kieu.VIEN);
        UI.FlatButton btnLuu = UI.nut("Lưu thay đổi", Icons.CHECK, UI.Kieu.CHINH);
        btnHoanTac.addActionListener(e -> taiLai());
        btnLuu.addActionListener(e -> luu());
        JPanel chan = new JPanel(new BorderLayout());
        chan.setOpaque(false);
        chan.add(lblLoi, BorderLayout.CENTER);
        chan.add(UI.hangPhai(10, btnHoanTac, btnLuu), BorderLayout.EAST);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 10;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 0, 0, 0);
        form.add(chan, g);

        return HocSinhUI.the("Chỉnh sửa thông tin", null, form);
    }

    /** GET hồ sơ + đếm số sự kiện đã tham gia / đánh giá đã gửi cho thẻ bên trái. */
    @Override
    public void taiLai() {
        String ten = Api.e(dieuHuong.tenDangNhap());
        lblLoi.setText(" ");
        Api.chayNen(this, () -> {
            JsonObject kq = new JsonObject();
            kq.add("hoSo", Api.get("/api/sinh-vien/thong-tin?tenDangNhap=" + ten));
            kq.add("dangKy", Api.get("/api/dang-ky/cua-toi?tenDangNhap=" + ten));
            kq.add("danhGia", Api.get("/api/danh-gia/cua-toi?tenDangNhap=" + ten));
            return kq;
        }, kq -> {
            JsonObject sv = Api.obj(kq, "hoSo");
            txtTenDangNhap.setText(Api.s(sv, "tenDangNhap"));
            txtMaSv.setText(Api.s(sv, "maSinhVien"));
            txtHoTen.setText(Api.s(sv, "hoTen"));
            txtLop.setText(Api.s(sv, "lop"));
            txtEmail.setText(Api.s(sv, "email"));
            txtSdt.setText(Api.s(sv, "soDienThoai"));
            dcNgaySinh.setDate(HocSinhUI.tuIsoNgay(Api.s(sv, "ngaySinh")));
            capNhatTheHoSo(Api.s(sv, "hoTen"), Api.s(sv, "maSinhVien"), Api.s(sv, "lop"));

            long thamGia = 0;
            for (JsonElement el : Api.arr(kq, "dangKy")) {
                if ("Đã tham gia".equals(Api.s(el.getAsJsonObject(), "trangThai"))) thamGia++;
            }
            lblThamGia.setText(String.valueOf(thamGia));
            lblDanhGia.setText(String.valueOf(Api.arr(kq, "danhGia").size()));
        });
    }

    private void capNhatTheHoSo(String hoTen, String maSv, String lop) {
        String ten = hoTen.isBlank() ? dieuHuong.tenDangNhap() : hoTen;
        lblAvatar.setIcon(UI.avatar(UI.vietTat(ten), 88, Theme.PRIMARY));
        lblHoTen.setText(ten);
        lblMaSv.setText((maSv.isBlank() ? "Chưa có mã SV" : maSv) + (lop.isBlank() ? "" : "  •  " + lop));
    }

    /** Kiểm tra sơ bộ ở giao diện rồi PUT /api/sinh-vien/cap-nhat (backend kiểm tra lại). */
    private void luu() {
        String hoTen = txtHoTen.getText().trim();
        String email = txtEmail.getText().trim();
        String sdt = txtSdt.getText().trim();
        if (hoTen.isEmpty()) {
            lblLoi.setText("Họ tên không được để trống");
            txtHoTen.requestFocus();
            return;
        }
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")) {
            lblLoi.setText("Email không đúng định dạng");
            txtEmail.requestFocus();
            return;
        }
        if (!sdt.isEmpty() && !sdt.matches("^0\\d{9}$")) {
            lblLoi.setText("Số điện thoại phải gồm 10 chữ số, bắt đầu bằng 0");
            txtSdt.requestFocus();
            return;
        }
        lblLoi.setText(" ");

        JsonObject body = new JsonObject();
        body.addProperty("maSinhVien", txtMaSv.getText().trim());
        body.addProperty("hoTen", hoTen);
        body.addProperty("lop", txtLop.getText().trim());
        body.addProperty("email", email);
        body.addProperty("soDienThoai", sdt);
        String ngaySinh = HocSinhUI.isoNgay(dcNgaySinh.getDate());
        if (ngaySinh != null) body.addProperty("ngaySinh", ngaySinh);

        String url = "/api/sinh-vien/cap-nhat?tenDangNhap=" + Api.e(dieuHuong.tenDangNhap());
        Api.chayNen(this, () -> Api.put(url, body), kq -> {
            HopThoai.thanhCong(this, "Đã lưu thông tin cá nhân");
            capNhatTheHoSo(hoTen, txtMaSv.getText().trim(), txtLop.getText().trim());
            if (sauKhiLuu != null) sauKhiLuu.run();
        });
    }
}