package com.eventmanager.fe.gui.HocSinh;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

/**
 * TRANG CHỦ SINH VIÊN:
 *  - 4 thẻ thống kê: sắp tham gia, đã tham gia, chờ đánh giá, sự kiện đang mở đăng ký
 *  - Sự kiện sắp diễn ra (bấm để xem chi tiết / đăng ký)
 *  - Hoạt động gần đây của tôi: đăng ký, tham gia, vắng, hủy, đánh giá
 */
public class TrangChuSinhVien extends TrangSinhVien {

    private final JLabel lblChao = UI.nhan("Xin chào!", Theme.H1, Theme.TEXT);
    private final UI.StatCard theSapThamGia = new UI.StatCard("Sắp tham gia", Icons.CALENDAR, Theme.PRIMARY, Theme.PRIMARY_SOFT);
    private final UI.StatCard theDaThamGia = new UI.StatCard("Đã tham gia", Icons.CHECK_CIRCLE, Theme.SUCCESS, Theme.SUCCESS_SOFT);
    private final UI.StatCard theChoDanhGia = new UI.StatCard("Chờ đánh giá", Icons.EDIT, Theme.WARNING, Theme.WARNING_SOFT);
    private final UI.StatCard theDangMo = new UI.StatCard("Đang mở đăng ký", Icons.LIST, Theme.PURPLE, Theme.PURPLE_SOFT);
    private final JPanel dsSapToi = new JPanel();
    private final JPanel dsHoatDong = new JPanel();

    public TrangChuSinhVien(DieuHuong dieuHuong) {
        super(dieuHuong);

        JPanel dau = new JPanel();
        dau.setOpaque(false);
        dau.setLayout(new BoxLayout(dau, BoxLayout.Y_AXIS));
        lblChao.setAlignmentX(LEFT_ALIGNMENT);
        JLabel moTa = UI.nhan("Theo dõi các sự kiện của Liên Chi đoàn CNTT và hoạt động của bạn", Theme.BODY, Theme.MUTED);
        moTa.setAlignmentX(LEFT_ALIGNMENT);
        dau.add(lblChao);
        dau.add(Box.createVerticalStrut(4));
        dau.add(moTa);
        add(dau, BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 18));
        giua.setOpaque(false);
        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.add(theSapThamGia);
        hangThe.add(theDaThamGia);
        hangThe.add(theChoDanhGia);
        hangThe.add(theDangMo);
        giua.add(hangThe, BorderLayout.NORTH);

        for (JPanel ds : new JPanel[]{dsSapToi, dsHoatDong}) {
            ds.setOpaque(false);
            ds.setLayout(new BoxLayout(ds, BoxLayout.Y_AXIS));
        }
        UI.FlatButton xemTatCa = UI.nut("Xem tất cả", Icons.CHEVRON_RIGHT, UI.Kieu.NHAT);
        xemTatCa.addActionListener(e -> dieuHuong.moTrang(DashboardHocSinh.TRA_CUU));
        UI.FlatButton xemCuaToi = UI.nut("Sự kiện của tôi", Icons.CHEVRON_RIGHT, UI.Kieu.NHAT);
        xemCuaToi.addActionListener(e -> dieuHuong.moTrang(DashboardHocSinh.CUA_TOI));

