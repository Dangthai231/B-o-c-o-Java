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

    private final UI.StatCard theTong = new UI.StatCard("Tổng sự kiện", Icons.CALENDAR, Theme.PRIMARY, Theme.PRIMARY_SOFT);
    private final UI.StatCard theSap = new UI.StatCard("Sắp diễn ra", Icons.CLOCK, Theme.SUCCESS, Theme.SUCCESS_SOFT);
    private final UI.StatCard theDangKy = new UI.StatCard("Lượt đăng ký", Icons.USERS, Theme.PURPLE, Theme.PURPLE_SOFT);
    private final UI.StatCard theCho = new UI.StatCard("Kế hoạch chờ duyệt", Icons.FILE, Theme.WARNING, Theme.WARNING_SOFT);

    private final UI.TabBar tabBar;
    private final UI.TextFieldDep oTim = UI.oTimKiem("Tìm theo tên sự kiện, mã, địa điểm...");
    private final JComboBox<String> cbTrangThai = UI.hopChon(TRANG_THAI, Icons.CALENDAR);
    private final BangDep bang = new BangDep("id", "Sự kiện", "Bắt đầu", "Đăng ký", "CB", "Trạng thái", "Duyệt", "Thao tác");
    private final JLabel lblSoLuong = UI.nhan(" ", Theme.SMALL, Theme.MUTED);
    private final List<JsonObject> tatCa = new ArrayList<>();
    private final List<JsonObject> dangHien = new ArrayList<>();

    public QuanLySuKienPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Quản lý sự kiện", "Tạo, cập nhật, duyệt kế hoạch, phân công và theo dõi sự kiện"),
                BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 18));
        giua.setOpaque(false);
        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.add(theTong);
        hangThe.add(theSap);
        hangThe.add(theDangKy);
        hangThe.add(theCho);
        giua.add(hangThe, BorderLayout.NORTH);

        UI.RoundPanel the = UI.card(new BorderLayout(0, 12), 0);
        the.setBorder(new EmptyBorder(6, 16, 14, 16));
        tabBar = new UI.TabBar(i -> locVaHien(), "Tất cả sự kiện", "Kế hoạch chờ duyệt");
        UI.FlatButton btnTao = UI.nut("Tạo sự kiện", Icons.PLUS, UI.Kieu.CHINH);
        btnTao.addActionListener(e -> SuKienDialogs.formSuKien(this, null, dieuHuong.tenDangNhap(), this::taiLai));
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
        JPanel loc = new JPanel(new GridBagLayout());
        loc.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        oTim.setPreferredSize(new Dimension(300, 38));
        loc.add(oTim, g);
        g.weightx = 0;
        g.insets = new Insets(0, 12, 0, 0);
        cbTrangThai.setPreferredSize(new Dimension(180, 38));
        loc.add(cbTrangThai, g);
        noiDung.add(loc, BorderLayout.NORTH);

        bang.anCot(0);
        bang.cotHaiDong(1);
        bang.doRongCot(1, 230);
        bang.doRongCot(2, 115);
        bang.doRongCot(3, 85);
        bang.coDinhCot(4, 62);
        bang.cotNhan(5);
        bang.cotNhan(6);
        bang.coDinhCot(7, 182);
        bang.cotThaoTac(7, List.of(
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
        noiDung.add(bang.trongKhungCuon(), BorderLayout.CENTER);
        noiDung.add(lblSoLuong, BorderLayout.SOUTH);
        the.add(noiDung, BorderLayout.CENTER);
        giua.add(the, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);

        oTim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { locVaHien(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { locVaHien(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { locVaHien(); }
        });
        cbTrangThai.addActionListener(e -> locVaHien());
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
            theTong.setSo(tatCa.size());
            theSap.setSo(sap);
            theDangKy.setSo(dangKy);
            theCho.setSo(cho);
            tabBar.doiTen(1, cho > 0 ? "Kế hoạch chờ duyệt (" + cho + ")" : "Kế hoạch chờ duyệt");
            locVaHien();
        });
        dieuHuong.capNhatChuong();
    }

    /** Lọc ngay trên dữ liệu đã tải (số sự kiện ít nên không cần gọi lại API). */
    private void locVaHien() {
        String kw = oTim.getText().trim().toLowerCase();
        String tt = cbTrangThai.getSelectedIndex() <= 0 ? "" : (String) cbTrangThai.getSelectedItem();
        boolean chiCho = tabBar != null && tabBar.getChon() == 1;
        dangHien.clear();
        bang.xoaHet();
        for (JsonObject o : tatCa) {
            boolean laCho = "Chờ duyệt".equals(Api.s(o, "trangThaiDuyet"));
            if (chiCho && !laCho) continue;
            if (!tt.isEmpty() && !tt.equals(Api.s(o, "trangThai"))) continue;
            String tim = (Api.s(o, "tenSuKien") + " " + Api.s(o, "maSuKien") + " " + Api.s(o, "diaDiem")).toLowerCase();
            if (!kw.isEmpty() && !tim.contains(kw)) continue;
            dangHien.add(o);
            bang.themDong(Api.n(o, "id"), Api.s(o, "tenSuKien") + "\n" + Api.s(o, "maSuKien") + "  |  " + Api.s(o, "diaDiem"),
                    Api.s(o, "thoiGianBatDau"), Api.n(o, "soDangKy") + " / " + Api.n(o, "soLuongToiDa"),
                    String.valueOf(Api.n(o, "soCanBo")), Api.s(o, "trangThai"), Api.s(o, "trangThaiDuyet"), "");
        }
        lblSoLuong.setText(dangHien.isEmpty() ? "Không tìm thấy sự kiện phù hợp"
                : "Hiển thị " + dangHien.size() + " / " + tatCa.size() + " sự kiện");
    }
}
