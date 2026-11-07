package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;

/**
 * Hộp thoại "Xem chi tiết sự kiện" + các thao tác Đăng ký / Hủy đăng ký dùng chung cho mọi trang.
 * Lấy dữ liệu từ 3 API: chi tiết sự kiện, trạng thái của tôi, tổng hợp đánh giá.
 */
final class ChiTietSuKien {

    private ChiTietSuKien() {
    }

    static void hien(Component cha, long suKienId, String tenDangNhap, Runnable sauKhiDoi) {
        Api.chayNen(cha, () -> {
            JsonObject ct = Api.obj(Api.get("/api/su-kien/" + suKienId));
            ct.add("cuaToi", Api.get("/api/dang-ky/trang-thai?suKienId=" + suKienId + "&tenDangNhap=" + Api.e(tenDangNhap)));
            ct.add("danhGia", Api.get("/api/danh-gia/su-kien/" + suKienId));
            return ct;
        }, ct -> dungHopThoai(cha, ct, tenDangNhap, sauKhiDoi));
    }

    private static void dungHopThoai(Component cha, JsonObject ct, String tenDangNhap, Runnable sauKhiDoi) {
        JsonObject sk = Api.obj(ct, "suKien");
        JsonObject toi = Api.obj(ct, "cuaToi");
        JsonObject dg = Api.obj(ct, "danhGia");
        String ten = Api.s(sk, "tenSuKien");
        String trangThaiToi = Api.s(toi, "trangThai");

        HopThoai.FormDialog d = new HopThoai.FormDialog(cha, ten,
                Api.s(sk, "maSuKien") + "  •  " + Api.s(sk, "donViToChuc"), 580);

        d.khoi(UI.hang(8, UI.nhan("Sự kiện:", Theme.SMALL_BOLD, Theme.TEXT_2), new HocSinhUI.Nhan(Api.s(sk, "trangThai")),
                Box.createHorizontalStrut(10),
                UI.nhan("Bạn:", Theme.SMALL_BOLD, Theme.TEXT_2), new HocSinhUI.Nhan(trangThaiToi)));
        d.truong("Bắt đầu", HopThoai.oChiXem(HocSinhUI.thoiGian(Api.s(sk, "thoiGianBatDau"))), false);
        d.truong("Kết thúc", HopThoai.oChiXem(HocSinhUI.thoiGian(Api.s(sk, "thoiGianKetThuc"))), false);
        d.truong("Địa điểm", HopThoai.oChiXem(Api.s(sk, "diaDiem")), true);

        // Số chỗ: "12 / 40 chỗ" + thanh tiến độ
        long da = Api.n(ct, "soDaDangKy"), toiDa = Api.n(sk, "soLuongToiDa");
        JPanel cho = new JPanel(new BorderLayout(10, 0));
        cho.setOpaque(false);
        cho.add(UI.nhan(da + " / " + (toiDa > 0 ? toiDa : "∞") + " chỗ", Theme.BODY_BOLD, Theme.TEXT), BorderLayout.WEST);
        cho.add(UI.thanhTienDo(toiDa > 0 ? (double) da / toiDa : 0, da >= toiDa && toiDa > 0 ? Theme.DANGER : Theme.PRIMARY),
                BorderLayout.CENTER);
        d.truong("Đã đăng ký", cho, false);

        // Đánh giá trung bình bằng ngôi sao
        long soLuot = Api.n(dg, "soLuot");
        SaoDanhGia sao = new SaoDanhGia(18, false);
        sao.setGiaTri((int) Math.round(Api.d(dg, "diemTrungBinh")));
        d.truong("Đánh giá của sinh viên", soLuot == 0 ? UI.nhan("Chưa có đánh giá", Theme.BODY, Theme.MUTED)
                : UI.hang(8, sao, UI.nhan(Api.s(dg, "diemTrungBinh") + "/5  (" + soLuot + " lượt)", Theme.SMALL, Theme.MUTED)), false);

        JTextArea moTa = UI.vungChu(4);
        moTa.setText(Api.s(sk, "moTa"));
        moTa.setEditable(false);
        moTa.setCaretPosition(0);
        d.truong("Mô tả", UI.cuonVungChu(moTa), true);

        JButton dong = UI.nut("Đóng", UI.Kieu.VIEN);
        dong.addActionListener(e -> d.dispose());
        d.nut(dong);

        boolean chuaBatDau = !HocSinhUI.daBatDau(Api.s(sk, "thoiGianBatDau"));
        boolean moDangKy = "Sắp diễn ra".equals(Api.s(sk, "trangThai")) && chuaBatDau;
        if (moDangKy && ("Chưa đăng ký".equals(trangThaiToi) || "Đã hủy".equals(trangThaiToi))) {
            JButton dk = UI.nut("Đăng ký tham gia", Icons.PLUS, UI.Kieu.CHINH);
            dk.addActionListener(e -> dangKy(d, Api.n(sk, "id"), ten, tenDangNhap, () -> {
                d.dispose();
                sauKhiDoi.run();
            }));
            d.nut(dk);
        } else if ("Đã đăng ký".equals(trangThaiToi) && chuaBatDau) {
            JButton huy = UI.nut("Hủy đăng ký", Icons.X, UI.Kieu.NGUY_HIEM);
            huy.addActionListener(e -> huyDangKy(d, Api.n(toi, "id"), ten, tenDangNhap, () -> {
                d.dispose();
                sauKhiDoi.run();
            }));
            d.nut(huy);
        }
        d.hien();
    }

    /** POST /api/dang-ky/dang-ky. Backend kiểm tra trùng, hết chỗ, sự kiện đã bắt đầu... */
    static void dangKy(Component cha, long suKienId, String tenSuKien, String tenDangNhap, Runnable xong) {
        if (!HopThoai.xacNhan(cha, "Đăng ký tham gia", "Bạn muốn đăng ký tham gia <b>" + tenSuKien + "</b>?",
                "Đăng ký", UI.Kieu.CHINH)) {
            return;
        }
        JsonObject body = new JsonObject();
        body.addProperty("suKienId", suKienId);
        body.addProperty("tenDangNhap", tenDangNhap);
        Api.chayNen(cha, () -> Api.post("/api/dang-ky/dang-ky", body), kq -> {
            HopThoai.thanhCong(cha, "Đăng ký thành công!");
            xong.run();
        });
    }

    /** PUT /api/dang-ky/huy/{id}. Chỉ hủy được khi sự kiện chưa bắt đầu. */
    static void huyDangKy(Component cha, long dangKyId, String tenSuKien, String tenDangNhap, Runnable xong) {
        if (!HopThoai.xacNhan(cha, "Hủy đăng ký", "Bạn chắc chắn muốn hủy đăng ký <b>" + tenSuKien + "</b>?",
                "Hủy đăng ký", UI.Kieu.NGUY_HIEM)) {
            return;
        }
        Api.chayNen(cha, () -> Api.put("/api/dang-ky/huy/" + dangKyId + "?tenDangNhap=" + Api.e(tenDangNhap), null), kq -> {
            HopThoai.thanhCong(cha, "Đã hủy đăng ký");
            xong.run();
        });
    }
}
