package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Use case "Tra cứu sự kiện": xem danh sách, tìm kiếm, lọc theo ngày, xem chi tiết,
 * và đăng ký tham gia ngay trên từng dòng.
 */
public class TraCuuSuKien extends TrangSinhVien {

    private static final String[] TRANG_THAI = {"Tất cả trạng thái", "Sắp diễn ra", "Đang diễn ra", "Đã kết thúc", "Đã hủy"};

    private final UI.TextFieldDep oTim = UI.oTimKiem("Tìm theo tên sự kiện...");
    private final JDateChooser dcTuNgay = HocSinhUI.oChonNgay();
    private final JDateChooser dcDenNgay = HocSinhUI.oChonNgay();
    private final JComboBox<String> cbTrangThai = UI.hopChon(TRANG_THAI, Icons.CALENDAR);
    private final BangDep bang = new BangDep("id", "Sự kiện", "Bắt đầu", "Số chỗ", "Trạng thái", "Của tôi", "Thao tác");
    private final JLabel lblSoLuong = UI.nhan(" ", Theme.SMALL, Theme.MUTED);

    private final List<JsonObject> tatCa = new ArrayList<>();
    private final List<JsonObject> dangHien = new ArrayList<>();
    private final Map<Long, String> cuaToi = new HashMap<>();

    public TraCuuSuKien(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Tra cứu sự kiện", "Tìm sự kiện theo tên, khoảng ngày; xem chi tiết và đăng ký tham gia"),
                BorderLayout.NORTH);

        UI.RoundPanel the = UI.card(new BorderLayout(0, 14), 0);
        the.setBorder(new EmptyBorder(16, 16, 14, 16));

        // --- Thanh lọc ---
        JPanel loc = new JPanel(new GridBagLayout());
        loc.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        oTim.setPreferredSize(new Dimension(240, 38));
        loc.add(oTim, g);
        g.weightx = 0;
        g.insets = new Insets(0, 12, 0, 0);
        loc.add(UI.nhan("Từ", Theme.SMALL_BOLD, Theme.TEXT_2), g);
        g.insets = new Insets(0, 6, 0, 0);
        loc.add(dcTuNgay, g);
        g.insets = new Insets(0, 10, 0, 0);
        loc.add(UI.nhan("đến", Theme.SMALL_BOLD, Theme.TEXT_2), g);
        g.insets = new Insets(0, 6, 0, 0);
        loc.add(dcDenNgay, g);
        g.insets = new Insets(0, 12, 0, 0);
        cbTrangThai.setPreferredSize(new Dimension(170, 38));
        loc.add(cbTrangThai, g);
        UI.FlatButton btnTim = UI.nut("Tìm", Icons.SEARCH, UI.Kieu.CHINH);
        loc.add(btnTim, g);
        g.insets = new Insets(0, 8, 0, 0);
        UI.FlatButton btnLamMoi = UI.nutIcon(Icons.REFRESH, "Xóa bộ lọc");
        loc.add(btnLamMoi, g);
        the.add(loc, BorderLayout.NORTH);

