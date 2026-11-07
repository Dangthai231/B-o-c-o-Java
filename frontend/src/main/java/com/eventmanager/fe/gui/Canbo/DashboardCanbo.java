package com.eventmanager.fe.gui.Canbo;

import Canbo.Trangchu;
import com.eventmanager.fe.gui.Admin.common.*;
import com.eventmanager.fe.gui.Login.Login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GIAO DIỆN CHÍNH CÁN BỘ PHỤ TRÁCH SỰ KIỆN:
 * Thiết kế chuẩn hiện đại, đồng bộ 100% màu sắc và vector icon với DashboardAdmin:
 *
 *  ┌──────────────┬──────────────────────────────────────────────┐
 *  │  LOGO        │  ☰                    [Tìm kiếm]  🔔  CB canbo01 │  <- thanh trên trắng
 *  │  Trang chủ   ├──────────────────────────────────────────────┤
 *  │  Báo cáo     │                                              │
 *  │  Tài liệu    │        VÙNG NỘI DUNG (CardLayout)            │
 *  │  Người t.gia │                                              │
 *  │  Thông báo   │                                              │
 *  │  Đăng xuất   │                                              │
 *  └──────────────┴──────────────────────────────────────────────┘
 */
public class DashboardCanbo {

    public static final String TRANG_CHU = "trangchu";
    public static final String BAO_CAO = "baocao";
    public static final String TAI_LIEU = "tailieu";
    public static final String NGUOI_THAM_GIA = "nguoithamgia";
    public static final String THONG_BAO = "thongbao";

    private final String currentUsername;
    private JPanel mainPanel;
    private JPanel sidebar;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel noiDung = new JPanel(cardLayout);
    private final Map<String, MucMenu> cacMuc = new LinkedHashMap<>();

    // Các trang con
    private Trangchu trangchu;
    private Lapbaocaothongke lapbaocaothongke;
    private Quanlytailieusukien quanlytailieusukien;
    private Quanlynguoithamgia quanlynguoithamgia;
    private Quanlythongbao quanlythongbao;

    public DashboardCanbo() {
        this("canbo01");
    }

