package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * MÀN HÌNH "TỔNG QUAN": nhìn nhanh số liệu + các việc đang chờ Admin xử lý.
 */
public class TongQuanPanel extends TrangAdmin {

    private final UI.StatCard theTk = new UI.StatCard("Tổng tài khoản", Icons.USERS, Theme.PRIMARY, Theme.PRIMARY_SOFT);
    private final UI.StatCard theSk = new UI.StatCard("Tổng sự kiện", Icons.CALENDAR, Theme.PURPLE, Theme.PURPLE_SOFT);
    private final UI.StatCard theSap = new UI.StatCard("Sắp diễn ra", Icons.CLOCK, Theme.SUCCESS, Theme.SUCCESS_SOFT);
    private final UI.StatCard theTb = new UI.StatCard("Thông báo đã đăng", Icons.BELL, Theme.WARNING, Theme.WARNING_SOFT);

    private final BangDep bangSapToi = new BangDep("id", "Sự kiện", "Bắt đầu", "Đăng ký");
    private final ViecCanLam viecKeHoach, viecThongBao, viecYeuCau;
    private final JLabel lblTongViec = UI.nhan(" ", Theme.SMALL, Theme.MUTED);

    public TongQuanPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        String ngay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        add(UI.tieuDeTrang("Tổng quan", "Hôm nay " + ngay + ". Tình hình hoạt động của Liên chi đoàn"), BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 18));
        giua.setOpaque(false);
        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.add(theTk);
        hangThe.add(theSk);
        hangThe.add(theSap);
        hangThe.add(theTb);
        giua.add(hangThe, BorderLayout.NORTH);

        JPanel duoi = new JPanel(new BorderLayout(18, 0));
        duoi.setOpaque(false);

        // Trái: sự kiện sắp diễn ra
        UI.RoundPanel trai = UI.card(new BorderLayout(0, 12), 18);
        JPanel dauTrai = new JPanel(new BorderLayout());
        dauTrai.setOpaque(false);
        dauTrai.add(UI.nhan("Sự kiện sắp diễn ra", Theme.H2, Theme.TEXT), BorderLayout.WEST);
        UI.FlatButton xemHet = UI.nut("Xem tất cả", Icons.CHEVRON_RIGHT, UI.Kieu.NHAT);
        xemHet.setHorizontalTextPosition(SwingConstants.LEFT);
        xemHet.addActionListener(e -> dieuHuong.moTrang(DashboardAdmin.SU_KIEN));
        dauTrai.add(xemHet, BorderLayout.EAST);
        trai.add(dauTrai, BorderLayout.NORTH);
        bangSapToi.anCot(0);
        bangSapToi.cotHaiDong(1);
        bangSapToi.doRongCot(1, 300);
        bangSapToi.doRongCot(2, 120);
        bangSapToi.doRongCot(3, 150);
        bangSapToi.getColumnModel().getColumn(3).setCellRenderer((t, v, s, f, r, c) -> {
            String[] p = (v == null ? "0/1" : v.toString()).split("/");
            double daDk = Double.parseDouble(p[0]), toiDa = Math.max(1, Double.parseDouble(p[1]));
            JPanel o = new JPanel(new BorderLayout(0, 4));
            o.setBorder(new EmptyBorder(8, 14, 8, 14));
            o.setBackground(bangSapToi.nenDong(r, s));
            o.add(UI.nhan((int) daDk + " / " + (int) toiDa, Theme.SMALL_BOLD, Theme.TEXT_2), BorderLayout.NORTH);
            o.add(UI.thanhTienDo(daDk / toiDa, Theme.PRIMARY), BorderLayout.CENTER);
            return o;
        });
        trai.add(bangSapToi.trongKhungCuon(), BorderLayout.CENTER);
        duoi.add(trai, BorderLayout.CENTER);

        // Phải: việc cần xử lý
        UI.RoundPanel phai = UI.card(new BorderLayout(0, 12), 18);
        phai.setPreferredSize(new Dimension(330, 0));
        JPanel dauPhai = new JPanel(new GridLayout(2, 1, 0, 2));
        dauPhai.setOpaque(false);
        dauPhai.add(UI.nhan("Việc cần xử lý", Theme.H2, Theme.TEXT));
        dauPhai.add(lblTongViec);
        phai.add(dauPhai, BorderLayout.NORTH);
        JPanel ds = new JPanel(new GridLayout(3, 1, 0, 10));
        ds.setOpaque(false);
        viecKeHoach = new ViecCanLam("Kế hoạch sự kiện chờ duyệt", Icons.FILE, Theme.WARNING, Theme.WARNING_SOFT,
                () -> dieuHuong.moTrang(DashboardAdmin.SU_KIEN));
        viecThongBao = new ViecCanLam("Thông báo chờ duyệt", Icons.BELL, Theme.PRIMARY, Theme.PRIMARY_SOFT,
                () -> dieuHuong.moTrang(DashboardAdmin.THONG_BAO));
        viecYeuCau = new ViecCanLam("Yêu cầu đổi thông tin", Icons.USER, Theme.PURPLE, Theme.PURPLE_SOFT,
                () -> dieuHuong.moTrang(DashboardAdmin.TAI_KHOAN));
        ds.add(viecKeHoach);
        ds.add(viecThongBao);
        ds.add(viecYeuCau);
        JPanel boc = new JPanel(new BorderLayout());
        boc.setOpaque(false);
        boc.add(ds, BorderLayout.NORTH);
        phai.add(boc, BorderLayout.CENTER);
        duoi.add(phai, BorderLayout.EAST);

        giua.add(duoi, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);
    }

    /** 1 dòng "việc cần làm": icon + tên + số lượng, bấm để chuyển tới màn hình xử lý. */
    private static class ViecCanLam extends UI.RoundPanel {
        private final JLabel so = UI.nhan("0", Theme.font(Font.BOLD, 20), Theme.TEXT);

        ViecCanLam(String ten, String icon, Color mau, Color nen, Runnable khiBam) {
            super(new BorderLayout(12, 0), 12, Theme.TABLE_HEAD, Theme.BORDER);
            setBorder(new EmptyBorder(14, 14, 14, 14));
            JComponent tron = new JComponent() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = Theme.muot(g);
                    g2.setColor(nen);
                    g2.fillRoundRect(0, 0, 40, 40, 12, 12);
                    g2.dispose();
                    Icons.of(icon, 20, mau).paintIcon(this, g, 10, 10);
                }
            };
            tron.setPreferredSize(new Dimension(40, 40));
            add(tron, BorderLayout.WEST);
            JPanel chu = new JPanel(new GridLayout(2, 1));
            chu.setOpaque(false);
            chu.add(UI.nhan(ten, Theme.BODY_BOLD, Theme.TEXT));
            chu.add(UI.nhan("Bấm để xử lý", Theme.SMALL, Theme.MUTED));
            add(chu, BorderLayout.CENTER);
            add(so, BorderLayout.EAST);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    khiBam.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setNen(Theme.ROW_HOVER);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setNen(Theme.TABLE_HEAD);
                }
            });
        }

        void setSo(long n) {
            so.setText(String.valueOf(n));
            so.setForeground(n > 0 ? Theme.DANGER : Theme.MUTED);
        }
    }

    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/tong-quan"), kq -> {
            JsonObject o = Api.obj(kq);
            theTk.setSo(Api.n(o, "tongTaiKhoan"));
            theSk.setSo(Api.n(o, "tongSuKien"));
            theSap.setSo(Api.n(o, "suKienSapDienRa"));
            theTb.setSo(Api.n(o, "thongBaoDaDang"));
            viecKeHoach.setSo(Api.n(o, "keHoachChoDuyet"));
            viecThongBao.setSo(Api.n(o, "thongBaoChoDuyet"));
            viecYeuCau.setSo(Api.n(o, "yeuCauChoDuyet"));
            long tong = Api.n(o, "tongViecCanXuLy");
            lblTongViec.setText(tong == 0 ? "Không có việc nào đang chờ" : tong + " việc đang chờ bạn xử lý");
            bangSapToi.xoaHet();
            for (JsonElement el : Api.arr(o, "suKienSapToi")) {
                JsonObject s = el.getAsJsonObject();
                bangSapToi.themDong(Api.n(s, "id"), Api.s(s, "tenSuKien") + "\n" + Api.s(s, "diaDiem"),
                        Api.s(s, "thoiGianBatDau"), Api.n(s, "soDangKy") + "/" + Api.n(s, "soLuongToiDa"));
            }
        });
    }
}
