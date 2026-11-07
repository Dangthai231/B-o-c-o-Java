package Canbo;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * Giao diện Trang chủ Cán bộ (chuẩn thiết kế hiện đại, đồng bộ phong cách với DashboardAdmin).
 */
public class Trangchu {

    private JPanel mainPanel;
    private final UI.StatCard theTk = new UI.StatCard("Tổng tài khoản", Icons.USERS, Theme.PRIMARY, Theme.PRIMARY_SOFT);
    private final UI.StatCard theSk = new UI.StatCard("Tổng sự kiện", Icons.CALENDAR, Theme.PURPLE, Theme.PURPLE_SOFT);
    private final UI.StatCard theSap = new UI.StatCard("Sắp diễn ra", Icons.CLOCK, Theme.SUCCESS, Theme.SUCCESS_SOFT);
    private final UI.StatCard theTb = new UI.StatCard("Thông báo đã đăng", Icons.BELL, Theme.WARNING, Theme.WARNING_SOFT);

    private final BangDep bangSapToi = new BangDep("id", "Sự kiện", "Bắt đầu", "Đăng ký");
    private ViecCanLam viecKeHoach, viecThongBao, viecYeuCau;
    private JLabel lblTongViec;
    private Consumer<String> onNavigate;

    public Trangchu() {
        this(null);
    }

    public Trangchu(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
        initUI();
        napDuLieuMacDinh();
        loadDuLieuTuBackend();
    }

