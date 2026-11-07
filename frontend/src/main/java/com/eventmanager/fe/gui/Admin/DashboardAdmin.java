package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.eventmanager.fe.gui.Login.Login;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * KHUNG CHÍNH của Quản trị viên (giống ảnh thiết kế):
 *
 *  ┌──────────────┬──────────────────────────────────────────────┐
 *  │  LOGO        │  ☰                    [Tìm kiếm]  🔔  AD Admin│  <- thanh trên
 *  │  Tổng quan   ├──────────────────────────────────────────────┤
 *  │  Sự kiện     │                                              │
 *  │  Thông báo   │        VÙNG NỘI DUNG (CardLayout)            │
 *  │  Tài khoản   │        mỗi menu = 1 màn hình                 │
 *  │  Phân quyền  │                                              │
 *  │  Báo cáo     │                                              │
 *  │  Cài đặt     │                                              │
 *  └──────────────┴──────────────────────────────────────────────┘
 */
public class DashboardAdmin implements TrangAdmin.DieuHuong {

    // Mã các trang
    public static final String TONG_QUAN = "tongquan", SU_KIEN = "sukien", THONG_BAO = "thongbao",
            TAI_KHOAN = "taikhoan", PHAN_QUYEN = "phanquyen", BAO_CAO = "baocao", CAI_DAT = "caidat";

    private final String tenDangNhap;
    private JPanel mainPanel;
    private JPanel sidebar;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel noiDung = new JPanel(cardLayout);
    private final Map<String, TrangAdmin> cacTrang = new LinkedHashMap<>();
    private final Map<String, MucMenu> cacMuc = new LinkedHashMap<>();
    private JLabel soChuong;

    private QuanLyTaiKhoanPanel trangTaiKhoan;
    private PhanQuyenPanel trangPhanQuyen;

    public DashboardAdmin() {
        this("admin");
    }