        JPanel haiCot = new JPanel(new GridLayout(1, 2, 18, 0));
        haiCot.setOpaque(false);
        haiCot.add(HocSinhUI.the("Sự kiện sắp diễn ra", xemTatCa, UI.cuon(dsSapToi)));
        haiCot.add(HocSinhUI.the("Hoạt động gần đây", xemCuaToi, UI.cuon(dsHoatDong)));
        giua.add(haiCot, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);
    }

    @Override
    public void taiLai() {
        String ten = Api.e(dieuHuong.tenDangNhap());
        Api.chayNen(this, () -> {
            JsonObject kq = new JsonObject();
            kq.add("suKien", Api.get("/api/su-kien/danh-sach"));
            kq.add("dangKy", Api.get("/api/dang-ky/cua-toi?tenDangNhap=" + ten));
            kq.add("danhGia", Api.get("/api/danh-gia/cua-toi?tenDangNhap=" + ten));
            return kq;
        }, this::hienThi);
    }

    private void hienThi(JsonObject kq) {
        lblChao.setText("Xin chào, " + dieuHuong.tenDangNhap() + "!");

        JsonArray dangKy = Api.arr(kq, "dangKy");
        JsonArray danhGia = Api.arr(kq, "danhGia");
        Set<Long> daDanhGia = new HashSet<>();
        for (JsonElement el : danhGia) daDanhGia.add(Api.n(el.getAsJsonObject(), "suKienId"));

        // Trạng thái của tôi theo từng sự kiện + số liệu thẻ
        Map<Long, String> cuaToi = new HashMap<>();
        long sapThamGia = 0, daThamGia = 0, choDanhGia = 0;
        for (JsonElement el : dangKy) {
            JsonObject o = el.getAsJsonObject();
            String tt = Api.s(o, "trangThai");
            cuaToi.put(Api.n(o, "suKienId"), tt);
            if ("Đã đăng ký".equals(tt) && !HocSinhUI.daBatDau(Api.s(o, "thoiGianBatDau"))) sapThamGia++;
            if ("Đã tham gia".equals(tt)) {
                daThamGia++;
                if (!daDanhGia.contains(Api.n(o, "suKienId"))) choDanhGia++;
            }
        }

        // Sự kiện sắp diễn ra (5 cái gần nhất) + số sự kiện còn mở đăng ký
        dsSapToi.removeAll();
        long dangMo = 0;
        int daHien = 0;
        for (JsonElement el : Api.arr(kq, "suKien")) {
            JsonObject sk = el.getAsJsonObject();
            if (!"Sắp diễn ra".equals(Api.s(sk, "trangThai")) || HocSinhUI.daBatDau(Api.s(sk, "thoiGianBatDau"))) continue;
            dangMo++;
            if (daHien < 5) {
                dsSapToi.add(dongSuKien(sk, cuaToi.getOrDefault(Api.n(sk, "id"), "")));
                daHien++;
            }
        }
        if (daHien == 0) dsSapToi.add(HocSinhUI.trong("Chưa có sự kiện nào sắp diễn ra"));

        theSapThamGia.setSo(sapThamGia);
        theDaThamGia.setSo(daThamGia);
        theChoDanhGia.setSo(choDanhGia);
        theDangMo.setSo(dangMo);

        // Hoạt động gần đây: gộp lượt đăng ký + đánh giá, mới nhất lên đầu
        List<HoatDong> hoatDong = new ArrayList<>();
        for (JsonElement el : dangKy) {
            JsonObject o = el.getAsJsonObject();
            String tt = Api.s(o, "trangThai");
            String ten = Api.s(o, "tenSuKien");
            String moTa = switch (tt) {
                case "Đã tham gia" -> "Đã tham gia sự kiện";
                case "Vắng mặt" -> "Vắng mặt tại sự kiện";
                case "Đã hủy" -> "Đã hủy đăng ký";
                default -> "Đăng ký tham gia";
            };
            // Tham gia / vắng tính theo ngày diễn ra; đăng ký / hủy tính theo ngày đăng ký
            boolean theoNgaySuKien = "Đã tham gia".equals(tt) || "Vắng mặt".equals(tt);
            String luc = Api.s(o, theoNgaySuKien ? "thoiGianBatDau" : "ngayDangKy");
            hoatDong.add(new HoatDong(HocSinhUI.docThoiGian(luc), luc, moTa, ten, tt, 0));
        }
        for (JsonElement el : danhGia) {
            JsonObject o = el.getAsJsonObject();
            String luc = Api.s(o, "ngayDanhGia");
            hoatDong.add(new HoatDong(HocSinhUI.docThoiGian(luc), luc, "Đã đánh giá",
                    Api.s(o, "tenSuKien"), null, (int) Api.n(o, "soSao")));
        }
        hoatDong.removeIf(h -> h.luc == null);
        hoatDong.sort(Comparator.comparing((HoatDong h) -> h.luc).reversed());

        dsHoatDong.removeAll();
        hoatDong.stream().limit(8).forEach(h -> dsHoatDong.add(dongHoatDong(h)));
        if (hoatDong.isEmpty()) dsHoatDong.add(HocSinhUI.trong("Bạn chưa có hoạt động nào"));

        revalidate();
        repaint();
    }

    /** Một dòng sự kiện: ô ngày | tên + giờ, địa điểm | nhãn trạng thái của tôi. Bấm để xem chi tiết. */
    private JComponent dongSuKien(JsonObject sk, String trangThaiToi) {
        LocalDateTime batDau = HocSinhUI.docThoiGian(Api.s(sk, "thoiGianBatDau"));
        UI.RoundPanel dong = new UI.RoundPanel(new BorderLayout(14, 0), 12, Color.WHITE, null);
        dong.setBorder(new EmptyBorder(8, 8, 8, 8));
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        dong.setAlignmentX(LEFT_ALIGNMENT);

        // Ô ngày kiểu tờ lịch
        UI.RoundPanel oNgay = new UI.RoundPanel(new GridLayout(2, 1), 10, Theme.PRIMARY_SOFT, null);
        oNgay.setPreferredSize(new Dimension(52, 48));
        JLabel ngay = UI.nhan(batDau == null ? "--" : String.valueOf(batDau.getDayOfMonth()), Theme.font(Font.BOLD, 17), Theme.PRIMARY);
        JLabel thang = UI.nhan(batDau == null ? "" : "Th " + batDau.getMonthValue(), Theme.SMALL, Theme.PRIMARY);
        ngay.setHorizontalAlignment(SwingConstants.CENTER);
        thang.setHorizontalAlignment(SwingConstants.CENTER);
        ngay.setVerticalAlignment(SwingConstants.BOTTOM);
        thang.setVerticalAlignment(SwingConstants.TOP);
        oNgay.add(ngay);
        oNgay.add(thang);
        dong.add(oNgay, BorderLayout.WEST);

        JPanel chu = new JPanel();
        chu.setOpaque(false);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        chu.add(Box.createVerticalGlue());
        chu.add(UI.nhan(Api.s(sk, "tenSuKien"), Theme.BODY_BOLD, Theme.TEXT));
        chu.add(Box.createVerticalStrut(3));
        String gio = batDau == null ? "" : String.format("%02d:%02d", batDau.getHour(), batDau.getMinute());
        chu.add(UI.nhan(gio + "  •  " + Api.s(sk, "diaDiem"), Theme.SMALL, Theme.MUTED));
        chu.add(Box.createVerticalGlue());
        dong.add(chu, BorderLayout.CENTER);
        if (!trangThaiToi.isEmpty()) {
            JPanel boc = new JPanel(new GridBagLayout());
            boc.setOpaque(false);
            boc.add(new HocSinhUI.Nhan(trangThaiToi));
            dong.add(boc, BorderLayout.EAST);
        }

        dong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dong.setToolTipText("Bấm để xem chi tiết");
        dong.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ChiTietSuKien.hien(TrangChuSinhVien.this, Api.n(sk, "id"), dieuHuong.tenDangNhap(), TrangChuSinhVien.this::taiLai);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                dong.setNen(Theme.ROW_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                dong.setNen(Color.WHITE);
            }
        });
        return dong;
    }

    private record HoatDong(LocalDateTime luc, String lucIso, String hanhDong, String tenSuKien, String trangThai, int soSao) {
    }

    /** Một dòng hoạt động: chấm icon tròn | "Hành động" + tên sự kiện | thời gian. */
    private JComponent dongHoatDong(HoatDong h) {
        JPanel dong = new JPanel(new BorderLayout(12, 0));
        dong.setOpaque(false);
        dong.setBorder(new EmptyBorder(8, 4, 8, 4));
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        dong.setAlignmentX(LEFT_ALIGNMENT);

        String icon;
        Color[] mau;
        if (h.soSao > 0) {
            icon = Icons.EDIT;
            mau = new Color[]{Theme.WARNING, Theme.WARNING_SOFT};
        } else {
            mau = HocSinhUI.mau(h.trangThai);
            icon = switch (h.trangThai) {
                case "Đã tham gia" -> Icons.CHECK_CIRCLE;
                case "Vắng mặt", "Đã hủy" -> Icons.X;
                default -> Icons.CALENDAR;
            };
        }
        JLabel tron = new JLabel(Icons.of(icon, 18, mau[0])) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.muot(g);
                g2.setColor(mau[1]);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tron.setPreferredSize(new Dimension(38, 38));
        tron.setHorizontalAlignment(SwingConstants.CENTER);
        JPanel bocTron = new JPanel(new GridBagLayout());
        bocTron.setOpaque(false);
        bocTron.add(tron);
        dong.add(bocTron, BorderLayout.WEST);

        JPanel chu = new JPanel();
        chu.setOpaque(false);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        JPanel dongDau = UI.hang(6, UI.nhan(h.hanhDong, Theme.SMALL_BOLD, mau[0]));
        ((FlowLayout) dongDau.getLayout()).setHgap(0);
        if (h.soSao > 0) {
            SaoDanhGia sao = new SaoDanhGia(12, false);
            sao.setGiaTri(h.soSao);
            dongDau.add(Box.createHorizontalStrut(8));
            dongDau.add(sao);
        }
        dongDau.setAlignmentX(LEFT_ALIGNMENT);
        JLabel ten = UI.nhan(h.tenSuKien, Theme.BODY, Theme.TEXT);
        ten.setAlignmentX(LEFT_ALIGNMENT);
        chu.add(dongDau);
        chu.add(Box.createVerticalStrut(2));
        chu.add(ten);
        dong.add(chu, BorderLayout.CENTER);

        JLabel luc = UI.nhan(HocSinhUI.truoc(h.lucIso), Theme.SMALL, Theme.MUTED);
        luc.setToolTipText(HocSinhUI.thoiGian(h.lucIso));
        dong.add(luc, BorderLayout.EAST);
        return dong;
    }
}