    public void setOnNavigate(Consumer<String> onNavigate) {
        this.onNavigate = onNavigate;
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 18));
        mainPanel.setBackground(Theme.BG);
        mainPanel.setBorder(new EmptyBorder(20, 24, 24, 24));

        // 1. Tiêu đề trang
        String ngay = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        mainPanel.add(UI.tieuDeTrang("Tổng quan", "Hôm nay " + ngay + ". Tình hình hoạt động của Liên chi đoàn"), BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(0, 18));
        giua.setOpaque(false);

        // 2. Hàng 4 thẻ thống kê (StatCards)
        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.setPreferredSize(new Dimension(0, 102));
        hangThe.add(theTk);
        hangThe.add(theSk);
        hangThe.add(theSap);
        hangThe.add(theTb);
        giua.add(hangThe, BorderLayout.NORTH);

        // 3. Vùng dưới: 2 cột (Trái: Sự kiện sắp diễn ra | Phải: Việc cần xử lý)
        JPanel duoi = new JPanel(new BorderLayout(18, 0));
        duoi.setOpaque(false);

        // --- Cột Trái: Sự kiện sắp diễn ra ---
        UI.RoundPanel trai = UI.card(new BorderLayout(0, 12), 18);
        JPanel dauTrai = new JPanel(new BorderLayout());
        dauTrai.setOpaque(false);
        dauTrai.add(UI.nhan("Sự kiện sắp diễn ra", Theme.H2, Theme.TEXT), BorderLayout.WEST);

        UI.FlatButton xemHet = UI.nut("Xem tất cả", Icons.CHEVRON_RIGHT, UI.Kieu.NHAT);
        xemHet.setHorizontalTextPosition(SwingConstants.LEFT);
        xemHet.addActionListener(e -> {
            if (onNavigate != null) onNavigate.accept("sukien");
        });
        dauTrai.add(xemHet, BorderLayout.EAST);
        trai.add(dauTrai, BorderLayout.NORTH);

        bangSapToi.anCot(0);
        bangSapToi.cotHaiDong(1);
        bangSapToi.doRongCot(1, 280);
        bangSapToi.doRongCot(2, 130);
        bangSapToi.doRongCot(3, 140);
        bangSapToi.getColumnModel().getColumn(3).setCellRenderer((t, v, s, f, r, c) -> {
            String[] p = (v == null ? "0/1" : v.toString()).split("/");
            double daDk = 0, toiDa = 1;
            try {
                daDk = Double.parseDouble(p[0].trim());
                toiDa = Math.max(1, Double.parseDouble(p[1].trim()));
            } catch (Exception ignored) {}
            JPanel o = new JPanel(new BorderLayout(0, 4));
            o.setBorder(new EmptyBorder(8, 14, 8, 14));
            o.setBackground(bangSapToi.nenDong(r, s));
            o.add(UI.nhan((int) daDk + " / " + (int) toiDa, Theme.SMALL_BOLD, Theme.TEXT_2), BorderLayout.NORTH);
            o.add(UI.thanhTienDo(daDk / toiDa, Theme.PRIMARY), BorderLayout.CENTER);
            return o;
        });
        trai.add(bangSapToi.trongKhungCuon(), BorderLayout.CENTER);
        duoi.add(trai, BorderLayout.CENTER);

        // --- Cột Phải: Việc cần xử lý ---
        UI.RoundPanel phai = UI.card(new BorderLayout(0, 12), 18);
        phai.setPreferredSize(new Dimension(330, 0));

        JPanel dauPhai = new JPanel(new GridLayout(2, 1, 0, 2));
        dauPhai.setOpaque(false);
        dauPhai.add(UI.nhan("Việc cần xử lý", Theme.H2, Theme.TEXT));
        lblTongViec = UI.nhan("8 việc đang chờ bạn xử lý", Theme.SMALL, Theme.MUTED);
        dauPhai.add(lblTongViec);
        phai.add(dauPhai, BorderLayout.NORTH);

        JPanel ds = new JPanel(new GridLayout(3, 1, 0, 10));
        ds.setOpaque(false);

        viecKeHoach = new ViecCanLam("Kế hoạch sự kiện chờ duyệt", Icons.FILE, Theme.WARNING, Theme.WARNING_SOFT,
                () -> { if (onNavigate != null) onNavigate.accept("sukien"); });
        viecThongBao = new ViecCanLam("Thông báo chờ duyệt", Icons.BELL, Theme.PRIMARY, Theme.PRIMARY_SOFT,
                () -> { if (onNavigate != null) onNavigate.accept("thongbao"); });
        viecYeuCau = new ViecCanLam("Yêu cầu đổi thông tin", Icons.USER, Theme.PURPLE, Theme.PURPLE_SOFT,
                () -> { if (onNavigate != null) onNavigate.accept("nguoithamgia"); });

        ds.add(viecKeHoach);
        ds.add(viecThongBao);
        ds.add(viecYeuCau);

        JPanel boc = new JPanel(new BorderLayout());
        boc.setOpaque(false);
        boc.add(ds, BorderLayout.NORTH);
        phai.add(boc, BorderLayout.CENTER);

        duoi.add(phai, BorderLayout.EAST);
        giua.add(duoi, BorderLayout.CENTER);
        mainPanel.add(giua, BorderLayout.CENTER);
    }

    private void napDuLieuMacDinh() {
        theTk.setSo(31);
        theSk.setSo(17);
        theSap.setSo(8);
        theTb.setSo(2);

        viecKeHoach.setSo(2);
        viecThongBao.setSo(3);
        viecYeuCau.setSo(3);
        lblTongViec.setText("8 việc đang chờ bạn xử lý");

        bangSapToi.xoaHet();
        bangSapToi.themDong(1L, "Đêm nhạc chào tân sinh viên\nSân trường EPU", "09/11/2026 08:00", "0 / 2");
        bangSapToi.themDong(2L, "Giải bóng đá Liên chi đoàn CNTT\nSân bóng EPU", "11/11/2026 08:00", "4 / 120");
        bangSapToi.themDong(3L, "Seminar An toàn thông tin cho sinh viên\nPhòng hội thảo nhà E", "13/11/2026 08:00", "4 / 100");
        bangSapToi.themDong(4L, "Workshop Lập trình Java Spring Boot\nPhòng máy B305", "16/11/2026 08:00", "5 / 40");
        bangSapToi.themDong(5L, "Cuộc thi Hackathon EPU\nPhòng máy B301-B305", "20/11/2026 08:00", "3 / 50");
    }

    public void taiLai() {
        loadDuLieuTuBackend();
    }

    private void loadDuLieuTuBackend() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    HttpClient client = HttpClient.newBuilder()
                            .connectTimeout(Duration.ofSeconds(3))
                            .build();

                    HttpRequest req = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/admin/tong-quan"))
                            .GET()
                            .timeout(Duration.ofSeconds(4))
                            .build();

                    HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                    if (res.statusCode() == 200) {
                        JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
                        SwingUtilities.invokeLater(() -> {
                            if (o.has("tongTaiKhoan")) theTk.setSo(o.get("tongTaiKhoan").getAsLong());
                            if (o.has("tongSuKien")) theSk.setSo(o.get("tongSuKien").getAsLong());
                            if (o.has("suKienSapDienRa")) theSap.setSo(o.get("suKienSapDienRa").getAsLong());
                            if (o.has("thongBaoDaDang")) theTb.setSo(o.get("thongBaoDaDang").getAsLong());

                            if (o.has("keHoachChoDuyet")) viecKeHoach.setSo(o.get("keHoachChoDuyet").getAsLong());
                            if (o.has("thongBaoChoDuyet")) viecThongBao.setSo(o.get("thongBaoChoDuyet").getAsLong());
                            if (o.has("yeuCauChoDuyet")) viecYeuCau.setSo(o.get("yeuCauChoDuyet").getAsLong());

                            long tong = o.has("tongViecCanXuLy") ? o.get("tongViecCanXuLy").getAsLong() : 0;
                            lblTongViec.setText(tong == 0 ? "Không có việc nào đang chờ" : tong + " việc đang chờ bạn xử lý");

                            if (o.has("suKienSapToi") && o.get("suKienSapToi").isJsonArray()) {
                                JsonArray arr = o.getAsJsonArray("suKienSapToi");
                                if (arr.size() > 0) {
                                    bangSapToi.xoaHet();
                                    for (JsonElement el : arr) {
                                        JsonObject s = el.getAsJsonObject();
                                        long id = s.has("id") ? s.get("id").getAsLong() : 0;
                                        String ten = s.has("tenSuKien") ? s.get("tenSuKien").getAsString() : "";
                                        String dd = s.has("diaDiem") ? s.get("diaDiem").getAsString() : "";
                                        String tg = s.has("thoiGianBatDau") ? s.get("thoiGianBatDau").getAsString() : "";
                                        long dk = s.has("soDangKy") ? s.get("soDangKy").getAsLong() : 0;
                                        long td = s.has("soLuongToiDa") ? s.get("soLuongToiDa").getAsLong() : 1;
                                        bangSapToi.themDong(id, ten + "\n" + dd, tg, dk + " / " + td);
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception ex) {
                    // Backend offline -> giữ nguyên dữ liệu mặc định đẹp mắt
                }
                return null;
            }
        };
        worker.execute();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /** 1 dòng "việc cần làm": icon vector + tên + số lượng */
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
                    if (khiBam != null) khiBam.run();
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
}
