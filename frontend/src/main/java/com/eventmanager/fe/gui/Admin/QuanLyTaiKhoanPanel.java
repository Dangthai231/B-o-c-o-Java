package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MÀN HÌNH "QUẢN LÝ TÀI KHOẢN" (frmTaotaikhoan + frmTraCuu + frmQuanLyTrangThaiTK + frmDuyetThayDoi)
 *
 *   [Tổng tài khoản] [Đang hoạt động] [Bị khóa] [Chờ duyệt]       <- 4 thẻ số liệu
 *   ┌ Danh sách tài khoản | Duyệt thay đổi thông tin ─── [+ Tạo tài khoản] ┐
 *   │ [Tìm theo họ tên, email...] [Vai trò ▾] [Trạng thái ▾]               │
 *   │ Bảng tài khoản ...                                                   │
 *   │ Hiển thị 1 - 10 trong tổng số 30           < 1 2 3 >                 │
 *   └──────────────────────────────────────────────────────────────────────┘
 */
public class QuanLyTaiKhoanPanel extends TrangAdmin {

    private static final int KICH_THUOC_TRANG = 10;
    private static final String[] VAI_TRO_HIEN = {"Tất cả vai trò", "Quản trị viên", "Cán bộ sự kiện", "Sinh viên"};
    private static final String[] VAI_TRO_MA = {"", "ADMIN", "CAN_BO", "HOC_SINH"};
    private static final String[] TRANG_THAI_HIEN = {"Tất cả trạng thái", "Hoạt động", "Bị khóa"};
    private static final String[] TRANG_THAI_MA = {"", "HOAT_DONG", "BI_KHOA"};

    // Thẻ thống kê
    private final UI.StatCard theTong = new UI.StatCard("Tổng tài khoản", Icons.USER, Theme.PRIMARY, Theme.PRIMARY_SOFT);
    private final UI.StatCard theHoatDong = new UI.StatCard("Đang hoạt động", Icons.CHECK_CIRCLE, Theme.SUCCESS, Theme.SUCCESS_SOFT);
    private final UI.StatCard theKhoa = new UI.StatCard("Bị khóa", Icons.LOCK, Theme.DANGER, Theme.DANGER_SOFT);
    private final UI.StatCard theCho = new UI.StatCard("Chờ duyệt", Icons.CLOCK, Theme.WARNING, Theme.WARNING_SOFT);

    // Tab 1: danh sách tài khoản
    private final UI.TextFieldDep oTim = UI.oTimKiem("Tìm theo họ tên, email, mã sinh viên...");
    private final JComboBox<String> cbVaiTro = UI.hopChon(VAI_TRO_HIEN, Icons.USER);
    private final JComboBox<String> cbTrangThai = UI.hopChon(TRANG_THAI_HIEN, Icons.SHIELD);
    private final BangDep bang = new BangDep("id", "", "Họ và tên", "Email", "Mã SV/Mã NV", "Vai trò",
            "Trạng thái", "Ngày tạo", "Thao tác");
    private final UI.PhanTrang phanTrang = new UI.PhanTrang(this::taiTrang);
    private final List<JsonObject> dsHienTai = new ArrayList<>();
    private final JLabel lblDaChon = UI.nhan("", Theme.SMALL_BOLD, Theme.PRIMARY);
    private final JPanel thanhChonNhieu;
    private int trangHienTai = 0;
    private final Timer henGioTim;

    // Tab 2: yêu cầu thay đổi thông tin
    private final BangDep bangYeuCau = new BangDep("id", "Người gửi", "Mã số", "Nội dung thay đổi",
            "Ngày gửi", "Trạng thái", "Thao tác");
    private final JComboBox<String> cbLocYeuCau = UI.hopChon(new String[]{"Chờ duyệt", "Đã duyệt", "Từ chối", "Tất cả"}, Icons.CLOCK);
    private final UI.TabBar tabBar;
    private final CardLayout cardTab = new CardLayout();
    private final JPanel vungTab = new JPanel(cardTab);

    public QuanLyTaiKhoanPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Quản lý tài khoản", "Tạo, tra cứu và quản lý trạng thái tài khoản trong hệ thống"),
                BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 18));
        giua.setOpaque(false);

        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.add(theTong);
        hangThe.add(theHoatDong);
        hangThe.add(theKhoa);
        hangThe.add(theCho);
        giua.add(hangThe, BorderLayout.NORTH);

        // ---------- Thẻ lớn chứa tab ----------
        UI.RoundPanel the = UI.card(new BorderLayout(0, 14), 0);
        the.setBorder(new EmptyBorder(6, 16, 14, 16));

        tabBar = new UI.TabBar(i -> {
            cardTab.show(vungTab, i == 0 ? "ds" : "yc");
            if (i == 1) taiYeuCau();
        }, "Danh sách tài khoản", "Duyệt thay đổi thông tin");
        UI.FlatButton btnTao = UI.nut("Tạo tài khoản", Icons.PLUS, UI.Kieu.CHINH);
        btnTao.addActionListener(e -> TaiKhoanDialogs.formTaiKhoan(this, null, this::taiLai));
        JPanel dauThe = new JPanel(new BorderLayout());
        dauThe.setOpaque(false);
        dauThe.add(tabBar, BorderLayout.CENTER);
        JPanel bocNut = new JPanel(new GridBagLayout());
        bocNut.setOpaque(false);
        bocNut.add(btnTao);
        dauThe.add(bocNut, BorderLayout.EAST);
        the.add(dauThe, BorderLayout.NORTH);

        vungTab.setOpaque(false);
        thanhChonNhieu = taoThanhChonNhieu();
        vungTab.add(taoTabDanhSach(), "ds");
        vungTab.add(taoTabYeuCau(), "yc");
        the.add(vungTab, BorderLayout.CENTER);
        giua.add(the, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);

        // Gõ tìm kiếm: đợi 350ms sau phím cuối mới gọi API (tránh gọi liên tục)
        henGioTim = new Timer(350, e -> taiTrang(0));
        henGioTim.setRepeats(false);
        oTim.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { henGioTim.restart(); }
            public void removeUpdate(DocumentEvent e) { henGioTim.restart(); }
            public void changedUpdate(DocumentEvent e) { henGioTim.restart(); }
        });
        cbVaiTro.addActionListener(e -> taiTrang(0));
        cbTrangThai.addActionListener(e -> taiTrang(0));
        cbLocYeuCau.addActionListener(e -> taiYeuCau());
    }

    // ======================= TAB 1: DANH SÁCH =======================
    private JPanel taoTabDanhSach() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        JPanel loc = new JPanel(new GridBagLayout());
        loc.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        oTim.setPreferredSize(new Dimension(300, 38));
        loc.add(oTim, g);
        g.weightx = 0;
        g.insets = new Insets(0, 12, 0, 0);
        cbVaiTro.setPreferredSize(new Dimension(170, 38));
        loc.add(cbVaiTro, g);
        cbTrangThai.setPreferredSize(new Dimension(170, 38));
        loc.add(cbTrangThai, g);
        p.add(loc, BorderLayout.NORTH);

        bang.anCot(0);
        bang.coDinhCot(1, 40);
        bang.doRongCot(2, 150);
        bang.doRongCot(3, 175);
        bang.doRongCot(4, 90);
        bang.doRongCot(7, 110);
        bang.coDinhCot(8, 186);
        bang.cotNhan(5);
        bang.cotNhan(6);
        bang.getColumnModel().getColumn(5).setMinWidth(126);
        bang.cotThaoTac(8, List.of(
                        new BangDep.NutThaoTac("xem", "Chi tiết", Icons.EYE, "Xem chi tiết"),
                        new BangDep.NutThaoTac("sua", null, Icons.EDIT, "Sửa thông tin"),
                        new BangDep.NutThaoTac("khoa", null, Icons.LOCK, "Khóa tài khoản"),
                        new BangDep.NutThaoTac("mo", null, Icons.UNLOCK, "Mở khóa tài khoản"),
                        new BangDep.NutThaoTac("them", null, Icons.MORE, "Thao tác khác")),
                (ma, dong) -> {
                    if (dong >= dsHienTai.size()) return false;
                    boolean biKhoa = "BI_KHOA".equals(Api.s(dsHienTai.get(dong), "trangThai"));
                    if (ma.equals("khoa")) return !biKhoa;
                    if (ma.equals("mo")) return biKhoa;
                    return true;
                },
                this::xuLyThaoTac);
        bang.getModelBang().addTableModelListener(e -> capNhatThanhChon());
        p.add(bang.trongKhungCuon(), BorderLayout.CENTER);

        JPanel chan = new JPanel(new BorderLayout());
        chan.setOpaque(false);
        chan.add(phanTrang, BorderLayout.CENTER);
        chan.add(thanhChonNhieu, BorderLayout.NORTH);
        p.add(chan, BorderLayout.SOUTH);
        return p;
    }

    /** Khi tích chọn nhiều dòng: hiện thanh "Đã chọn n tài khoản  [Khóa] [Mở khóa] [Bỏ chọn]". */
    private JPanel taoThanhChonNhieu() {
        UI.FlatButton khoa = UI.nut("Khóa", Icons.LOCK, UI.Kieu.VIEN);
        UI.FlatButton mo = UI.nut("Mở khóa", Icons.UNLOCK, UI.Kieu.VIEN);
        UI.FlatButton bo = UI.nut("Bỏ chọn", UI.Kieu.NHAT);
        khoa.addActionListener(e -> doiTrangThaiNhieu("BI_KHOA"));
        mo.addActionListener(e -> doiTrangThaiNhieu("HOAT_DONG"));
        bo.addActionListener(e -> {
            for (int i = 0; i < bang.getRowCount(); i++) bang.getModelBang().setValueAt(false, i, 1);
        });
        JPanel p = UI.hang(8, lblDaChon, khoa, mo, bo);
        p.setBorder(new EmptyBorder(0, 0, 8, 0));
        p.setVisible(false);
        return p;
    }

    private List<Long> idDaChon() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < bang.getModelBang().getRowCount(); i++) {
            if (Boolean.TRUE.equals(bang.getModelBang().getValueAt(i, 1))) {
                ids.add(((Number) bang.getModelBang().getValueAt(i, 0)).longValue());
            }
        }
        return ids;
    }

    private void capNhatThanhChon() {
        int so = idDaChon().size();
        lblDaChon.setText("Đã chọn " + so + " tài khoản");
        thanhChonNhieu.setVisible(so > 0);
    }

    private void doiTrangThaiNhieu(String trangThai) {
        List<Long> ids = idDaChon();
        String hanhDong = "BI_KHOA".equals(trangThai) ? "khóa" : "mở khóa";
        if (!HopThoai.xacNhan(this, "Xác nhận", "Bạn muốn " + hanhDong + " " + ids.size() + " tài khoản đã chọn?",
                "Đồng ý", UI.Kieu.CHINH)) return;
        Api.chayNen(this, () -> {
            List<String> loi = new ArrayList<>();
            for (Long id : ids) {
                try {
                    Api.put("/api/admin/tai-khoan/" + id + "/trang-thai", Api.body("trangThai", trangThai));
                } catch (com.eventmanager.fe.api.ApiClient.ApiException ex) {
                    loi.add(ex.getMessage());
                }
            }
            return loi;
        }, loi -> {
            if (!loi.isEmpty()) HopThoai.loi(this, String.join("\n", new java.util.LinkedHashSet<>(loi)));
            else HopThoai.thanhCong(this, "Đã " + hanhDong + " " + ids.size() + " tài khoản");
            taiLai();
        });
    }

    private void xuLyThaoTac(String ma, int dong) {
        JsonObject tk = dsHienTai.get(dong);
        long id = Api.n(tk, "id");
        switch (ma) {
            case "xem" -> TaiKhoanDialogs.chiTiet(this, tk, dieuHuong);
            case "sua" -> TaiKhoanDialogs.formTaiKhoan(this, tk, this::taiLai);
            case "khoa", "mo" -> doiTrangThai(tk, ma.equals("khoa") ? "BI_KHOA" : "HOAT_DONG");
            case "them" -> hienMenuThem(tk, dong);
            default -> {
            }
        }
    }

    /** Biểu đồ trình tự "Quản lý trạng thái tài khoản": chọn trạng thái mới -> xác nhận -> cập nhật. */
    private void doiTrangThai(JsonObject tk, String trangThaiMoi) {
        boolean khoa = "BI_KHOA".equals(trangThaiMoi);
        String ten = Api.s(tk, "hoTen");
        String noiDung = khoa
                ? "Tài khoản <b>" + ten + "</b> sẽ không thể đăng nhập cho đến khi được mở khóa."
                : "Tài khoản <b>" + ten + "</b> sẽ đăng nhập lại được bình thường.";
        if (!HopThoai.xacNhan(this, khoa ? "Khóa tài khoản" : "Mở khóa tài khoản", noiDung,
                khoa ? "Khóa tài khoản" : "Mở khóa", khoa ? UI.Kieu.NGUY_HIEM : UI.Kieu.THANH_CONG)) return;
        Api.chayNen(this,
                () -> Api.put("/api/admin/tai-khoan/" + Api.n(tk, "id") + "/trang-thai", Api.body("trangThai", trangThaiMoi)),
                kq -> {
                    HopThoai.thanhCong(this, khoa ? "Đã khóa tài khoản" : "Đã mở khóa tài khoản");
                    taiLai();
                });
    }

    private void hienMenuThem(JsonObject tk, int dong) {
        JPopupMenu m = new JPopupMenu();
        JMenuItem datLai = new JMenuItem("Đặt lại mật khẩu", Icons.of(Icons.KEY, 16, Theme.TEXT_2));
        JMenuItem phanQuyen = new JMenuItem("Phân quyền", Icons.of(Icons.SHIELD, 16, Theme.TEXT_2));
        JMenuItem xoa = new JMenuItem("Xóa tài khoản", Icons.of(Icons.TRASH, 16, Theme.DANGER));
        xoa.setForeground(Theme.DANGER);
        for (JMenuItem mi : new JMenuItem[]{datLai, phanQuyen, xoa}) {
            mi.setFont(Theme.BODY);
            mi.setBorder(new EmptyBorder(7, 8, 7, 20));
            mi.setBackground(Color.WHITE);
            m.add(mi);
        }
        datLai.addActionListener(e -> TaiKhoanDialogs.datLaiMatKhau(this, tk));
        phanQuyen.addActionListener(e -> dieuHuong.phanQuyenCho(Api.n(tk, "id")));
        xoa.addActionListener(e -> {
            if (!HopThoai.xacNhan(this, "Xóa tài khoản", "Xóa vĩnh viễn tài khoản <b>" + Api.s(tk, "tenDangNhap")
                    + "</b>? Thao tác này không thể hoàn tác.", "Xóa", UI.Kieu.NGUY_HIEM)) return;
            Api.chayNen(this, () -> Api.delete("/api/admin/tai-khoan/" + Api.n(tk, "id")), kq -> {
                HopThoai.thanhCong(this, "Đã xóa tài khoản");
                taiLai();
            });
        });
        Rectangle o = bang.getCellRect(bang.convertRowIndexToView(dong), 8, true);
        m.show(bang, o.x + o.width - 150, o.y + o.height - 6);
    }

    // ======================= TAB 2: DUYỆT THAY ĐỔI THÔNG TIN =======================
    private JPanel taoTabYeuCau() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        cbLocYeuCau.setPreferredSize(new Dimension(170, 38));
        JLabel goiY = UI.nhan("Người dùng gửi yêu cầu đổi thông tin cá nhân, Quản trị viên kiểm tra và quyết định.",
                Theme.SMALL, Theme.MUTED);
        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.add(goiY, BorderLayout.CENTER);
        dau.add(cbLocYeuCau, BorderLayout.EAST);
        p.add(dau, BorderLayout.NORTH);

        bangYeuCau.anCot(0);
        bangYeuCau.cotHaiDong(1);
        bangYeuCau.doRongCot(1, 200);
        bangYeuCau.doRongCot(3, 220);
        bangYeuCau.cotNhan(5);
        bangYeuCau.coDinhCot(6, 140);
        bangYeuCau.cotThaoTac(6, List.of(new BangDep.NutThaoTac("xem", "Xem & duyệt", Icons.EYE, "Xem chi tiết yêu cầu")),
                null, (ma, dong) -> TaiKhoanDialogs.duyetYeuCau(this,
                        ((Number) bangYeuCau.getModelBang().getValueAt(dong, 0)).longValue(), this::taiLai));
        p.add(bangYeuCau.trongKhungCuon(), BorderLayout.CENTER);
        return p;
    }

    private void taiYeuCau() {
        String loc = (String) cbLocYeuCau.getSelectedItem();
        String tt = "Tất cả".equals(loc) ? "" : loc;
        Api.chayNen(this, () -> Api.get("/api/admin/yeu-cau?trangThai=" + Api.e(tt)), kq -> {
            bangYeuCau.xoaHet();
            for (JsonElement el : Api.arr(kq)) {
                JsonObject o = el.getAsJsonObject();
                bangYeuCau.themDong(Api.n(o, "id"), Api.s(o, "hoTen") + "\n" + Api.s(o, "tenDangNhap"),
                        Api.s(o, "maSo"), Api.s(o, "noiDungThayDoi"), Api.s(o, "ngayGui"), Api.s(o, "trangThai"), "");
            }
        });
    }

    // ======================= TẢI DỮ LIỆU =======================
    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/tai-khoan/thong-ke"), kq -> {
            JsonObject o = Api.obj(kq);
            theTong.setSo(Api.n(o, "tongTaiKhoan"));
            theHoatDong.setSo(Api.n(o, "dangHoatDong"));
            theKhoa.setSo(Api.n(o, "biKhoa"));
            long cho = Api.n(o, "choDuyet");
            theCho.setSo(cho);
            tabBar.doiTen(1, cho > 0 ? "Duyệt thay đổi thông tin (" + cho + ")" : "Duyệt thay đổi thông tin");
        });
        taiTrang(trangHienTai);
        if (tabBar.getChon() == 1) taiYeuCau();
        dieuHuong.capNhatChuong();
    }

    /** Dùng khi tìm nhanh từ ô tìm kiếm trên thanh trên cùng. */
    public void datTuKhoa(String tuKhoa) {
        tabBar.chonTab(0);
        oTim.setText(tuKhoa);
    }

    private void taiTrang(int trang) {
        String tuKhoa = oTim.getText().trim();
        String vaiTro = VAI_TRO_MA[cbVaiTro.getSelectedIndex()];
        String trangThai = TRANG_THAI_MA[cbTrangThai.getSelectedIndex()];
        String url = "/api/admin/tai-khoan?tuKhoa=" + Api.e(tuKhoa) + "&vaiTro=" + vaiTro + "&trangThai=" + trangThai
                + "&trang=" + Math.max(trang, 0) + "&kichThuoc=" + KICH_THUOC_TRANG;
        Api.chayNen(this, () -> Api.get(url), kq -> {
            JsonObject o = Api.obj(kq);
            int tongTrang = (int) Api.n(o, "tongTrang");
            // Trang hiện tại vượt quá (VD: vừa xóa dòng cuối) -> lùi về trang cuối
            if (trang > 0 && trang >= tongTrang && tongTrang > 0) {
                taiTrang(tongTrang - 1);
                return;
            }
            trangHienTai = (int) Api.n(o, "trang");
            hienBang(Api.arr(o, "noiDung"));
            phanTrang.capNhat(trangHienTai, Math.max(tongTrang, 1), Api.n(o, "tongSo"), KICH_THUOC_TRANG, "tài khoản");
        });
    }

    private void hienBang(JsonArray ds) {
        dsHienTai.clear();
        bang.xoaHet();
        for (JsonElement el : ds) {
            JsonObject tk = el.getAsJsonObject();
            dsHienTai.add(tk);
            bang.themDong(Api.n(tk, "id"), false, Api.s(tk, "hoTen"), Api.s(tk, "email"), Api.s(tk, "maSo"),
                    Api.s(tk, "tenVaiTro"), "BI_KHOA".equals(Api.s(tk, "trangThai")) ? "Bị khóa" : "Hoạt động",
                    Api.s(tk, "ngayTao"), "");
        }
        capNhatThanhChon();
    }
}
