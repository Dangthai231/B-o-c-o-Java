package Canbo;

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
import java.time.format.DateTimeFormatter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Trangchu {
    private JPanel mainPanel;
    private JLabel lblLoiChaoFooter;
    private JLabel lblSoSuKien;
    private JLabel lblSoCongViec;
    private JLabel lblSoNguoiThamGia;
    private JLabel lblSoTaiLieu;
    private JLabel lblSoThongBao;
    private JLabel lblSoBaoCao;

    // Colors matching DashboardCanbo theme
    private static final Color CONTENT_BG = new Color(240, 242, 245);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(230, 233, 238);
    private static final Color TEXT_DARK = new Color(30, 35, 45);
    private static final Color TEXT_MUTED = new Color(120, 125, 135);
    private static final Color ACCENT_BLUE = new Color(33, 150, 243);

    // Icon circle colors
    private static final Color ICON_BLUE = new Color(33, 150, 243);
    private static final Color ICON_GREEN = new Color(76, 175, 80);
    private static final Color ICON_ORANGE = new Color(255, 152, 0);
    private static final Color ICON_PURPLE = new Color(156, 39, 176);
    private static final Color ICON_RED = new Color(244, 67, 54);
    private static final Color ICON_TEAL = new Color(0, 150, 136);

    public Trangchu() {
        initUI();
        loadDuLieu();
    }

    private void initUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(0, 0));
        mainPanel.setBackground(CONTENT_BG);
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Scrollable wrapper
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setBackground(CONTENT_BG);
        scrollContent.setBorder(new EmptyBorder(28, 32, 28, 32));

        // ====== HEADER SECTION ======
        JPanel headerSection = new JPanel(new BorderLayout());
        headerSection.setOpaque(false);
        headerSection.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lblTitle = new JLabel("Tổng quan");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        headerSection.add(lblTitle, BorderLayout.NORTH);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        JLabel lblSubtitle = new JLabel("Hôm nay " + today + ". Tình hình hoạt động của Liên chi đoàn");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitle.setForeground(TEXT_MUTED);
        headerSection.add(lblSubtitle, BorderLayout.SOUTH);

        scrollContent.add(headerSection);
        scrollContent.add(Box.createVerticalStrut(24));

        // ====== KPI CARDS ROW (4 cards) ======
        JPanel kpiRow = new JPanel(new GridLayout(1, 4, 18, 0));
        kpiRow.setOpaque(false);
        kpiRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        lblSoNguoiThamGia = new JLabel("30");
        kpiRow.add(createKpiCard("👥", "Tổng tài khoản", lblSoNguoiThamGia, ICON_BLUE));

        lblSoSuKien = new JLabel("12");
        kpiRow.add(createKpiCard("📅", "Tổng sự kiện", lblSoSuKien, ICON_GREEN));

        lblSoCongViec = new JLabel("3");
        kpiRow.add(createKpiCard("⏱", "Sắp diễn ra", lblSoCongViec, ICON_ORANGE));

        lblSoThongBao = new JLabel("33");
        kpiRow.add(createKpiCard("🔔", "Thông báo đã đăng", lblSoThongBao, ICON_RED));

        scrollContent.add(kpiRow);
        scrollContent.add(Box.createVerticalStrut(24));

        // ====== BOTTOM SECTION: 2 columns ======
        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 18, 0));
        bottomRow.setOpaque(false);
        bottomRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        // Left: "Sự kiện sắp diễn ra" card
        bottomRow.add(createEventListCard());

        // Right: "Việc cần xử lý" card
        bottomRow.add(createTodoCard());

        scrollContent.add(bottomRow);
        scrollContent.add(Box.createVerticalStrut(24));

        // ====== 2nd KPI ROW (smaller, remaining 2 stats) ======
        JPanel kpiRow2 = new JPanel(new GridLayout(1, 2, 18, 0));
        kpiRow2.setOpaque(false);
        kpiRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        lblSoTaiLieu = new JLabel("2");
        kpiRow2.add(createKpiCard("📁", "Tài liệu sự kiện", lblSoTaiLieu, ICON_PURPLE));

        lblSoBaoCao = new JLabel("2");
        kpiRow2.add(createKpiCard("📊", "Báo cáo hoàn thành", lblSoBaoCao, ICON_TEAL));

        scrollContent.add(kpiRow2);
        scrollContent.add(Box.createVerticalStrut(16));

        // Footer
        JPanel footerCard = createRoundedCard();
        footerCard.setBorder(new EmptyBorder(14, 20, 14, 20));
        footerCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        lblLoiChaoFooter = new JLabel("Ngày " + today + " - Chúc bạn một ngày làm việc hiệu quả và quản lý sự kiện thành công!");
        lblLoiChaoFooter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblLoiChaoFooter.setForeground(TEXT_MUTED);
        footerCard.add(lblLoiChaoFooter, BorderLayout.WEST);

        scrollContent.add(footerCard);

        JScrollPane scrollPane = new JScrollPane(scrollContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(CONTENT_BG);
        scrollPane.getViewport().setBackground(CONTENT_BG);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    // ===== KPI Card matching screenshot style =====
    private JPanel createKpiCard(String icon, String title, JLabel numLabel, Color iconColor) {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(12, 0));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Left: colored circle icon
        JPanel iconCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Light tinted circle
                g2.setColor(new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 30));
                g2.fillOval(0, 0, 44, 44);
                // Icon text
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (44 - fm.stringWidth(icon)) / 2;
                int y = (44 + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(iconColor);
                g2.drawString(icon, x, y);
                g2.dispose();
            }
        };
        iconCircle.setOpaque(false);
        iconCircle.setPreferredSize(new Dimension(44, 44));
        card.add(iconCircle, BorderLayout.WEST);

        // Right: text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTitle.setForeground(TEXT_MUTED);
        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));

        numLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        numLabel.setForeground(TEXT_DARK);
        textPanel.add(numLabel);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    // ===== "Sự kiện sắp diễn ra" table card =====
    private JPanel createEventListCard() {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Card header
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel lblTitle = new JLabel("Sự kiện sắp diễn ra");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_DARK);
        headerRow.add(lblTitle, BorderLayout.WEST);

        JLabel lblViewAll = new JLabel("Xem tất cả >");
        lblViewAll.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblViewAll.setForeground(ACCENT_BLUE);
        lblViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        headerRow.add(lblViewAll, BorderLayout.EAST);

        card.add(headerRow, BorderLayout.NORTH);

        // Table
        String[] columns = {"Sự kiện", "Bắt đầu", "Đăng ký"};
        Object[][] data = {
                {"Đêm nhạc chào tân sinh viên", "09/11/2026 08:00", "0 / 2"},
                {"Giải bóng đá Liên chi đoàn CNTT", "11/11/2026 08:00", "4 / 120"},
                {"Seminar An toàn thông tin", "13/11/2026 08:00", "4 / 100"},
                {"Workshop Lập trình Java Spring Boot", "16/11/2026 08:00", "5 / 40"},
                {"Cuộc thi Hackathon EPU", "20/11/2026 08:00", "3 / 50"},
        };

        JTable table = new JTable(data, columns);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(48);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setBackground(CARD_BG);
        table.setSelectionBackground(new Color(232, 240, 254));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(CARD_BG);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                new EmptyBorder(8, 0, 8, 0)
        ));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CARD_BG);

        card.add(scrollPane, BorderLayout.CENTER);

        return card;
    }

    // ===== "Việc cần xử lý" card =====
    private JPanel createTodoCard() {
        JPanel card = createRoundedCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel lblTitle = new JLabel("Việc cần xử lý");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_DARK);

        JLabel lblSubtitle = new JLabel("8 việc đang chờ bạn xử lý");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(TEXT_MUTED);

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.add(lblTitle);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(lblSubtitle);

        card.add(headerPanel, BorderLayout.NORTH);

        // Todo items
        JPanel todoList = new JPanel();
        todoList.setLayout(new BoxLayout(todoList, BoxLayout.Y_AXIS));
        todoList.setOpaque(false);

        todoList.add(Box.createVerticalStrut(8));
        todoList.add(createTodoItem("📋", "Kế hoạch sự kiện chờ duyệt", "Bấm để xử lý", "2", new Color(255, 152, 0)));
        todoList.add(Box.createVerticalStrut(10));
        todoList.add(createTodoItem("🔔", "Thông báo chờ duyệt", "Bấm để xử lý", "3", new Color(244, 67, 54)));
        todoList.add(Box.createVerticalStrut(10));
        todoList.add(createTodoItem("👤", "Yêu cầu đổi thông tin", "Bấm để xử lý", "3", new Color(33, 150, 243)));

        card.add(todoList, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTodoItem(String icon, String title, String subtitle, String count, Color accentColor) {
        JPanel item = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 249, 252));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBorder(new EmptyBorder(14, 16, 14, 16));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 68));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Icon circle
        JPanel iconPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fillRoundRect(0, 0, 40, 40, 10, 10);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(accentColor);
                g2.drawString(icon, (40 - fm.stringWidth(icon)) / 2, (40 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(40, 40));
        item.add(iconPanel, BorderLayout.WEST);

        // Text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_DARK);
        textPanel.add(lblTitle);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(TEXT_MUTED);
        textPanel.add(lblSub);

        item.add(textPanel, BorderLayout.CENTER);

        // Count badge
        JLabel lblCount = new JLabel(count);
        lblCount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblCount.setForeground(accentColor);
        item.add(lblCount, BorderLayout.EAST);

        return item;
    }

    // ===== Rounded white card panel =====
    private JPanel createRoundedCard() {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 14, 14);
                // Card
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                // Border
                g2.setColor(CARD_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 3, getHeight() - 3, 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        return card;
    }

    private void loadDuLieu() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/thongke/tong-quan"))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() == 200) {
                        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                        SwingUtilities.invokeLater(() -> {
                            if (json.has("soSuKien") && lblSoSuKien != null)
                                lblSoSuKien.setText(String.valueOf(json.get("soSuKien").getAsInt()));
                            if (json.has("soCongViec") && lblSoCongViec != null)
                                lblSoCongViec.setText(String.valueOf(json.get("soCongViec").getAsInt()));
                            if (json.has("soNguoiThamGia") && lblSoNguoiThamGia != null)
                                lblSoNguoiThamGia.setText(String.valueOf(json.get("soNguoiThamGia").getAsInt()));
                            if (json.has("soTaiLieu") && lblSoTaiLieu != null)
                                lblSoTaiLieu.setText(String.valueOf(json.get("soTaiLieu").getAsInt()));
                            if (json.has("soThongBao") && lblSoThongBao != null)
                                lblSoThongBao.setText(String.valueOf(json.get("soThongBao").getAsInt()));
                            if (json.has("soBaoCao") && lblSoBaoCao != null)
                                lblSoBaoCao.setText(String.valueOf(json.get("soBaoCao").getAsInt()));
                        });
                    }
                } catch (Exception ex) {
                    System.out.println("[Trangchu] Backend offline hoặc chưa có dữ liệu: " + ex.getMessage());
                }
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
}