    public DashboardAdmin(String tenDangNhap) {
        this.tenDangNhap = (tenDangNhap == null || tenDangNhap.isBlank()) ? "admin" : tenDangNhap.trim();
        initComponents();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1150, 720));

        // 1. Tạo các trang
        trangTaiKhoan = new QuanLyTaiKhoanPanel(this);
        trangPhanQuyen = new PhanQuyenPanel(this);
        cacTrang.put(TONG_QUAN, new TongQuanPanel(this));
        cacTrang.put(SU_KIEN, new QuanLySuKienPanel(this));
        cacTrang.put(THONG_BAO, new QuanLyThongBaoPanel(this));
        cacTrang.put(TAI_KHOAN, trangTaiKhoan);
        cacTrang.put(PHAN_QUYEN, trangPhanQuyen);
        cacTrang.put(BAO_CAO, new BaoCaoPanel(this));
        cacTrang.put(CAI_DAT, new CaiDatPanel(this));
        noiDung.setBackground(Theme.BG);
        cacTrang.forEach((ma, trang) -> noiDung.add(trang, ma));

        // 2. Ghép khung
        mainPanel.add(taoSidebar(), BorderLayout.WEST);
        JPanel phai = new JPanel(new BorderLayout());
        phai.add(taoThanhTren(), BorderLayout.NORTH);
        phai.add(noiDung, BorderLayout.CENTER);
        mainPanel.add(phai, BorderLayout.CENTER);

        moTrang(TONG_QUAN);

        // Bảng của Admin nhiều cột -> khi cửa sổ hiện lên, nới rộng tối thiểu 1280x780 (nếu màn hình đủ lớn)
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

    // ======================= THANH BÊN TRÁI =======================
    private JPanel taoSidebar() {
        sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Theme.SIDEBAR);
        sidebar.setPreferredSize(new Dimension(222, 0));

        // Logo
        JPanel logo = new JPanel(new BorderLayout(10, 0));
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.SIDEBAR_LINE), new EmptyBorder(18, 14, 18, 8)));
        logo.add(new JLabel(Icons.of(Icons.CAP, 28, Color.WHITE)), BorderLayout.WEST);
        JPanel chuLogo = new JPanel();
        chuLogo.setOpaque(false);
        chuLogo.setLayout(new BoxLayout(chuLogo, BoxLayout.Y_AXIS));
        chuLogo.add(UI.nhan("HỆ THỐNG QUẢN LÝ SỰ KIỆN", Theme.font(Font.BOLD, 11), Color.WHITE));
        chuLogo.add(Box.createVerticalStrut(3));
        chuLogo.add(UI.nhan("BCH LCĐ CNS - ĐH Điện lực", Theme.font(Font.PLAIN, 10), Theme.SIDEBAR_TEXT));
        logo.add(chuLogo, BorderLayout.CENTER);
        sidebar.add(logo, BorderLayout.NORTH);

        // Menu
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(14, 12, 12, 12));
        themMuc(menu, TONG_QUAN, "Tổng quan", Icons.HOME);
        themMuc(menu, SU_KIEN, "Quản lý sự kiện", Icons.CALENDAR);
        themMuc(menu, THONG_BAO, "Quản lý thông báo", Icons.BELL);
        themMuc(menu, TAI_KHOAN, "Quản lý tài khoản", Icons.USER);
        themMuc(menu, PHAN_QUYEN, "Phân quyền người dùng", Icons.SHIELD);
        themMuc(menu, BAO_CAO, "Báo cáo", Icons.CHART);
        themMuc(menu, CAI_DAT, "Cài đặt", Icons.GEAR);
        sidebar.add(menu, BorderLayout.CENTER);

        // Chân
        JPanel chan = new JPanel(new BorderLayout(10, 0));
        chan.setOpaque(false);
        chan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.SIDEBAR_LINE), new EmptyBorder(14, 18, 16, 12)));
        chan.add(new JLabel(Icons.of(Icons.CAP, 22, Theme.SIDEBAR_TEXT)), BorderLayout.WEST);
        JPanel chuChan = new JPanel();
        chuChan.setOpaque(false);
        chuChan.setLayout(new BoxLayout(chuChan, BoxLayout.Y_AXIS));
        chuChan.add(UI.nhan("Đại học Điện lực", Theme.font(Font.PLAIN, 11), Color.WHITE));
        chuChan.add(UI.nhan("Kết nối tri thức - Kiến tạo tương lai", Theme.font(Font.PLAIN, 9), Theme.SIDEBAR_TEXT));
        chan.add(chuChan, BorderLayout.CENTER);
        sidebar.add(chan, BorderLayout.SOUTH);
        return sidebar;
    }

    private void themMuc(JPanel menu, String ma, String ten, String icon) {
        MucMenu m = new MucMenu(ten, icon, () -> moTrang(ma));
        cacMuc.put(ma, m);
        menu.add(m);
        menu.add(Box.createVerticalStrut(4));
    }

    /** Một mục trên menu trái: nền xanh khi đang chọn, sáng nhẹ khi rê chuột. */
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

    // ======================= THANH TRÊN CÙNG =======================
    private JPanel taoThanhTren() {
        JPanel tren = new JPanel(new BorderLayout());
        tren.setBackground(Color.WHITE);
        tren.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER), new EmptyBorder(10, 18, 10, 22)));

        // ☰ ẩn / hiện thanh bên
        JLabel hamburger = new JLabel(Icons.of(Icons.MENU, 22, Theme.TEXT));
        hamburger.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        hamburger.setToolTipText("Ẩn / hiện menu");
        hamburger.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sidebar.setVisible(!sidebar.isVisible());
                mainPanel.revalidate();
            }
        });
        tren.add(hamburger, BorderLayout.WEST);

        // Ô tìm nhanh tài khoản
        UI.TextFieldDep timNhanh = UI.oTimKiem("Tìm tài khoản...");
        timNhanh.setPreferredSize(new Dimension(220, 36));
        timNhanh.setToolTipText("Nhập tên / email / mã số rồi nhấn Enter");
        timNhanh.addActionListener(e -> {
            timTaiKhoan(timNhanh.getText().trim());
            timNhanh.setText("");
        });

        // Chuông: số việc đang chờ Admin xử lý
        JPanel chuong = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(36, 36);
            }
        };
        chuong.setOpaque(false);
        JLabel iconChuong = new JLabel(Icons.of(Icons.BELL, 22, Theme.TEXT));
        iconChuong.setBounds(6, 7, 24, 24);
        soChuong = new JLabel("", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                if (getText().isEmpty()) return;
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
        chuong.setToolTipText("Việc cần xử lý");
        chuong.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                moTrang(TONG_QUAN);
            }
        });

        // Avatar + tên + menu xổ xuống
        JLabel tk = new JLabel("Admin", UI.avatar("AD", 34, Theme.SIDEBAR), SwingConstants.LEFT);
        tk.setFont(Theme.BODY_BOLD);
        tk.setForeground(Theme.TEXT);
        tk.setIconTextGap(10);
        JLabel muiTen = new JLabel(Icons.of(Icons.CHEVRON_DOWN, 16, Theme.TEXT_2));
        JPanel khoiTk = UI.hang(8, tk, muiTen);
        khoiTk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPopupMenu popup = new JPopupMenu();
        JMenuItem miCaiDat = new JMenuItem("Đổi mật khẩu", Icons.of(Icons.KEY, 16, Theme.TEXT_2));
        JMenuItem miDangXuat = new JMenuItem("Đăng xuất", Icons.of(Icons.LOGOUT, 16, Theme.DANGER));
        for (JMenuItem mi : new JMenuItem[]{miCaiDat, miDangXuat}) {
            mi.setFont(Theme.BODY);
            mi.setBorder(new EmptyBorder(8, 10, 8, 24));
            mi.setBackground(Color.WHITE);
            popup.add(mi);
        }
        miCaiDat.addActionListener(e -> moTrang(CAI_DAT));
        miDangXuat.addActionListener(e -> dangXuat());
        khoiTk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.show(khoiTk, 0, khoiTk.getHeight() + 6);
            }
        });

        JPanel phai = UI.hangPhai(14, timNhanh, chuong, khoiTk);
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

    // ======================= ĐIỀU HƯỚNG =======================
    @Override
    public void moTrang(String ma) {
        cacMuc.forEach((k, m) -> m.setChon(k.equals(ma)));
        cardLayout.show(noiDung, ma);
        cacTrang.get(ma).taiLai();
        capNhatChuong();
    }

    @Override
    public void timTaiKhoan(String tuKhoa) {
        trangTaiKhoan.datTuKhoa(tuKhoa);
        moTrang(TAI_KHOAN);
    }

    @Override
    public void phanQuyenCho(long taiKhoanId) {
        trangPhanQuyen.chonSan(taiKhoanId);
        moTrang(PHAN_QUYEN);
    }

    /** Lấy tổng số việc đang chờ (kế hoạch + thông báo + yêu cầu) để hiện trên chuông. */
    @Override
    public void capNhatChuong() {
        new SwingWorker<Long, Void>() {
            @Override
            protected Long doInBackground() throws Exception {
                JsonObject o = Api.obj(Api.get("/api/admin/tong-quan"));
                return Api.n(o, "tongViecCanXuLy");
            }

            @Override
            protected void done() {
                try {
                    long so = get();
                    soChuong.setText(so <= 0 ? "" : (so > 9 ? "9+" : String.valueOf(so)));
                } catch (Exception ignored) {
                    soChuong.setText("");
                }
                soChuong.repaint();
            }
        }.execute();
    }

    @Override
    public String tenDangNhap() {
        return tenDangNhap;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
}
