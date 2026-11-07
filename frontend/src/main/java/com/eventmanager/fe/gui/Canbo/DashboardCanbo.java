package com.eventmanager.fe.gui.Canbo;

import Canbo.Trangchu;
import com.eventmanager.fe.gui.Login.Login;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class DashboardCanbo {
    private JPanel mainPanel;
    private JPanel sidebar;
    private JPanel contentPanel;
    private JLabel lblUser;

    private List<JPanel> navItems;
    private JPanel activeNavItem;
    private String currentUsername = "canbo01";

    // Color Palette — Dark Navy Theme (matching screenshot)
    private static final Color SIDEBAR_BG = new Color(30, 42, 74);       // #1E2A4A
    private static final Color SIDEBAR_DARKER = new Color(22, 32, 58);   // #16203A
    private static final Color NAV_ACTIVE_BG = new Color(33, 150, 243);  // #2196F3
    private static final Color NAV_HOVER_BG = new Color(40, 55, 95);     // subtle hover
    private static final Color HEADER_BG = new Color(30, 42, 74);        // same as sidebar
    private static final Color CONTENT_BG = new Color(240, 242, 245);    // #F0F2F5
    private static final Color TEXT_WHITE = new Color(255, 255, 255);
    private static final Color TEXT_MUTED = new Color(180, 195, 220);
    private static final Color ACCENT_BLUE = new Color(33, 150, 243);

    // Các trang con
    private Trangchu trangchu;
    private Lapbaocaothongke lapbaocaothongke;
    private Quanlytailieusukien quanlytailieusukien;
    private Quanlynguoithamgia quanlynguoithamgia;
    private Quanlythongbao quanlythongbao;

    // Navigation labels
    private static final String[] NAV_LABELS = {
            "Trang chủ",
            "Lập báo cáo thống kê",
            "Quản lý tài liệu sự kiện",
            "Quản lý người tham gia",
            "Quản lý thông báo"
    };
    private static final String[] NAV_ICONS = {"🏠", "📊", "📁", "👥", "🔔"};

    public DashboardCanbo() {
        this("canbo01");
    }

    public DashboardCanbo(String username) {
        if (username != null && !username.trim().isEmpty()) {
            this.currentUsername = username.trim();
        }
        initUI();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setPreferredSize(new Dimension(1200, 750));

        // ================= TOP HEADER BAR =================
        JPanel headerBar = createHeaderBar();
        mainPanel.add(headerBar, BorderLayout.NORTH);

        // ================= LEFT SIDEBAR =================
        sidebar = createSidebar();
        mainPanel.add(sidebar, BorderLayout.WEST);

        // ================= CONTENT PANEL =================
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CONTENT_BG);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        // Khởi tạo các trang con
        trangchu = new Trangchu();
        lapbaocaothongke = new Lapbaocaothongke();
        quanlytailieusukien = new Quanlytailieusukien();
        quanlynguoithamgia = new Quanlynguoithamgia();
        quanlythongbao = new Quanlythongbao();

        // Mặc định hiển thị Trang chủ
        showPanel(trangchu.getMainPanel(), navItems.get(0));
    }

    // ============ TOP HEADER BAR ============
    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setPreferredSize(new Dimension(0, 52));
        header.setBorder(new EmptyBorder(0, 16, 0, 20));

        // Left: Hamburger menu icon
        JLabel lblMenu = new JLabel("  ☰  ");
        lblMenu.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        lblMenu.setForeground(TEXT_WHITE);
        lblMenu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        header.add(lblMenu, BorderLayout.WEST);

        // Center: Search Bar
        JPanel searchWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        searchWrapper.setOpaque(false);

        JPanel searchBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        searchBox.setOpaque(false);
        searchBox.setPreferredSize(new Dimension(260, 32));
        searchBox.setBorder(new EmptyBorder(0, 12, 0, 12));

        JLabel lblSearch = new JLabel("🔍 ");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSearch.setForeground(new Color(150, 150, 150));
        searchBox.add(lblSearch, BorderLayout.WEST);

        JTextField txtSearch = new JTextField("Tìm tài khoản...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setForeground(new Color(150, 150, 150));
        txtSearch.setBorder(null);
        txtSearch.setOpaque(false);
        txtSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().equals("Tìm tài khoản...")) {
                    txtSearch.setText("");
                    txtSearch.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txtSearch.getText().isEmpty()) {
                    txtSearch.setText("Tìm tài khoản...");
                    txtSearch.setForeground(new Color(150, 150, 150));
                }
            }
        });
        searchBox.add(txtSearch, BorderLayout.CENTER);
        searchWrapper.add(searchBox);
        header.add(searchWrapper, BorderLayout.CENTER);

        // Right: Notification + User avatar
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        rightPanel.setOpaque(false);

        // Notification bell
        JLabel lblBell = new JLabel("🔔");
        lblBell.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        lblBell.setForeground(TEXT_WHITE);
        lblBell.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(lblBell);

        // User avatar circle
        String initials = currentUsername.length() >= 2
                ? currentUsername.substring(0, 2).toUpperCase()
                : currentUsername.toUpperCase();
        JPanel avatarCircle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_BLUE);
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                FontMetrics fm = g2.getFontMetrics();
                int x = (32 - fm.stringWidth(initials)) / 2;
                int y = (32 + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(initials, x, y);
                g2.dispose();
            }
        };
        avatarCircle.setOpaque(false);
        avatarCircle.setPreferredSize(new Dimension(32, 32));
        rightPanel.add(avatarCircle);

        JLabel lblUsername = new JLabel(currentUsername + " ▾");
        lblUsername.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsername.setForeground(TEXT_WHITE);
        lblUsername.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(lblUsername);

        header.add(rightPanel, BorderLayout.EAST);

        return header;
    }

    // ============ LEFT SIDEBAR ============
    private JPanel createSidebar() {
        JPanel sb = new JPanel();
        sb.setLayout(new BoxLayout(sb, BoxLayout.Y_AXIS));
        sb.setBackground(SIDEBAR_BG);
        sb.setPreferredSize(new Dimension(220, 0));
        sb.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo & Title section
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));
        logoPanel.setBackground(SIDEBAR_DARKER);
        logoPanel.setBorder(new EmptyBorder(18, 16, 18, 16));
        logoPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel lblSystemName = new JLabel("HỆ THỐNG QUẢN LÝ SỰ KIỆN");
        lblSystemName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSystemName.setForeground(TEXT_WHITE);
        lblSystemName.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoPanel.add(lblSystemName);
        logoPanel.add(Box.createVerticalStrut(3));

        JLabel lblSubtitle = new JLabel("BCH LCĐ CNS - ĐH Điện lực");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSubtitle.setForeground(TEXT_MUTED);
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoPanel.add(lblSubtitle);

        sb.add(logoPanel);
        sb.add(Box.createVerticalStrut(12));

        // Navigation Items
        navItems = new ArrayList<>();
        for (int i = 0; i < NAV_LABELS.length; i++) {
            JPanel navItem = createNavItem(NAV_ICONS[i], NAV_LABELS[i], i);
            sb.add(navItem);
            sb.add(Box.createVerticalStrut(2));
        }

        // Spacer to push footer to bottom
        sb.add(Box.createVerticalGlue());

        // Logout button (styled differently)
        JPanel logoutItem = createNavItem("🚪", "Đăng xuất", -1);
        logoutItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        mainPanel,
                        "Bạn có chắc muốn đăng xuất khỏi hệ thống?",
                        "Xác nhận đăng xuất",
                        JOptionPane.YES_NO_OPTION
                );
                if (confirm == JOptionPane.YES_OPTION) {
                    Window win = SwingUtilities.getWindowAncestor(mainPanel);
                    if (win != null) {
                        win.dispose();
                    }
                    Login.main(new String[]{});
                }
            }
        });
        sb.add(logoutItem);
        sb.add(Box.createVerticalStrut(8));

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
        footerPanel.setBackground(SIDEBAR_DARKER);
        footerPanel.setBorder(new EmptyBorder(12, 16, 14, 16));
        footerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel lblFooter1 = new JLabel("Đại học Điện lực");
        lblFooter1.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblFooter1.setForeground(TEXT_WHITE);
        lblFooter1.setAlignmentX(Component.LEFT_ALIGNMENT);
        footerPanel.add(lblFooter1);

        JLabel lblFooter2 = new JLabel("Kết nối tri thức - Kiến tạo tương lai");
        lblFooter2.setFont(new Font("Segoe UI", Font.ITALIC, 9));
        lblFooter2.setForeground(TEXT_MUTED);
        lblFooter2.setAlignmentX(Component.LEFT_ALIGNMENT);
        footerPanel.add(lblFooter2);

        sb.add(footerPanel);

        return sb;
    }

    // ============ SINGLE NAV ITEM ============
    private JPanel createNavItem(String icon, String text, int index) {
        JPanel item = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(6, 0, getWidth() - 12, getHeight(), 10, 10));
                g2.dispose();
            }
        };
        item.setOpaque(false);
        item.setBackground(SIDEBAR_BG); // default transparent-like
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        item.setPreferredSize(new Dimension(220, 40));
        item.setBorder(new EmptyBorder(8, 20, 8, 16));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblIcon = new JLabel(icon + "  ");
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblIcon.setForeground(TEXT_WHITE);
        item.add(lblIcon, BorderLayout.WEST);

        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblText.setForeground(TEXT_WHITE);
        item.add(lblText, BorderLayout.CENTER);

        if (index >= 0) {
            navItems.add(item);
        }

        // Hover effect
        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (item != activeNavItem) {
                    item.setBackground(NAV_HOVER_BG);
                    item.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (item != activeNavItem) {
                    item.setBackground(SIDEBAR_BG);
                    item.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (index < 0) return; // logout handled separately
                switch (index) {
                    case 0 -> showPanel(trangchu.getMainPanel(), item);
                    case 1 -> showPanel(lapbaocaothongke.getMainPanel(), item);
                    case 2 -> showPanel(quanlytailieusukien.getMainPanel(), item);
                    case 3 -> showPanel(quanlynguoithamgia.getMainPanel(), item);
                    case 4 -> showPanel(quanlythongbao.getMainPanel(), item);
                }
            }
        });

        return item;
    }

    // ============ SWITCH CONTENT ============
    private void showPanel(JPanel panel, JPanel activeItem) {
        // Reset all nav items
        if (navItems != null) {
            for (JPanel nav : navItems) {
                nav.setBackground(SIDEBAR_BG);
                nav.repaint();
            }
        }

        // Highlight active
        activeNavItem = activeItem;
        if (activeItem != null) {
            activeItem.setBackground(NAV_ACTIVE_BG);
            activeItem.repaint();
        }

        // Swap content
        contentPanel.removeAll();
        contentPanel.add(panel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void $$$setupUI$$$() {
    }

    public static void main(String[] args) {
        // Bắt buộc người dùng phải qua màn hình Đăng nhập trước
        Login.main(args);
    }
}