        // --- Bảng ---
        bang.anCot(0);
        bang.cotHaiDong(1);
        bang.doRongCot(1, 300);
        bang.doRongCot(2, 130);
        bang.doRongCot(3, 90);
        HocSinhUI.cotNhan(bang, 4);
        HocSinhUI.cotNhan(bang, 5);
        bang.coDinhCot(6, 190);
        bang.cotThaoTac(6, List.of(
                        new BangDep.NutThaoTac("chitiet", "Chi tiết", Icons.EYE, "Xem chi tiết sự kiện"),
                        new BangDep.NutThaoTac("dangky", "Đăng ký", Icons.PLUS, "Đăng ký tham gia")),
                (ma, dong) -> !ma.equals("dangky") || (dong < dangHien.size() && coTheDangKy(dangHien.get(dong))),
                (ma, dong) -> {
                    JsonObject sk = dangHien.get(dong);
                    long id = Api.n(sk, "id");
                    if (ma.equals("chitiet")) {
                        ChiTietSuKien.hien(this, id, dieuHuong.tenDangNhap(), this::taiLai);
                    } else {
                        ChiTietSuKien.dangKy(this, id, Api.s(sk, "tenSuKien"), dieuHuong.tenDangNhap(), this::taiLai);
                    }
                });
        bang.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int r = bang.rowAtPoint(e.getPoint());
                if (e.getClickCount() == 2 && r >= 0) {
                    ChiTietSuKien.hien(TraCuuSuKien.this, bang.idDong(r), dieuHuong.tenDangNhap(), TraCuuSuKien.this::taiLai);
                }
            }
        });
        the.add(bang.trongKhungCuon(), BorderLayout.CENTER);
        the.add(lblSoLuong, BorderLayout.SOUTH);
        add(the, BorderLayout.CENTER);

        btnTim.addActionListener(e -> taiLai());
        oTim.addActionListener(e -> taiLai());
        cbTrangThai.addActionListener(e -> locVaHien());
        btnLamMoi.addActionListener(e -> {
            oTim.setText("");
            dcTuNgay.setDate(null);
            dcDenNgay.setDate(null);
            cbTrangThai.setSelectedIndex(0);
            taiLai();
        });
    }

    /** Ô tìm nhanh trên thanh trên cùng gọi vào đây trước khi mở trang. */
    public void datTuKhoa(String tuKhoa) {
        oTim.setText(tuKhoa == null ? "" : tuKhoa);
    }

    /** GET /api/su-kien/danh-sach (lọc tên + ngày ở backend) và /api/dang-ky/cua-toi (để biết mình đã đăng ký chưa). */
    @Override
    public void taiLai() {
        Date tu = dcTuNgay.getDate(), den = dcDenNgay.getDate();
        if (tu != null && den != null && tu.after(den)) {
            HopThoai.loi(this, "\"Từ ngày\" phải trước \"Đến ngày\"");
            return;
        }
        StringBuilder url = new StringBuilder("/api/su-kien/danh-sach?tuKhoa=").append(Api.e(oTim.getText().trim()));
        if (tu != null) url.append("&tuNgay=").append(HocSinhUI.isoNgay(tu));
        if (den != null) url.append("&denNgay=").append(HocSinhUI.isoNgay(den));
        String ten = Api.e(dieuHuong.tenDangNhap());

        Api.chayNen(this, () -> {
            JsonObject kq = new JsonObject();
            kq.add("suKien", Api.get(url.toString()));
            kq.add("dangKy", Api.get("/api/dang-ky/cua-toi?tenDangNhap=" + ten));
            return kq;
        }, kq -> {
            tatCa.clear();
            for (JsonElement el : Api.arr(kq, "suKien")) tatCa.add(el.getAsJsonObject());
            cuaToi.clear();
            for (JsonElement el : Api.arr(kq, "dangKy")) {
                JsonObject o = el.getAsJsonObject();
                cuaToi.put(Api.n(o, "suKienId"), Api.s(o, "trangThai"));
            }
            locVaHien();
        });
    }

    /** Lọc trạng thái ngay trên dữ liệu đã tải (không cần gọi lại API). */
    private void locVaHien() {
        String tt = cbTrangThai.getSelectedIndex() <= 0 ? "" : (String) cbTrangThai.getSelectedItem();
        dangHien.clear();
        bang.xoaHet();
        for (JsonObject sk : tatCa) {
            if (!tt.isEmpty() && !tt.equals(Api.s(sk, "trangThai"))) continue;
            dangHien.add(sk);
            long toiDa = Api.n(sk, "soLuongToiDa");
            bang.themDong(Api.n(sk, "id"),
                    Api.s(sk, "tenSuKien") + "\n" + Api.s(sk, "maSuKien") + "  |  " + Api.s(sk, "diaDiem"),
                    HocSinhUI.thoiGian(Api.s(sk, "thoiGianBatDau")),
                    Api.n(sk, "soDaDangKy") + " / " + (toiDa > 0 ? toiDa : "∞"),
                    Api.s(sk, "trangThai"),
                    cuaToi.getOrDefault(Api.n(sk, "id"), ""),
                    "");
        }
        lblSoLuong.setText(dangHien.isEmpty() ? "Không tìm thấy sự kiện phù hợp"
                : "Hiển thị " + dangHien.size() + " / " + tatCa.size() + " sự kiện  •  Nhấp đúp vào một dòng để xem chi tiết");
    }

    /** Chỉ hiện nút Đăng ký khi sự kiện còn mở, chưa bắt đầu, chưa đầy và mình chưa giữ chỗ. */
    private boolean coTheDangKy(JsonObject sk) {
        String toi = cuaToi.getOrDefault(Api.n(sk, "id"), "");
        long toiDa = Api.n(sk, "soLuongToiDa");
        return "Sắp diễn ra".equals(Api.s(sk, "trangThai"))
                && !HocSinhUI.daBatDau(Api.s(sk, "thoiGianBatDau"))
                && (toiDa <= 0 || Api.n(sk, "soDaDangKy") < toiDa)
                && (toi.isEmpty() || "Đã hủy".equals(toi));
    }
}