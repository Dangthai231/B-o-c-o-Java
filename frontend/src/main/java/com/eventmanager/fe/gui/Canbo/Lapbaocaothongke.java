package com.eventmanager.fe.gui.Canbo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Lapbaocaothongke {
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JPanel mainPanel;
    private JLabel lblCapNhat;
    private JCheckBox chkLocNgay;
    private JTextField txtTuNgay;
    private JTextField txtDenNgay;

    private JLabel lblTongSuKien;
    private JLabel lblDangDienRa;
    private JLabel lblHoanThanh;
    private JLabel lblDaHuy;
    private JLabel lblTongThamGia;
    private JLabel lblDaThamDu;
    private JLabel lblTyLeThamDu;

    private BarChartPanel chartPanel;
    private int[] monthlyData = {1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 0, 0};

    public Lapbaocaothongke() {
        initUI();
        loadBaoCaoData();
    }

    private void initUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 20, 20, 20));

        // 1. KPI Cards Bar (7 metrics)
        JPanel kpiRow = new JPanel(new GridLayout(1, 7, 10, 0));
        kpiRow.setBackground(Color.WHITE);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        kpiRow.setPreferredSize(new Dimension(850, 75));

        lblTongSuKien = new JLabel("12", SwingConstants.CENTER);
        kpiRow.add(createKpiCard("Tổng sự kiện", lblTongSuKien, new Color(43, 91, 132)));

        lblDangDienRa = new JLabel("3", SwingConstants.CENTER);
        kpiRow.add(createKpiCard("Đang diễn ra", lblDangDienRa, new Color(30, 136, 229)));

        lblHoanThanh = new JLabel("7", SwingConstants.CENTER);
        kpiRow.add(createKpiCard("Hoàn thành", lblHoanThanh, new Color(46, 125, 50)));

        lblDaHuy = new JLabel("2", SwingConstants.CENTER);
        kpiRow.add(createKpiCard("Đã hủy", lblDaHuy, new Color(211, 47, 47)));

        lblTongThamGia = new JLabel("30", SwingConstants.CENTER);
        kpiRow.add(createKpiCard("T.số tham gia", lblTongThamGia, new Color(123, 31, 162)));

        lblDaThamDu = new JLabel("16", SwingConstants.CENTER);
        kpiRow.add(createKpiCard("Đã tham dự", lblDaThamDu, new Color(0, 137, 123)));

        lblTyLeThamDu = new JLabel("53%", SwingConstants.CENTER);
        kpiRow.add(createKpiCard("Tỷ lệ tham dự", lblTyLeThamDu, new Color(230, 81, 0)));

        mainPanel.add(kpiRow);
        mainPanel.add(Box.createVerticalStrut(15));

        // 2. Filter Row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterRow.setBackground(Color.WHITE);
        filterRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        chkLocNgay = new JCheckBox("Lọc ngày:");
        chkLocNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkLocNgay.setBackground(Color.WHITE);
        filterRow.add(chkLocNgay);

        JLabel lblTuNgay = new JLabel("Từ ngày:");
        lblTuNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterRow.add(lblTuNgay);

        txtTuNgay = new JTextField("01/01/2026", 8);
        txtTuNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTuNgay.setBorder(new CompoundBorder(new LineBorder(new Color(180, 180, 180)), new EmptyBorder(3, 6, 3, 6)));
        filterRow.add(txtTuNgay);

        JLabel lblDenNgay = new JLabel("Đến:");
        lblDenNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        filterRow.add(lblDenNgay);

        txtDenNgay = new JTextField("31/12/2026", 8);
        txtDenNgay.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDenNgay.setBorder(new CompoundBorder(new LineBorder(new Color(180, 180, 180)), new EmptyBorder(3, 6, 3, 6)));
        filterRow.add(txtDenNgay);

        JButton btnXemBaoCao = new JButton("Xem báo cáo");
        btnXemBaoCao.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnXemBaoCao.setBackground(new Color(60, 120, 168));
        btnXemBaoCao.setForeground(Color.WHITE);
        // Giao diện Windows bỏ qua setBackground của JButton -> tự tô nền, nếu không chữ trắng trên nền trắng
        btnXemBaoCao.setContentAreaFilled(false);
        btnXemBaoCao.setOpaque(true);
        btnXemBaoCao.setFocusPainted(false);
        btnXemBaoCao.setBorder(new EmptyBorder(6, 16, 6, 16));
        btnXemBaoCao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        filterRow.add(btnXemBaoCao);

        lblCapNhat = new JLabel("Cập nhật lúc " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        lblCapNhat.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCapNhat.setForeground(new Color(100, 100, 100));
        filterRow.add(lblCapNhat);

        btnXemBaoCao.addActionListener(e -> loadBaoCaoData());
        // Tick / bỏ tick "Lọc ngày" thì xem lại báo cáo ngay
        chkLocNgay.addActionListener(e -> loadBaoCaoData());

        mainPanel.add(filterRow);
        mainPanel.add(Box.createVerticalStrut(15));

        // 3. Dark Banner Bar: "Báo cáo Thống kê"
        JPanel darkBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        darkBanner.setBackground(new Color(30, 34, 56));
        darkBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        darkBanner.setPreferredSize(new Dimension(850, 40));

        JLabel lblBanner = new JLabel("Báo cáo  Thống kê");
        lblBanner.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblBanner.setForeground(Color.WHITE);
        darkBanner.add(lblBanner);

        mainPanel.add(darkBanner);
        mainPanel.add(Box.createVerticalStrut(15));

        // 4. Chart Title
        JPanel chartTitlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        chartTitlePanel.setBackground(Color.WHITE);
        chartTitlePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        JLabel lblChartTitle = new JLabel("Số sự kiện theo tháng");
        lblChartTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblChartTitle.setForeground(new Color(30, 30, 30));
        chartTitlePanel.add(lblChartTitle);

        mainPanel.add(chartTitlePanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // 5. Chart Panel (Custom paint matching WinForms chart)
        chartPanel = new BarChartPanel(monthlyData);
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(800, 320));
        mainPanel.add(chartPanel);
    }

    private JPanel createKpiCard(String label, JLabel lblVal, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(Color.WHITE);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 235, 240), 1),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JLabel lblName = new JLabel(label, SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblName.setForeground(new Color(100, 100, 100));
        card.add(lblName, BorderLayout.NORTH);

        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblVal.setForeground(valueColor);
        card.add(lblVal, BorderLayout.CENTER);

        return card;
    }

    /** Tick "Lọc ngày" thì gửi ?tuNgay=yyyy-MM-dd&denNgay=yyyy-MM-dd, không tick thì xem cả năm nay. */
    private void loadBaoCaoData() {
        String url = "http://localhost:8080/api/thongke/bao-cao";
        if (chkLocNgay != null && chkLocNgay.isSelected()) {
            try {
                LocalDate tu = LocalDate.parse(txtTuNgay.getText().trim(), NGAY);
                LocalDate den = LocalDate.parse(txtDenNgay.getText().trim(), NGAY);
                if (tu.isAfter(den)) {
                    JOptionPane.showMessageDialog(mainPanel, "\"Từ ngày\" phải trước \"Đến ngày\"", "Lọc ngày", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                url += "?tuNgay=" + tu + "&denNgay=" + den;
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(mainPanel, "Ngày phải nhập dạng dd/MM/yyyy, ví dụ 01/10/2026", "Lọc ngày", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (lblCapNhat != null) {
            lblCapNhat.setText("Cập nhật lúc " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
                    + (url.contains("?") ? "  (từ " + txtTuNgay.getText().trim() + " đến " + txtDenNgay.getText().trim() + ")" : "  (năm nay)"));
        }
        final String duongDan = url;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(duongDan))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        SwingUtilities.invokeLater(() -> {
                            if (json.has("tongSuKien") && lblTongSuKien != null)
                                lblTongSuKien.setText(String.valueOf(json.get("tongSuKien").getAsInt()));
                            if (json.has("dangDienRa") && lblDangDienRa != null)
                                lblDangDienRa.setText(String.valueOf(json.get("dangDienRa").getAsInt()));
                            if (json.has("hoanThanh") && lblHoanThanh != null)
                                lblHoanThanh.setText(String.valueOf(json.get("hoanThanh").getAsInt()));
                            if (json.has("daHuy") && lblDaHuy != null)
                                lblDaHuy.setText(String.valueOf(json.get("daHuy").getAsInt()));
                            if (json.has("tongThamGia") && lblTongThamGia != null)
                                lblTongThamGia.setText(String.valueOf(json.get("tongThamGia").getAsInt()));
                            if (json.has("daThamDu") && lblDaThamDu != null)
                                lblDaThamDu.setText(String.valueOf(json.get("daThamDu").getAsInt()));
                            if (json.has("tyLeThamDu") && lblTyLeThamDu != null)
                                lblTyLeThamDu.setText(json.get("tyLeThamDu").getAsString());

                            if (json.has("monthlyEvents") && chartPanel != null) {
                                JsonArray arr = json.getAsJsonArray("monthlyEvents");
                                int[] m = new int[arr.size()];
                                for (int i = 0; i < arr.size(); i++) {
                                    m[i] = arr.get(i).getAsInt();
                                }
                                chartPanel.setData(m);
                            }
                        });
                    }
                } catch (Exception ignored) {}
                return null;
            }
        };
        worker.execute();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void $$$setupUI$$$() {
    }

    private static class BarChartPanel extends JPanel {
        private int[] data;

        public BarChartPanel(int[] data) {
            this.data = data;
        }

        public void setData(int[] newData) {
            this.data = newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            int leftMargin = 70;
            int rightMargin = 40;
            int topMargin = 30;
            int bottomMargin = 45;

            int plotWidth = width - leftMargin - rightMargin;
            int plotHeight = height - topMargin - bottomMargin;

            if (plotWidth <= 0 || plotHeight <= 0) {
                g2.dispose();
                return;
            }

            // Draw Y-axis reference lines and labels: 7 bước, bước 0,5 khi số nhỏ, tự giãn khi số liệu thật lớn hơn
            int steps = 7;
            int maxData = 0;
            if (data != null) for (int v : data) maxData = Math.max(maxData, v);
            double buoc = maxData <= 3 ? 0.5 : Math.ceil(maxData / (double) steps);
            double maxY = buoc * steps;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            for (int i = 0; i <= steps; i++) {
                double val = i * buoc;
                int y = topMargin + plotHeight - (int) ((val / maxY) * plotHeight);

                // Grid line
                g2.setColor(new Color(225, 225, 225));
                g2.drawLine(leftMargin, y, leftMargin + plotWidth, y);

                // Y label
                g2.setColor(new Color(50, 50, 50));
                String labelStr = (val == (long) val) ? String.format("%d", (long) val) : String.format("%.1f", val).replace(".", ",");
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(labelStr, leftMargin - fm.stringWidth(labelStr) - 10, y + 4);

                // Small tick
                g2.setColor(new Color(100, 100, 100));
                g2.drawLine(leftMargin - 4, y, leftMargin, y);
            }

            // Draw Y-axis line
            g2.setColor(new Color(160, 160, 160));
            g2.drawLine(leftMargin, topMargin, leftMargin, topMargin + plotHeight);

            // Draw Bars
            int numBars = data != null ? data.length : 12;
            int slotWidth = plotWidth / numBars;
            int barWidth = Math.min(36, (int) (slotWidth * 0.65));

            Color barColor = new Color(60, 120, 168);

            for (int i = 0; i < numBars; i++) {
                int val = (data != null && i < data.length) ? data[i] : 0;
                int x = leftMargin + i * slotWidth + (slotWidth - barWidth) / 2;
                int barH = (int) ((val / maxY) * plotHeight);
                int y = topMargin + plotHeight - barH;

                // Bar
                if (val > 0) {
                    g2.setColor(barColor);
                    g2.fillRect(x, y, barWidth, barH);
                }

                // Bar value text
                g2.setColor(new Color(40, 40, 40));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String valStr = String.valueOf(val);
                FontMetrics fmVal = g2.getFontMetrics();
                g2.drawString(valStr, x + (barWidth - fmVal.stringWidth(valStr)) / 2, y - 6);

                // Month tick & label
                int tickX = leftMargin + i * slotWidth + slotWidth / 2;
                g2.setColor(new Color(120, 120, 120));
                g2.drawLine(tickX, topMargin + plotHeight, tickX, topMargin + plotHeight + 4);

                g2.setColor(new Color(40, 40, 40));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                String monthStr = "T" + (i + 1);
                FontMetrics fmMonth = g2.getFontMetrics();
                g2.drawString(monthStr, tickX - fmMonth.stringWidth(monthStr) / 2, topMargin + plotHeight + 20);
            }

            g2.dispose();
        }
    }
}
