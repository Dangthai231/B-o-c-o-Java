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

    private final UI.TabBar tabBar;
    private final UI.TextFieldDep oTim = UI.oTimKiem("Tìm theo tiêu đề, mã, đối tượng...");
    private final BangDep bang = new BangDep("id", "Thông báo", "Người tạo", "Ngày gửi", "Trạng thái", "Thao tác");
    private final List<JsonObject> dangHien = new ArrayList<>();
    private final JLabel lblGoiY = UI.nhan(" ", Theme.SMALL, Theme.MUTED);
    private final javax.swing.Timer henGio;

    public QuanLyThongBaoPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Quản lý thông báo", "Tạo, duyệt, đăng và quản lý các thông báo gửi tới đoàn viên"),
                BorderLayout.NORTH);

        UI.RoundPanel the = UI.card(new BorderLayout(0, 12), 0);
        the.setBorder(new EmptyBorder(6, 16, 14, 16));
        tabBar = new UI.TabBar(i -> taiDanhSach(), TAB);
        UI.FlatButton btnTao = UI.nut("Tạo thông báo", Icons.PLUS, UI.Kieu.CHINH);
        btnTao.addActionListener(e -> formThongBao(null));
        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.add(tabBar, BorderLayout.CENTER);
        JPanel bocNut = new JPanel(new GridBagLayout());
        bocNut.setOpaque(false);
        bocNut.add(btnTao);
        dau.add(bocNut, BorderLayout.EAST);
        the.add(dau, BorderLayout.NORTH);

        JPanel noiDung = new JPanel(new BorderLayout(0, 12));
        noiDung.setOpaque(false);
        JPanel loc = new JPanel(new BorderLayout(12, 0));
        loc.setOpaque(false);
        oTim.setPreferredSize(new Dimension(360, 38));
        loc.add(oTim, BorderLayout.WEST);
        loc.add(lblGoiY, BorderLayout.CENTER);
        noiDung.add(loc, BorderLayout.NORTH);

        bang.anCot(0);
        bang.cotHaiDong(1);
        bang.doRongCot(1, 330);
        bang.doRongCot(2, 110);
        bang.doRongCot(3, 100);
        bang.cotNhan(4);
        bang.coDinhCot(5, 186);
        bang.cotThaoTac(5, List.of(
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
        noiDung.add(bang.trongKhungCuon(), BorderLayout.CENTER);
        the.add(noiDung, BorderLayout.CENTER);
        add(the, BorderLayout.CENTER);

        henGio = new javax.swing.Timer(300, e -> taiDanhSach());
        henGio.setRepeats(false);
        oTim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
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

    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/thong-bao/dem"), kq -> {
            JsonObject o = Api.obj(kq);
            long tong = 0;
            for (int i = 0; i < TAB.length - 1; i++) {
                long so = Api.n(o, TAB[i]);
                tong += so;
                tabBar.doiTen(i, TAB[i] + " (" + so + ")");
            }
            tabBar.doiTen(TAB.length - 1, "Tất cả (" + tong + ")");
        });
        taiDanhSach();
        dieuHuong.capNhatChuong();
    }

    private void taiDanhSach() {
        String tt = tabBar.getChon() == TAB.length - 1 ? "" : TAB[tabBar.getChon()];
        lblGoiY.setText(switch (tt) {
            case "Chờ duyệt" -> "Kiểm tra nội dung rồi bấm ✓ để duyệt, hoặc mở Chi tiết để từ chối.";
            case "Đã duyệt" -> "Thông báo đã duyệt, bấm nút gửi để đăng cho người nhận.";
            case "Đã đăng" -> "Đang hiển thị với người nhận. Có thể gỡ khi không còn phù hợp.";
            case "Từ chối" -> "Sửa lại nội dung, thông báo sẽ quay về hàng chờ duyệt.";
            default -> " ";
        });
        String url = "/api/admin/thong-bao?trangThai=" + Api.e(tt) + "&tuKhoa=" + Api.e(oTim.getText().trim());
        Api.chayNen(this, () -> Api.get(url), kq -> {
            dangHien.clear();
            bang.xoaHet();
            for (JsonElement el : Api.arr(kq)) {
                JsonObject o = el.getAsJsonObject();
                dangHien.add(o);
                bang.themDong(Api.n(o, "id"), Api.s(o, "tieuDe") + "\n" + Api.s(o, "maThongBao") + "  |  Gửi tới: "
                                + Api.s(o, "doiTuong"), Api.s(o, "nguoiTao"), Api.s(o, "ngayGui"), Api.s(o, "trangThai"), "");
            }
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
        Api.chayNen(this, () -> Api.put("/api/admin/thong-bao/" + id + "/" + hanhDong, null), r -> {
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
        UI.TextFieldDep oTieuDe = UI.oNhap("VD: Lịch họp BCH tháng 12");
        JComboBox<String> cbDoiTuong = UI.hopChon(DOI_TUONG, Icons.USERS);
        cbDoiTuong.setEditable(false);
        JTextArea oNoiDung = UI.vungChu(7);
        if (laSua) {
            oTieuDe.setText(Api.s(tb, "tieuDe"));
            String dt = Api.s(tb, "doiTuong");
            boolean co = false;
            for (String s : DOI_TUONG) co |= s.equals(dt);
            if (!co && !dt.isEmpty()) cbDoiTuong.addItem(dt);
            cbDoiTuong.setSelectedItem(dt);
            oNoiDung.setText(Api.s(tb, "noiDung"));
        }
        d.truong("Tiêu đề *", oTieuDe, true);
        d.truong("Gửi tới *", cbDoiTuong, true);
        d.truong("Nội dung *", UI.cuonVungChu(oNoiDung), true);
        JLabel loi = UI.nhan(" ", Theme.SMALL, Theme.DANGER);
        d.khoi(loi);

        UI.FlatButton huy = UI.nut("Hủy", UI.Kieu.VIEN);
        huy.addActionListener(e -> d.dispose());
        UI.FlatButton luu = UI.nut(laSua ? "Cập nhật" : "Tạo thông báo", Icons.CHECK, UI.Kieu.CHINH);
        luu.addActionListener(e -> {
            JsonObject body = Api.body("tieuDe", oTieuDe.getText().trim(), "noiDung", oNoiDung.getText().trim(),
                    "doiTuong", String.valueOf(cbDoiTuong.getSelectedItem()));
            luu.setEnabled(false);
            SuKienDialogs.guiForm(d, luu, loi,
                    () -> laSua ? Api.put("/api/admin/thong-bao/" + Api.n(tb, "id"), body)
                            : Api.post("/api/admin/thong-bao?nguoiTao=" + Api.e(dieuHuong.tenDangNhap()), body),
                    () -> {
                        HopThoai.thanhCong(this, laSua ? "Cập nhật thành công" : "Tạo thông báo thành công");
                        if (!laSua) tabBar.chonTab(0);
                        taiLai();
                    });
        });
        d.nut(huy);
        d.nut(luu);
        d.hien();
    }
}
