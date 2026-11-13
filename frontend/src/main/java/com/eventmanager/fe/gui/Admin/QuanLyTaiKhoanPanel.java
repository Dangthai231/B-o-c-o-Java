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

    // Tab 1: danh sách tài khoản
    private final List<JsonObject> dsHienTai = new ArrayList<>();
    private int trangHienTai = 0;
    private int lanTaiDanhSach;
    private int lanTaiYeuCau;
    private final Timer henGioTim;

    // Tab 2: yêu cầu thay đổi thông tin

    public QuanLyTaiKhoanPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        $$$setupUI$$$();
        lblBieuTuongTongTaiKhoan.setIcon(Icons.of(Icons.USER, 26, new Color(0x1565C0)));
        lblBieuTuongTaiKhoanHoatDong.setIcon(Icons.of(Icons.CHECK_CIRCLE, 26, new Color(0x159B64)));
        lblBieuTuongTaiKhoanBiKhoa.setIcon(Icons.of(Icons.LOCK, 26, new Color(0xDC3545)));
        lblBieuTuongYeuCauChoDuyet.setIcon(Icons.of(Icons.CLOCK, 26, new Color(0xD79318)));
        vungNoiDung.add(mainPanel, BorderLayout.CENTER);
        btnTaoTaiKhoan.addActionListener(e -> TaiKhoanDialogs.formTaiKhoan(this, null, this::taiLai));
        tabChucNang.addChangeListener(e -> { if (tabChucNang.getSelectedIndex() == 1) taiYeuCau(); });
        thanhChonNhieu.setVisible(false);
        btnKhoaNhieu.addActionListener(e -> doiTrangThaiNhieu("BI_KHOA"));
        btnMoNhieu.addActionListener(e -> doiTrangThaiNhieu("HOAT_DONG"));
        btnBoChon.addActionListener(e -> { for (int i=0;i<bangTaiKhoan.getRowCount();i++) bangTaiKhoan.getModelBang().setValueAt(false,i,1); });
        bangTaiKhoan.anCot(0);
        bangTaiKhoan.coDinhCot(1, 40);
        bangTaiKhoan.doRongCot(2, 150);
        bangTaiKhoan.doRongCot(3, 175);
        bangTaiKhoan.doRongCot(4, 90);
        bangTaiKhoan.doRongCot(7, 110);
        bangTaiKhoan.coDinhCot(8, 186);
        bangTaiKhoan.cotNhan(5);
        bangTaiKhoan.cotNhan(6);
        bangTaiKhoan.getColumnModel().getColumn(5).setMinWidth(126);
        bangTaiKhoan.cotThaoTac(8, List.of(
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
        bangTaiKhoan.getModelBang().addTableModelListener(e -> capNhatThanhChon());
        bangYeuCau.anCot(0);
        bangYeuCau.cotHaiDong(1);
        bangYeuCau.doRongCot(1, 200);
        bangYeuCau.doRongCot(3, 220);
        bangYeuCau.cotNhan(5);
        bangYeuCau.coDinhCot(6, 140);
        bangYeuCau.cotThaoTac(6, List.of(new BangDep.NutThaoTac("xem", "Xem & duyệt", Icons.EYE, "Xem chi tiết yêu cầu")),
                null, (ma, dong) -> TaiKhoanDialogs.duyetYeuCau(this,
                        ((Number) bangYeuCau.getModelBang().getValueAt(dong, 0)).longValue(), this::taiLai));
        // Gõ tìm kiếm: đợi 350ms sau phím cuối mới gọi API (tránh gọi liên tục)
        henGioTim = new Timer(350, e -> taiTrang(0));
        henGioTim.setRepeats(false);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { henGioTim.restart(); }
            public void removeUpdate(DocumentEvent e) { henGioTim.restart(); }
            public void changedUpdate(DocumentEvent e) { henGioTim.restart(); }
        });
        cboVaiTro.addActionListener(e -> taiTrang(0));
        cboTrangThai.addActionListener(e -> taiTrang(0));
        cboTrangThaiYeuCau.addActionListener(e -> taiYeuCau());

    }

    // ======================= TAB 1: DANH SÁCH =======================


    /** Khi tích chọn nhiều dòng: hiện thanh "Đã chọn n tài khoản  [Khóa] [Mở khóa] [Bỏ chọn]". */


    private List<Long> idDaChon() {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < bangTaiKhoan.getModelBang().getRowCount(); i++) {
            if (Boolean.TRUE.equals(bangTaiKhoan.getModelBang().getValueAt(i, 1))) {
                ids.add(((Number) bangTaiKhoan.getModelBang().getValueAt(i, 0)).longValue());
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
        Api.chayThaoTac(this, () -> {
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
        Api.chayThaoTac(this,
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
            Api.chayThaoTac(this, () -> Api.delete("/api/admin/tai-khoan/" + Api.n(tk, "id")), kq -> {
                HopThoai.thanhCong(this, "Đã xóa tài khoản");
                taiLai();
            });
        });
        Rectangle o = bangTaiKhoan.getCellRect(bangTaiKhoan.convertRowIndexToView(dong), 8, true);
        m.show(bangTaiKhoan, o.x + o.width - 150, o.y + o.height - 6);
    }

    // ======================= TAB 2: DUYỆT THAY ĐỔI THÔNG TIN =======================


    private void taiYeuCau() {
        final int lanTai = ++lanTaiYeuCau;
        String loc = (String) cboTrangThaiYeuCau.getSelectedItem();
        String tt = "Tất cả".equals(loc) ? "" : loc;
        Api.chayNen(this, () -> Api.get("/api/admin/yeu-cau?trangThai=" + Api.e(tt)), kq -> {
            if (lanTai != lanTaiYeuCau) return;
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
            lblTongTaiKhoan.setText(o.has("tongTaiKhoan") && !o.get("tongTaiKhoan").isJsonNull() ? o.get("tongTaiKhoan").getAsString() : "—");
            lblTaiKhoanHoatDong.setText(o.has("dangHoatDong") && !o.get("dangHoatDong").isJsonNull() ? o.get("dangHoatDong").getAsString() : "—");
            lblTaiKhoanBiKhoa.setText(o.has("biKhoa") && !o.get("biKhoa").isJsonNull() ? o.get("biKhoa").getAsString() : "—");
            long cho = Api.n(o, "choDuyet");
            lblYeuCauChoDuyet.setText(String.valueOf(cho));
            tabChucNang.setTitleAt(1, cho > 0 ? "Duyệt thay đổi thông tin (" + cho + ")" : "Duyệt thay đổi thông tin");
        });
        taiTrang(trangHienTai);
        if (tabChucNang.getSelectedIndex() == 1) taiYeuCau();
        dieuHuong.capNhatChuong();
    }

    /** Dùng khi tìm nhanh từ ô tìm kiếm trên thanh trên cùng. */
    public void datTuKhoa(String tuKhoa) {
        tabChucNang.setSelectedIndex(0);
        txtTimKiem.setText(tuKhoa);
    }

    private void taiTrang(int trang) {
        final int lanTai = ++lanTaiDanhSach;
        String tuKhoa = txtTimKiem.getText().trim();
        String vaiTro = VAI_TRO_MA[cboVaiTro.getSelectedIndex()];
        String trangThai = TRANG_THAI_MA[cboTrangThai.getSelectedIndex()];
        String url = "/api/admin/tai-khoan?tuKhoa=" + Api.e(tuKhoa) + "&vaiTro=" + vaiTro + "&trangThai=" + trangThai
                + "&trang=" + Math.max(trang, 0) + "&kichThuoc=" + KICH_THUOC_TRANG;
        Api.chayNen(this, () -> Api.get(url), kq -> {
            if (lanTai != lanTaiDanhSach) return;
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
        bangTaiKhoan.xoaHet();
        for (JsonElement el : ds) {
            JsonObject tk = el.getAsJsonObject();
            dsHienTai.add(tk);
            bangTaiKhoan.themDong(Api.n(tk, "id"), false, Api.s(tk, "hoTen"), Api.s(tk, "email"), Api.s(tk, "maSo"),
                    Api.s(tk, "tenVaiTro"), "BI_KHOA".equals(Api.s(tk, "trangThai")) ? "Bị khóa" : "Hoạt động",
                    Api.s(tk, "ngayTao"), "");
        }
        capNhatThanhChon();
        hienSoKetQua(dsHienTai.size());
    }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel lblTongTaiKhoan;
    private javax.swing.JLabel lblTaiKhoanHoatDong;
    private javax.swing.JLabel lblTaiKhoanBiKhoa;
    private javax.swing.JLabel lblYeuCauChoDuyet;
    private javax.swing.JButton btnTaoTaiKhoan;
    private javax.swing.JTabbedPane tabChucNang;
    private javax.swing.JTextField txtTimKiem;
    private javax.swing.JComboBox<String> cboVaiTro;
    private javax.swing.JComboBox<String> cboTrangThai;
    private com.eventmanager.fe.gui.Admin.common.BangDep bangTaiKhoan;
    private javax.swing.JPanel thanhChonNhieu;
    private javax.swing.JLabel lblDaChon;
    private javax.swing.JButton btnKhoaNhieu;
    private javax.swing.JButton btnMoNhieu;
    private javax.swing.JButton btnBoChon;
    private com.eventmanager.fe.gui.Admin.common.UI.PhanTrang phanTrang;
    private javax.swing.JComboBox<String> cboTrangThaiYeuCau;
    private com.eventmanager.fe.gui.Admin.common.BangDep bangYeuCau;

    private javax.swing.JPanel pnlTieuDe;
    private javax.swing.JPanel pnlNoiDung;
    private javax.swing.JPanel pnlThongKe;
    private javax.swing.JPanel pnlQuanLy;
    private javax.swing.JPanel pnlBoLoc;

    private JLabel lblBieuTuongTongTaiKhoan;
    private JLabel lblBieuTuongTaiKhoanHoatDong;
    private JLabel lblBieuTuongTaiKhoanBiKhoa;
    private JLabel lblBieuTuongYeuCauChoDuyet;

    private javax.swing.JPanel pnlTruongTxtTimKiem;
    private javax.swing.JLabel lblNhanTxtTimKiem;
    private javax.swing.JPanel pnlTruongCboVaiTro;
    private javax.swing.JLabel lblNhanCboVaiTro;
    private javax.swing.JPanel pnlTruongCboTrangThai;
    private javax.swing.JLabel lblNhanCboTrangThai;
    private javax.swing.JPanel pnlTruongCboTrangThaiYeuCau;
    private javax.swing.JLabel lblNhanCboTrangThaiYeuCau;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xF5F7FA));
        pnlTieuDe = new javax.swing.JPanel();
        pnlTieuDe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlTieuDe.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlTieuDe, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        javax.swing.JLabel panela47 = new javax.swing.JLabel();
        panela47.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela47.setForeground(new java.awt.Color(0x0B2B50));
        panela47.setText("Quản lý tài khoản");
        pnlTieuDe.add(panela47, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela48 = new javax.swing.JLabel();
        panela48.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela48.setForeground(new java.awt.Color(0x627D98));
        panela48.setText("Tạo, tra cứu và quản lý trạng thái tài khoản trong hệ thống");
        pnlTieuDe.add(panela48, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlNoiDung = new javax.swing.JPanel();
        pnlNoiDung.setLayout(new java.awt.BorderLayout(12, 16));
        pnlNoiDung.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlNoiDung, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        pnlThongKe = new javax.swing.JPanel();
        pnlThongKe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new java.awt.Insets(0, 0, 0, 0), 12, 10, true, false));
        pnlThongKe.setBackground(new java.awt.Color(0xF5F7FA));
        pnlNoiDung.add(pnlThongKe, java.awt.BorderLayout.NORTH);
        javax.swing.JPanel panela30 = new javax.swing.JPanel();
        panela30.setLayout(new java.awt.BorderLayout(12, 16));
        panela30.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela30.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela30, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongTongTaiKhoan = new javax.swing.JLabel();
        lblBieuTuongTongTaiKhoan.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongTongTaiKhoan.setForeground(new java.awt.Color(0x1565C0));
        lblBieuTuongTongTaiKhoan.setText("");
        lblBieuTuongTongTaiKhoan.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongTongTaiKhoan.setHorizontalAlignment(0);
        panela30.add(lblBieuTuongTongTaiKhoan, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela29 = new javax.swing.JPanel();
        panela29.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela29.setBackground(new java.awt.Color(0xFFFFFF));
        panela30.add(panela29, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela27 = new javax.swing.JLabel();
        panela27.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela27.setForeground(new java.awt.Color(0x627D98));
        panela27.setText("Tổng tài khoản");
        panela29.add(panela27, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTongTaiKhoan = new javax.swing.JLabel();
        lblTongTaiKhoan.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTongTaiKhoan.setForeground(new java.awt.Color(0x1565C0));
        lblTongTaiKhoan.setText("—");
        panela29.add(lblTongTaiKhoan, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela35 = new javax.swing.JPanel();
        panela35.setLayout(new java.awt.BorderLayout(12, 16));
        panela35.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela35.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela35, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongTaiKhoanHoatDong = new javax.swing.JLabel();
        lblBieuTuongTaiKhoanHoatDong.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongTaiKhoanHoatDong.setForeground(new java.awt.Color(0x159B64));
        lblBieuTuongTaiKhoanHoatDong.setText("");
        lblBieuTuongTaiKhoanHoatDong.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongTaiKhoanHoatDong.setHorizontalAlignment(0);
        panela35.add(lblBieuTuongTaiKhoanHoatDong, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela34 = new javax.swing.JPanel();
        panela34.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela34.setBackground(new java.awt.Color(0xFFFFFF));
        panela35.add(panela34, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela32 = new javax.swing.JLabel();
        panela32.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela32.setForeground(new java.awt.Color(0x627D98));
        panela32.setText("Đang hoạt động");
        panela34.add(panela32, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTaiKhoanHoatDong = new javax.swing.JLabel();
        lblTaiKhoanHoatDong.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTaiKhoanHoatDong.setForeground(new java.awt.Color(0x159B64));
        lblTaiKhoanHoatDong.setText("—");
        panela34.add(lblTaiKhoanHoatDong, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela40 = new javax.swing.JPanel();
        panela40.setLayout(new java.awt.BorderLayout(12, 16));
        panela40.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela40.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela40, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongTaiKhoanBiKhoa = new javax.swing.JLabel();
        lblBieuTuongTaiKhoanBiKhoa.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongTaiKhoanBiKhoa.setForeground(new java.awt.Color(0xDC3545));
        lblBieuTuongTaiKhoanBiKhoa.setText("");
        lblBieuTuongTaiKhoanBiKhoa.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongTaiKhoanBiKhoa.setHorizontalAlignment(0);
        panela40.add(lblBieuTuongTaiKhoanBiKhoa, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela39 = new javax.swing.JPanel();
        panela39.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela39.setBackground(new java.awt.Color(0xFFFFFF));
        panela40.add(panela39, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela37 = new javax.swing.JLabel();
        panela37.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela37.setForeground(new java.awt.Color(0x627D98));
        panela37.setText("Bị khóa");
        panela39.add(panela37, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblTaiKhoanBiKhoa = new javax.swing.JLabel();
        lblTaiKhoanBiKhoa.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblTaiKhoanBiKhoa.setForeground(new java.awt.Color(0xDC3545));
        lblTaiKhoanBiKhoa.setText("—");
        panela39.add(lblTaiKhoanBiKhoa, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela45 = new javax.swing.JPanel();
        panela45.setLayout(new java.awt.BorderLayout(12, 16));
        panela45.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        panela45.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThongKe.add(panela45, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, 0, 3, 7, 3, null, null, null));
        lblBieuTuongYeuCauChoDuyet = new javax.swing.JLabel();
        lblBieuTuongYeuCauChoDuyet.setFont(new java.awt.Font("Segoe UI", 1, 28));
        lblBieuTuongYeuCauChoDuyet.setForeground(new java.awt.Color(0xD79318));
        lblBieuTuongYeuCauChoDuyet.setText("");
        lblBieuTuongYeuCauChoDuyet.setPreferredSize(new java.awt.Dimension(36, 48));
        lblBieuTuongYeuCauChoDuyet.setHorizontalAlignment(0);
        panela45.add(lblBieuTuongYeuCauChoDuyet, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela44 = new javax.swing.JPanel();
        panela44.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela44.setBackground(new java.awt.Color(0xFFFFFF));
        panela45.add(panela44, java.awt.BorderLayout.CENTER);
        javax.swing.JLabel panela42 = new javax.swing.JLabel();
        panela42.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela42.setForeground(new java.awt.Color(0x627D98));
        panela42.setText("Chờ duyệt");
        panela44.add(panela42, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblYeuCauChoDuyet = new javax.swing.JLabel();
        lblYeuCauChoDuyet.setFont(new java.awt.Font("Segoe UI", 1, 22));
        lblYeuCauChoDuyet.setForeground(new java.awt.Color(0xD79318));
        lblYeuCauChoDuyet.setText("—");
        panela44.add(lblYeuCauChoDuyet, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlQuanLy = new javax.swing.JPanel();
        pnlQuanLy.setLayout(new java.awt.BorderLayout(12, 16));
        pnlQuanLy.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        pnlQuanLy.setBackground(new java.awt.Color(0xFFFFFF));
        pnlNoiDung.add(pnlQuanLy, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela23 = new javax.swing.JPanel();
        panela23.setLayout(new java.awt.BorderLayout(12, 16));
        panela23.setBackground(new java.awt.Color(0xFFFFFF));
        pnlQuanLy.add(panela23, java.awt.BorderLayout.NORTH);
        javax.swing.JLabel panela21 = new javax.swing.JLabel();
        panela21.setFont(new java.awt.Font("Segoe UI", 1, 15));
        panela21.setForeground(new java.awt.Color(0x243B53));
        panela21.setText("Tài khoản hệ thống");
        panela23.add(panela21, java.awt.BorderLayout.CENTER);
        btnTaoTaiKhoan = new javax.swing.JButton();
        btnTaoTaiKhoan.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnTaoTaiKhoan.setBackground(new java.awt.Color(0x1565C0));
        btnTaoTaiKhoan.setForeground(new java.awt.Color(0xFFFFFF));
        btnTaoTaiKhoan.setFocusPainted(false);
        btnTaoTaiKhoan.setContentAreaFilled(false);
        btnTaoTaiKhoan.setOpaque(true);
        btnTaoTaiKhoan.setBorderPainted(false);
        btnTaoTaiKhoan.setPreferredSize(new java.awt.Dimension(130, 30));
        btnTaoTaiKhoan.setText("+ Tạo tài khoản");
        panela23.add(btnTaoTaiKhoan, java.awt.BorderLayout.EAST);
        tabChucNang = new javax.swing.JTabbedPane();
        pnlQuanLy.add(tabChucNang, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela15 = new javax.swing.JPanel();
        panela15.setLayout(new java.awt.BorderLayout(12, 16));
        panela15.setBackground(new java.awt.Color(0xFFFFFF));
        tabChucNang.addTab("Danh sách tài khoản", panela15);
        pnlBoLoc = new javax.swing.JPanel();
        pnlBoLoc.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlBoLoc.setBackground(new java.awt.Color(0xFFFFFF));
        panela15.add(pnlBoLoc, java.awt.BorderLayout.NORTH);
        txtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTimKiem.setToolTipText("Tên, email, mã SV/NV");
        txtTimKiem.setPreferredSize(new java.awt.Dimension(180, 32));
        txtTimKiem.setMinimumSize(new java.awt.Dimension(50, 32));
        pnlTruongTxtTimKiem = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongTxtTimKiem.setOpaque(false);
        lblNhanTxtTimKiem = new javax.swing.JLabel("Tìm tài khoản");
        lblNhanTxtTimKiem.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanTxtTimKiem.setForeground(new java.awt.Color(0x627D98));
        lblNhanTxtTimKiem.setLabelFor(txtTimKiem);
        pnlTruongTxtTimKiem.add(lblNhanTxtTimKiem, java.awt.BorderLayout.NORTH);
        pnlTruongTxtTimKiem.add(txtTimKiem, java.awt.BorderLayout.CENTER);
        pnlBoLoc.add(pnlTruongTxtTimKiem, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        cboVaiTro = new javax.swing.JComboBox();
        cboVaiTro.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboVaiTro.setToolTipText("Lọc theo vai trò tài khoản");
        cboVaiTro.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Tất cả vai trò", "Quản trị viên", "Cán bộ sự kiện", "Sinh viên"}));
        cboVaiTro.setPreferredSize(new java.awt.Dimension(175, 32));
        cboVaiTro.setMinimumSize(new java.awt.Dimension(110, 32));
        pnlTruongCboVaiTro = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongCboVaiTro.setOpaque(false);
        lblNhanCboVaiTro = new javax.swing.JLabel("Vai trò");
        lblNhanCboVaiTro.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanCboVaiTro.setForeground(new java.awt.Color(0x627D98));
        lblNhanCboVaiTro.setLabelFor(cboVaiTro);
        pnlTruongCboVaiTro.add(lblNhanCboVaiTro, java.awt.BorderLayout.NORTH);
        pnlTruongCboVaiTro.add(cboVaiTro, java.awt.BorderLayout.CENTER);
        pnlBoLoc.add(pnlTruongCboVaiTro, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 1, 7, 0, null, null, null));
        cboTrangThai = new javax.swing.JComboBox();
        cboTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboTrangThai.setToolTipText("Lọc tài khoản hoạt động hoặc bị khóa");
        cboTrangThai.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Tất cả trạng thái", "Hoạt động", "Bị khóa"}));
        cboTrangThai.setPreferredSize(new java.awt.Dimension(175, 32));
        cboTrangThai.setMinimumSize(new java.awt.Dimension(110, 32));
        pnlTruongCboTrangThai = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongCboTrangThai.setOpaque(false);
        lblNhanCboTrangThai = new javax.swing.JLabel("Trạng thái");
        lblNhanCboTrangThai.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanCboTrangThai.setForeground(new java.awt.Color(0x627D98));
        lblNhanCboTrangThai.setLabelFor(cboTrangThai);
        pnlTruongCboTrangThai.add(lblNhanCboTrangThai, java.awt.BorderLayout.NORTH);
        pnlTruongCboTrangThai.add(cboTrangThai, java.awt.BorderLayout.CENTER);
        pnlBoLoc.add(pnlTruongCboTrangThai, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JScrollPane panela13 = new javax.swing.JScrollPane();
        panela15.add(panela13, java.awt.BorderLayout.CENTER);
        panela13.setViewportView(bangTaiKhoan);
        javax.swing.JPanel panela14 = new javax.swing.JPanel();
        panela14.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela14.setBackground(new java.awt.Color(0xFFFFFF));
        panela15.add(panela14, java.awt.BorderLayout.SOUTH);
        thanhChonNhieu = new javax.swing.JPanel();
        thanhChonNhieu.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 4, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        thanhChonNhieu.setBackground(new java.awt.Color(0xFFFFFF));
        panela14.add(thanhChonNhieu, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        lblDaChon = new javax.swing.JLabel();
        lblDaChon.setFont(new java.awt.Font("Segoe UI", 0, 13));
        lblDaChon.setForeground(new java.awt.Color(0x243B53));
        lblDaChon.setText("");
        thanhChonNhieu.add(lblDaChon, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        btnKhoaNhieu = new javax.swing.JButton();
        btnKhoaNhieu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnKhoaNhieu.setBackground(new java.awt.Color(0xFFFFFF));
        btnKhoaNhieu.setForeground(new java.awt.Color(0x243B53));
        btnKhoaNhieu.setFocusPainted(false);
        btnKhoaNhieu.setPreferredSize(new java.awt.Dimension(130, 30));
        btnKhoaNhieu.setText("Khóa");
        thanhChonNhieu.add(btnKhoaNhieu, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 4, 0, 0, 0, null, null, null));
        btnMoNhieu = new javax.swing.JButton();
        btnMoNhieu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnMoNhieu.setBackground(new java.awt.Color(0xFFFFFF));
        btnMoNhieu.setForeground(new java.awt.Color(0x243B53));
        btnMoNhieu.setFocusPainted(false);
        btnMoNhieu.setPreferredSize(new java.awt.Dimension(130, 30));
        btnMoNhieu.setText("Mở khóa");
        thanhChonNhieu.add(btnMoNhieu, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, 4, 0, 0, 0, null, null, null));
        btnBoChon = new javax.swing.JButton();
        btnBoChon.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnBoChon.setBackground(new java.awt.Color(0xFFFFFF));
        btnBoChon.setForeground(new java.awt.Color(0x243B53));
        btnBoChon.setFocusPainted(false);
        btnBoChon.setPreferredSize(new java.awt.Dimension(130, 30));
        btnBoChon.setText("Bỏ chọn");
        thanhChonNhieu.add(btnBoChon, new com.intellij.uiDesigner.core.GridConstraints(0, 3, 1, 1, 4, 0, 0, 0, null, null, null));
        panela14.add(phanTrang, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela20 = new javax.swing.JPanel();
        panela20.setLayout(new java.awt.BorderLayout(12, 16));
        panela20.setBackground(new java.awt.Color(0xFFFFFF));
        tabChucNang.addTab("Duyệt thay đổi thông tin", panela20);
        javax.swing.JPanel panela18 = new javax.swing.JPanel();
        panela18.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela18.setBackground(new java.awt.Color(0xFFFFFF));
        panela20.add(panela18, java.awt.BorderLayout.NORTH);
        javax.swing.JLabel panela16 = new javax.swing.JLabel();
        panela16.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela16.setForeground(new java.awt.Color(0x243B53));
        panela16.setText("Yêu cầu thay đổi thông tin cá nhân");
        panela18.add(panela16, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        cboTrangThaiYeuCau = new javax.swing.JComboBox();
        cboTrangThaiYeuCau.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboTrangThaiYeuCau.setToolTipText("Chọn trạng thái yêu cầu thay đổi thông tin");
        cboTrangThaiYeuCau.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Chờ duyệt", "Đã duyệt", "Từ chối", "Tất cả"}));
        cboTrangThaiYeuCau.setPreferredSize(new java.awt.Dimension(175, 32));
        cboTrangThaiYeuCau.setMinimumSize(new java.awt.Dimension(110, 32));
        pnlTruongCboTrangThaiYeuCau = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongCboTrangThaiYeuCau.setOpaque(false);
        lblNhanCboTrangThaiYeuCau = new javax.swing.JLabel("Trạng thái yêu cầu");
        lblNhanCboTrangThaiYeuCau.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanCboTrangThaiYeuCau.setForeground(new java.awt.Color(0x627D98));
        lblNhanCboTrangThaiYeuCau.setLabelFor(cboTrangThaiYeuCau);
        pnlTruongCboTrangThaiYeuCau.add(lblNhanCboTrangThaiYeuCau, java.awt.BorderLayout.NORTH);
        pnlTruongCboTrangThaiYeuCau.add(cboTrangThaiYeuCau, java.awt.BorderLayout.CENTER);
        panela18.add(pnlTruongCboTrangThaiYeuCau, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 1, 7, 0, null, null, null));
        javax.swing.JScrollPane panela19 = new javax.swing.JScrollPane();
        panela20.add(panela19, java.awt.BorderLayout.CENTER);
        panela19.setViewportView(bangYeuCau);
    }

    private void createUIComponents() {
        txtTimKiem = UI.oTimKiem("Tên, email, mã SV/NV");
        bangTaiKhoan = new BangDep("id", "", "Họ và tên", "Email", "Mã SV/Mã NV", "Vai trò", "Trạng thái", "Ngày tạo", "Thao tác");
        phanTrang = new UI.PhanTrang(this::taiTrang);
        bangYeuCau = new BangDep("id", "Người gửi", "Mã số", "Nội dung thay đổi", "Ngày gửi", "Trạng thái", "Thao tác");
    }
}