    public DashboardCanbo(String username) {
        this.currentUsername = (username == null || username.isBlank()) ? "canbo01" : username.trim();
        initComponents();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1220, 740));

        // 1. Khởi tạo các trang con
        trangchu = new Trangchu(this::moTrang);
        lapbaocaothongke = new Lapbaocaothongke();
        quanlytailieusukien = new Quanlytailieusukien();
        quanlynguoithamgia = new Quanlynguoithamgia();
        quanlythongbao = new Quanlythongbao();

        noiDung.setBackground(Theme.BG);
        noiDung.add(trangchu.getMainPanel(), TRANG_CHU);
        noiDung.add(lapbaocaothongke.getMainPanel(), BAO_CAO);
        noiDung.add(quanlytailieusukien.getMainPanel(), TAI_LIEU);
        noiDung.add(quanlynguoithamgia.getMainPanel(), NGUOI_THAM_GIA);
        noiDung.add(quanlythongbao.getMainPanel(), THONG_BAO);

        // 2. Ghép khung: Cột trái full-height Sidebar + Vùng phải (Thanh trên trắng + Nội dung)
        mainPanel.add(taoSidebar(), BorderLayout.WEST);

        JPanel phai = new JPanel(new BorderLayout());
        phai.add(taoThanhTren(), BorderLayout.NORTH);
        phai.add(noiDung, BorderLayout.CENTER);
        mainPanel.add(phai, BorderLayout.CENTER);

        // Mặc định mở Trang chủ
        moTrang(TRANG_CHU);

        // Tự động nới rộng cửa sổ nếu màn hình đủ lớn
        mainPanel.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && mainPanel.isShowing()) {
                Window w = SwingUtilities.getWindowAncestor(mainPanel);
                Rectangle manHinh = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                if (w != null && w.getWidth() < 1280 && manHinh.width >= 1300 && manHinh.height >= 800) {
                    w.setSize(1280, 780);
                    w.setLocationRelativeTo(null);
                }
            }
        });
    }

    // ======================= THANH BÊN TRÁI (SIDEBAR) =======================
    private JPanel taoSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(228, 0));

        // 1. Logo trường & khoa
        JPanel logo = new JPanel(new BorderLayout(10, 0));
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.SIDEBAR_LINE),
                new EmptyBorder(18, 14, 18, 8)
        ));
        logo.add(new JLabel(Icons.of(Icons.CAP, 28, Color.WHITE)), BorderLayout.WEST);

        JPanel chuLogo = new JPanel();
        chuLogo.setOpaque(false);
        chuLogo.setLayout(new BoxLayout(chuLogo, BoxLayout.Y_AXIS));
        chuLogo.add(UI.nhan("HỆ THỐNG QUẢN LÝ SỰ KIỆN", Theme.font(Font.BOLD, 11), Color.WHITE));
        chuLogo.add(Box.createVerticalStrut(3));
        chuLogo.add(UI.nhan("BCH LCĐ CNS - ĐH Điện lực", Theme.font(Font.PLAIN, 10), Theme.SIDEBAR_TEXT));
        logo.add(chuLogo, BorderLayout.CENTER);
        sidebar.add(logo, BorderLayout.NORTH);

        // 2. Danh sách Menu
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(14, 12, 12, 12));

        themMuc(menu, TRANG_CHU, "Trang chủ", Icons.HOME);
        themMuc(menu, BAO_CAO, "Lập báo cáo và thống kê", Icons.CHART);
        themMuc(menu, TAI_LIEU, "Quản lý tài liệu sự kiện", Icons.FILE);
        themMuc(menu, NGUOI_THAM_GIA, "Quản lý người tham gia", Icons.USERS);
        themMuc(menu, THONG_BAO, "Quản lý thông báo", Icons.BELL);

        sidebar.add(menu, BorderLayout.CENTER);

        // 3. Chân Sidebar: Nút Đăng xuất + Thông tin ĐH Điện lực
        JPanel chan = new JPanel(new BorderLayout(0, 10));
        chan.setOpaque(false);
        chan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.SIDEBAR_LINE),
                new EmptyBorder(10, 12, 14, 12)
        ));

        // Nút Đăng xuất
        MucMenu btnDangXuat = new MucMenu("Đăng xuất", Icons.LOGOUT, this::dangXuat);
        chan.add(btnDangXuat, BorderLayout.NORTH);

        // Thông tin bản quyền
        JPanel chuChan = new JPanel(new BorderLayout(8, 0));
        chuChan.setOpaque(false);
        chuChan.add(new JLabel(Icons.of(Icons.CAP, 20, Theme.SIDEBAR_TEXT)), BorderLayout.WEST);

        JPanel dongChu = new JPanel();
        dongChu.setOpaque(false);
        dongChu.setLayout(new BoxLayout(dongChu, BoxLayout.Y_AXIS));
        dongChu.add(UI.nhan("Đại học Điện lực", Theme.font(Font.PLAIN, 11), Color.WHITE));
        dongChu.add(UI.nhan("Kết nối tri thức - Kiến tạo tương lai", Theme.font(Font.PLAIN, 9), Theme.SIDEBAR_TEXT));
        chuChan.add(dongChu, BorderLayout.CENTER);

        chan.add(chuChan, BorderLayout.SOUTH);
        sidebar.add(chan, BorderLayout.SOUTH);

        return sidebar;
    }

    private void themMuc(JPanel menu, String ma, String ten, String icon) {
        MucMenu m = new MucMenu(ten, icon, () -> moTrang(ma));
        cacMuc.put(ma, m);
        menu.add(m);
        menu.add(Box.createVerticalStrut(4));
    }

    // ======================= THANH TRÊN CÙNG (TOP HEADER) =======================
    private JPanel taoThanhTren() {
        JPanel tren = new JPanel(new BorderLayout());
        tren.setBackground(Color.WHITE);
        tren.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(10, 18, 10, 22)
        ));

        // ☰ Nút ẩn / hiện thanh bên trái
        JLabel hamburger = new JLabel(Icons.of(Icons.MENU, 22, Theme.TEXT));
        hamburger.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hamburger.setToolTipText("Ẩn / hiện menu bên trái");
        hamburger.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sidebar.setVisible(!sidebar.isVisible());
                mainPanel.revalidate();
            }
        });
        tren.add(hamburger, BorderLayout.WEST);

        // Ô tìm kiếm nhanh bo tròn
        UI.TextFieldDep timNhanh = UI.oTimKiem("Tìm kiếm sự kiện, người tham gia...");
        timNhanh.setPreferredSize(new Dimension(280, 36));

        // Chuông thông báo
        JPanel chuong = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }
        };
        chuong.setOpaque(false);
        JLabel iconChuong = new JLabel(Icons.of(Icons.BELL, 22, Theme.TEXT));
        iconChuong.setBounds(6, 7, 24, 24);

        JLabel soChuong = new JLabel("3", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.muot(g);
                g2.setColor(Theme.DANGER);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        soChuong.setFont(Theme.font(Font.BOLD, 9));
        soChuong.setForeground(Color.WHITE);
        soChuong.setBounds(19, 2, 16, 16);
        chuong.add(soChuong);
        chuong.add(iconChuong);
        chuong.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chuong.setToolTipText("Thông báo hệ thống");
        chuong.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moTrang(THONG_BAO);
            }
        });

        // Avatar + Tên cán bộ + Menu xổ xuống
        String initials = UI.vietTat(currentUsername);
        if (initials.isEmpty()) initials = "CB";

        JLabel lblAvatar = new JLabel(currentUsername, UI.avatar(initials, 34, Theme.SIDEBAR), SwingConstants.LEFT);
        lblAvatar.setFont(Theme.BODY_BOLD);
        lblAvatar.setForeground(Theme.TEXT);
        lblAvatar.setIconTextGap(10);

        JLabel muiTen = new JLabel(Icons.of(Icons.CHEVRON_DOWN, 16, Theme.TEXT_2));
        JPanel khoiTk = UI.hang(8, lblAvatar, muiTen);
        khoiTk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPopupMenu popup = new JPopupMenu();
        JMenuItem miDangXuat = new JMenuItem("Đăng xuất", Icons.of(Icons.LOGOUT, 16, Theme.DANGER));
        miDangXuat.setFont(Theme.BODY);
        miDangXuat.setBorder(new EmptyBorder(8, 12, 8, 24));
        miDangXuat.setBackground(Color.WHITE);
        miDangXuat.addActionListener(e -> dangXuat());
        popup.add(miDangXuat);

        khoiTk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.show(khoiTk, 0, khoiTk.getHeight() + 6);
            }
        });

        JPanel phai = UI.hangPhai(16, timNhanh, chuong, khoiTk);
        tren.add(phai, BorderLayout.EAST);
        return tren;
    }

    private void dangXuat() {
        if (HopThoai.xacNhan(mainPanel, "Đăng xuất", "Bạn có chắc muốn đăng xuất khỏi hệ thống?",
                "Đăng xuất", UI.Kieu.NGUY_HIEM)) {
            Window win = SwingUtilities.getWindowAncestor(mainPanel);
            if (win != null) win.dispose();
            Login.main(new String[]{});
        }
    }

    // ======================= ĐIỀU HƯỚNG TỚI TRANG =======================
    public void moTrang(String ma) {
        cacMuc.forEach((k, m) -> m.setChon(k.equals(ma)));
        cardLayout.show(noiDung, ma);

        if (TRANG_CHU.equals(ma)) {
            trangchu.taiLai();
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public static void main(String[] args) {
        // Mở màn hình Login chuẩn
        Login.main(args);
    }

    /** Một mục trên menu bên trái: bo góc 10px, đổi màu khi hover/chọn */
    private static class MucMenu extends UI.RoundPanel {
        private final JLabel lbl;
        private final String icon;
        private boolean chon;
        private boolean hover;

        MucMenu(String ten, String icon, Runnable onClick) {
            super(new BorderLayout(), 10, Theme.SIDEBAR, null);
            this.icon = icon;
            lbl = new JLabel(ten, Icons.of(icon, 18, Theme.SIDEBAR_TEXT), SwingConstants.LEFT);
            lbl.setFont(Theme.font(Font.PLAIN, 13));
            lbl.setForeground(Theme.SIDEBAR_TEXT);
            lbl.setIconTextGap(12);
            lbl.setBorder(new EmptyBorder(10, 12, 10, 10));
            add(lbl);

            setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    onClick.run();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    veLai();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    veLai();
                }
            });
        }

        void setChon(boolean c) {
            chon = c;
            veLai();
        }

        private void veLai() {
            setNen(chon ? Theme.SIDEBAR_ACTIVE : hover ? Theme.SIDEBAR_HOVER : Theme.SIDEBAR);
            Color mau = chon ? Color.WHITE : Theme.SIDEBAR_TEXT;
            lbl.setForeground(mau);
            lbl.setFont(Theme.font(chon ? Font.BOLD : Font.PLAIN, 13));
            lbl.setIcon(Icons.of(icon, 18, mau));
        }
    }
}
