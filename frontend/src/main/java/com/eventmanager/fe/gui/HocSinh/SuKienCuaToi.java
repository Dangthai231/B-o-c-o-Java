package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Use case "Đăng ký tham gia sự kiện": xem thông tin đăng ký, hủy đăng ký, xem trạng thái tham gia.
 * Chia tab: Tất cả / Sắp tới / Đã tham gia / Vắng - Đã hủy.
 */
public class SuKienCuaToi extends TrangSinhVien {

    private final UI.StatCard theTong = new UI.StatCard("Tổng lượt đăng ký", Icons.LIST, Theme.PRIMARY, Theme.PRIMARY_SOFT);
    private final UI.StatCard theSapToi = new UI.StatCard("Sắp tham gia", Icons.CLOCK, Theme.PURPLE, Theme.PURPLE_SOFT);
    private final UI.StatCard theThamGia = new UI.StatCard("Đã tham gia", Icons.CHECK_CIRCLE, Theme.SUCCESS, Theme.SUCCESS_SOFT);
    private final UI.StatCard theVang = new UI.StatCard("Vắng / Đã hủy", Icons.X, Theme.DANGER, Theme.DANGER_SOFT);

    private final UI.TabBar tabBar;
    // Cột 0 = id đăng ký (ẩn)
    private final BangDep bang = new BangDep("id", "Sự kiện", "Bắt đầu", "Ngày đăng ký", "Trạng thái", "Thao tác");
    private final JLabel lblSoLuong = UI.nhan(" ", Theme.SMALL, Theme.MUTED);

    private final List<JsonObject> tatCa = new ArrayList<>();
    private final List<JsonObject> dangHien = new ArrayList<>();
    private final Set<Long> daDanhGia = new HashSet<>();

