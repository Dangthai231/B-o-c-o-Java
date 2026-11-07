package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MÀN HÌNH "BÁO CÁO": số liệu tổng hợp + 2 biểu đồ cột tự vẽ (không cần thư viện ngoài).
 */
public class BaoCaoPanel extends TrangAdmin {

    private final UI.StatCard theDk = new UI.StatCard("Lượt đăng ký", Icons.USERS, Theme.PRIMARY, Theme.PRIMARY_SOFT);
    private final UI.StatCard theTg = new UI.StatCard("Đã tham gia", Icons.CHECK_CIRCLE, Theme.SUCCESS, Theme.SUCCESS_SOFT);
    private final UI.StatCard theVang = new UI.StatCard("Vắng mặt", Icons.X, Theme.DANGER, Theme.DANGER_SOFT);
    private final UI.StatCard theDiem = new UI.StatCard("Điểm đánh giá TB", Icons.CHART, Theme.WARNING, Theme.WARNING_SOFT);
    private final BieuDoCot bdThang = new BieuDoCot(false);
    private final BieuDoCot bdTop = new BieuDoCot(true);
    private final JLabel lblVaiTro = UI.nhan(" ", Theme.SMALL, Theme.TEXT_2);

    public BaoCaoPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        UI.FlatButton lamMoi = UI.nut("Làm mới", Icons.REFRESH, UI.Kieu.VIEN);
        lamMoi.addActionListener(e -> taiLai());
        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.add(UI.tieuDeTrang("Báo cáo", "Thống kê tình hình tổ chức và tham gia sự kiện"), BorderLayout.CENTER);
        dau.add(lamMoi, BorderLayout.EAST);
        add(dau, BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 18));
        giua.setOpaque(false);
        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.add(theDk);
        hangThe.add(theTg);
        hangThe.add(theVang);
        hangThe.add(theDiem);
        giua.add(hangThe, BorderLayout.NORTH);

        JPanel hai = new JPanel(new GridLayout(1, 2, 18, 0));
        hai.setOpaque(false);
        hai.add(theBieuDo("Số sự kiện theo tháng", "6 tháng gần nhất, tính theo ngày bắt đầu", bdThang, null));
        hai.add(theBieuDo("Sự kiện nhiều lượt đăng ký nhất", "Top 5, so với số chỗ tối đa", bdTop, lblVaiTro));
        giua.add(hai, BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);
    }

    private JPanel theBieuDo(String tieuDe, String moTa, JComponent bd, JComponent chan) {
        UI.RoundPanel p = UI.card(new BorderLayout(0, 12), 18);
        p.add(UI.tieuDeTrang(tieuDe, moTa).getComponent(0).getParent(), BorderLayout.NORTH);
        ((JLabel) ((JPanel) p.getComponent(0)).getComponent(0)).setFont(Theme.H2);
        p.add(bd, BorderLayout.CENTER);
        if (chan != null) p.add(chan, BorderLayout.SOUTH);
        return p;
    }

    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/bao-cao"), kq -> {
            JsonObject o = Api.obj(kq);
            theDk.setSo(Api.n(o, "tongDangKy"));
            theTg.setSo(Api.n(o, "soThamGia"));
            theVang.setSo(Api.n(o, "soVangMat"));
            double diem = Api.d(o, "diemDanhGiaTb");
            theDiem.setChu(diem == 0 ? "–" : diem + " / 5");
            bdThang.datDuLieu(Api.arr(o, "suKienTheoThang"));
            bdTop.datDuLieu(Api.arr(o, "topDangKy"));
            JsonObject vt = Api.obj(o, "taiKhoanTheoVaiTro");
            List<String> s = new ArrayList<>();
            vt.entrySet().forEach(e -> s.add(e.getKey() + ": " + e.getValue().getAsLong()));
            lblVaiTro.setText("Tài khoản theo vai trò  -  " + String.join("   ", s));
        });
    }

    /** Biểu đồ cột. ngang = true: cột nằm ngang (dùng cho tên dài), có vạch số chỗ tối đa. */
    private static class BieuDoCot extends JComponent {
        private final boolean ngang;
        private final List<String> nhan = new ArrayList<>();
        private final List<Long> giaTri = new ArrayList<>();
        private final List<Long> toiDa = new ArrayList<>();

        BieuDoCot(boolean ngang) {
            this.ngang = ngang;
            setPreferredSize(new Dimension(300, 240));
        }

        void datDuLieu(com.google.gson.JsonArray ds) {
            nhan.clear();
            giaTri.clear();
            toiDa.clear();
            for (JsonElement el : ds) {
                JsonObject o = el.getAsJsonObject();
                nhan.add(Api.s(o, "nhan"));
                giaTri.add(Api.n(o, "giaTri"));
                toiDa.add(Api.n(o, "toiDa"));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Theme.muot(g);
            int w = getWidth(), h = getHeight();
            if (nhan.isEmpty()) {
                g2.setColor(Theme.MUTED);
                g2.setFont(Theme.BODY);
                g2.drawString("Chưa có dữ liệu", w / 2 - 45, h / 2);
                g2.dispose();
                return;
            }
            long max = 1;
            for (int i = 0; i < giaTri.size(); i++) max = Math.max(max, Math.max(giaTri.get(i), ngang ? toiDa.get(i) : 0));
            g2.setFont(Theme.SMALL);
            FontMetrics fm = g2.getFontMetrics();
            if (ngang) {
                int hangCao = Math.min(44, h / nhan.size());
                for (int i = 0; i < nhan.size(); i++) {
                    int y = i * hangCao;
                    String ten = nhan.get(i);
                    while (fm.stringWidth(ten) > w - 70 && ten.length() > 3) ten = ten.substring(0, ten.length() - 4) + "...";
                    g2.setColor(Theme.TEXT_2);
                    g2.drawString(ten, 0, y + fm.getAscent());
                    int yb = y + fm.getHeight() + 3;
                    int rongMax = w - 60;
                    g2.setColor(Theme.GRAY_SOFT);
                    g2.fillRoundRect(0, yb, rongMax, 10, 10, 10);
                    g2.setColor(Theme.PRIMARY);
                    int rong = (int) (rongMax * giaTri.get(i) / (double) Math.max(1, toiDa.get(i) > 0 ? toiDa.get(i) : max));
                    g2.fillRoundRect(0, yb, Math.max(rong, 4), 10, 10, 10);
                    g2.setColor(Theme.TEXT);
                    g2.setFont(Theme.SMALL_BOLD);
                    g2.drawString(giaTri.get(i) + "/" + toiDa.get(i), rongMax + 8, yb + 10);
                    g2.setFont(Theme.SMALL);
                }
            } else {
                int day = h - 24;
                int soCot = nhan.size();
                int khoang = w / soCot;
                int rongCot = Math.min(42, khoang - 20);
                // đường kẻ ngang mờ
                g2.setColor(new Color(0xEEF1F5));
                for (int k = 0; k <= 4; k++) {
                    int y = 10 + (day - 10) * k / 4;
                    g2.drawLine(0, y, w, y);
                }
                for (int i = 0; i < soCot; i++) {
                    int cao = (int) ((day - 24) * giaTri.get(i) / (double) max);
                    int x = i * khoang + (khoang - rongCot) / 2;
                    g2.setColor(i == soCot - 1 ? Theme.PRIMARY : new Color(0x8DB4EE));
                    g2.fillRoundRect(x, day - cao, rongCot, Math.max(cao, 3), 8, 8);
                    g2.setColor(Theme.TEXT);
                    g2.setFont(Theme.SMALL_BOLD);
                    String so = String.valueOf(giaTri.get(i));
                    g2.drawString(so, x + (rongCot - g2.getFontMetrics().stringWidth(so)) / 2, day - cao - 6);
                    g2.setFont(Theme.SMALL);
                    g2.setColor(Theme.MUTED);
                    g2.drawString(nhan.get(i), i * khoang + (khoang - fm.stringWidth(nhan.get(i))) / 2, h - 4);
                }
            }
            g2.dispose();
        }
    }
}