    public SuKienCuaToi(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Sự kiện của tôi", "Các sự kiện bạn đã đăng ký, trạng thái tham gia và hủy đăng ký"),
                BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 18));
        giua.setOpaque(false);
        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.add(theTong);
        hangThe.add(theSapToi);
        hangThe.add(theThamGia);
        hangThe.add(theVang);
        giua.add(hangThe, BorderLayout.NORTH);

        UI.RoundPanel the = UI.card(new BorderLayout(0, 12), 0);
        the.setBorder(new EmptyBorder(6, 16, 14, 16));
        tabBar = new UI.TabBar(i -> locVaHien(), "Tất cả", "Sắp tới", "Đã tham gia", "Vắng / Đã hủy");
        UI.FlatButton btnTim = UI.nut("Tìm thêm sự kiện", Icons.SEARCH, UI.Kieu.CHINH);
        btnTim.addActionListener(e -> dieuHuong.moTrang(DashboardHocSinh.TRA_CUU));
        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.add(tabBar, BorderLayout.CENTER);
        JPanel bocNut = new JPanel(new GridBagLayout());
        bocNut.setOpaque(false);
        bocNut.add(btnTim);
        dau.add(bocNut, BorderLayout.EAST);
        the.add(dau, BorderLayout.NORTH);

        bang.anCot(0);
        bang.cotHaiDong(1);
        bang.doRongCot(1, 300);
        bang.doRongCot(2, 130);
        bang.doRongCot(3, 130);
        HocSinhUI.cotNhan(bang, 4);
        bang.coDinhCot(5, 200);
        bang.cotThaoTac(5, List.of(
                        new BangDep.NutThaoTac("chitiet", "Chi tiết", Icons.EYE, "Xem chi tiết sự kiện"),
                        new BangDep.NutThaoTac("danhgia", "Đánh giá", Icons.EDIT, "Đánh giá sự kiện đã tham gia"),
                        new BangDep.NutThaoTac("huy", "Hủy", Icons.X, "Hủy đăng ký")),
                (ma, dong) -> {
                    if (dong >= dangHien.size()) return false;
                    JsonObject o = dangHien.get(dong);
                    String tt = Api.s(o, "trangThai");
                    return switch (ma) {
                        case "danhgia" -> "Đã tham gia".equals(tt);
                        case "huy" -> "Đã đăng ký".equals(tt) && !HocSinhUI.daBatDau(Api.s(o, "thoiGianBatDau"));
                        default -> true;
                    };
                },
                (ma, dong) -> {
                    JsonObject o = dangHien.get(dong);
                    switch (ma) {
                        case "chitiet" -> ChiTietSuKien.hien(this, Api.n(o, "suKienId"), dieuHuong.tenDangNhap(), this::taiLai);
                        case "danhgia" -> dieuHuong.danhGia(Api.n(o, "suKienId"));
                        case "huy" -> ChiTietSuKien.huyDangKy(this, Api.n(o, "id"), Api.s(o, "tenSuKien"),
                                dieuHuong.tenDangNhap(), this::taiLai);
                        default -> {
                        }
                    }
                });
        JPanel noiDung = new JPanel(new BorderLayout(0, 10));
        noiDung.setOpaque(false);
        noiDung.add(bang.trongKhungCuon(), BorderLayout.CENTER);
        noiDung.add(lblSoLuong, BorderLayout.SOUTH);
        the.add(noiDung, BorderLayout.CENTER);
        giua.add(the, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);
    }

    /** GET /api/dang-ky/cua-toi và /api/danh-gia/cua-toi (để biết sự kiện nào đã đánh giá). */
    @Override
    public void taiLai() {
        String ten = Api.e(dieuHuong.tenDangNhap());
        Api.chayNen(this, () -> {
            JsonObject kq = new JsonObject();
            kq.add("dangKy", Api.get("/api/dang-ky/cua-toi?tenDangNhap=" + ten));
            kq.add("danhGia", Api.get("/api/danh-gia/cua-toi?tenDangNhap=" + ten));
            return kq;
        }, kq -> {
            tatCa.clear();
            long sapToi = 0, thamGia = 0, vang = 0;
            for (JsonElement el : Api.arr(kq, "dangKy")) {
                JsonObject o = el.getAsJsonObject();
                tatCa.add(o);
                switch (nhom(o)) {
                    case 1 -> sapToi++;
                    case 2 -> thamGia++;
                    case 3 -> vang++;
                    default -> {
                    }
                }
            }
            daDanhGia.clear();
            for (JsonElement el : Api.arr(kq, "danhGia")) daDanhGia.add(Api.n(el.getAsJsonObject(), "suKienId"));

            theTong.setSo(tatCa.size());
            theSapToi.setSo(sapToi);
            theThamGia.setSo(thamGia);
            theVang.setSo(vang);
            tabBar.doiTen(1, "Sắp tới (" + sapToi + ")");
            locVaHien();
        });
    }

    /** 1 = sắp tới, 2 = đã tham gia, 3 = vắng / đã hủy, 0 = khác (đã đăng ký nhưng sự kiện đã qua). */
    private static int nhom(JsonObject o) {
        String tt = Api.s(o, "trangThai");
        if ("Đã tham gia".equals(tt)) return 2;
        if ("Vắng mặt".equals(tt) || "Đã hủy".equals(tt)) return 3;
        if ("Đã đăng ký".equals(tt) && !HocSinhUI.daBatDau(Api.s(o, "thoiGianBatDau"))) return 1;
        return 0;
    }

    private void locVaHien() {
        int tab = tabBar == null ? 0 : tabBar.getChon();
        dangHien.clear();
        bang.xoaHet();
        for (JsonObject o : tatCa) {
            if (tab != 0 && nhom(o) != tab) continue;
            dangHien.add(o);
            String tt = Api.s(o, "trangThai");
            // Đã tham gia mà chưa đánh giá -> nhắc bằng nhãn "Chờ đánh giá"
            if ("Đã tham gia".equals(tt) && !daDanhGia.contains(Api.n(o, "suKienId"))) tt = "Chờ đánh giá";
            bang.themDong(Api.n(o, "id"),
                    Api.s(o, "tenSuKien") + "\n" + Api.s(o, "maSuKien") + "  |  " + Api.s(o, "diaDiem"),
                    HocSinhUI.thoiGian(Api.s(o, "thoiGianBatDau")),
                    HocSinhUI.thoiGian(Api.s(o, "ngayDangKy")),
                    tt, "");
        }
        lblSoLuong.setText(dangHien.isEmpty() ? "Chưa có sự kiện nào ở mục này"
                : "Hiển thị " + dangHien.size() + " / " + tatCa.size() + " lượt đăng ký");
    }
}