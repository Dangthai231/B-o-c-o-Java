package com.eventmanager.fe.gui.HocSinh;

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
 * Mỗi màn hình là một file riêng trong gói HocSinh, cùng kế thừa TrangSinhVien.
 */
public class DashboardHocSinh implements TrangSinhVien.DieuHuong {

    public static final String TRANG_CHU = "trangchu";
    public static final String TRA_CUU = "tracuu";
    public static final String CUA_TOI = "cuatoi";
    public static final String DANH_GIA = "danhgia";
    public static final String CA_NHAN = "canhan";

    private final String currentUsername;
    private JPanel mainPanel;
    private JPanel sidebar;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel noiDung = new JPanel(cardLayout);
    private final Map<String, MucMenu> cacMuc = new LinkedHashMap<>();
    private final Map<String, TrangSinhVien> cacTrang = new LinkedHashMap<>();
    private final JLabel lblTaiKhoan = new JLabel();

    // Các trang con
    private TraCuuSuKien traCuuSuKien;
    private DanhGiaPhanHoi danhGiaPhanHoi;

    public DashboardHocSinh() {
        this("hocsinh01");
    }

    public DashboardHocSinh(String username) {
        this.currentUsername = (username == null || username.isBlank()) ? "hocsinh01" : username.trim();
        initComponents();
    }

    private void initComponents() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1220, 740));

        // 1. Khởi tạo các trang con (mỗi trang tự gọi API khi được mở)
        traCuuSuKien = new TraCuuSuKien(this);
        danhGiaPhanHoi = new DanhGiaPhanHoi(this);
        cacTrang.put(TRANG_CHU, new TrangChuSinhVien(this));
        cacTrang.put(TRA_CUU, traCuuSuKien);
        cacTrang.put(CUA_TOI, new SuKienCuaToi(this));
        cacTrang.put(DANH_GIA, danhGiaPhanHoi);
        cacTrang.put(CA_NHAN, new ThongTinCaNhan(this, this::capNhatTenHienThi));

        noiDung.setBackground(Theme.BG);
        cacTrang.forEach((ma, trang) -> noiDung.add(trang, ma));

        // 2. Ghép khung: Sidebar bên trái + (Thanh trên + Nội dung) bên phải
        mainPanel.add(taoSidebar(), BorderLayout.WEST);
        JPanel phai = new JPanel(new BorderLayout());
        phai.add(taoThanhTren(), BorderLayout.NORTH);
        phai.add(noiDung, BorderLayout.CENTER);
        mainPanel.add(phai, BorderLayout.CENTER);

        moTrang(TRANG_CHU);
        capNhatTenHienThi();

        // Tự nới rộng cửa sổ nếu màn hình đủ lớn
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
        sidebar.setPreferredSize(new Dimension(228, 0));

        JPanel logo = new JPanel(new BorderLayout(10, 0));
        logo.setOpaque(false);
        logo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.SIDEBAR_LINE),
                new EmptyBorder(18, 14, 18, 8)));
        logo.add(new JLabel(Icons.of(Icons.CAP, 28, Color.WHITE)), BorderLayout.WEST);
        JPanel chuLogo = new JPanel();
        chuLogo.setOpaque(false);
        chuLogo.setLayout(new BoxLayout(chuLogo, BoxLayout.Y_AXIS));
        chuLogo.add(UI.nhan("HỆ THỐNG QUẢN LÝ SỰ KIỆN", Theme.font(Font.BOLD, 11), Color.WHITE));
        chuLogo.add(Box.createVerticalStrut(3));
        chuLogo.add(UI.nhan("Cổng thông tin Sinh viên", Theme.font(Font.PLAIN, 10), Theme.SIDEBAR_TEXT));
        logo.add(chuLogo, BorderLayout.CENTER);
        sidebar.add(logo, BorderLayout.NORTH);

        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(14, 12, 12, 12));
        themMuc(menu, TRANG_CHU, "Trang chủ", Icons.HOME);
        themMuc(menu, TRA_CUU, "Tra cứu sự kiện", Icons.SEARCH);
        themMuc(menu, CUA_TOI, "Sự kiện của tôi", Icons.CALENDAR);
        themMuc(menu, DANH_GIA, "Đánh giá và phản hồi", Icons.EDIT);
        themMuc(menu, CA_NHAN, "Thông tin cá nhân", Icons.USER);
        sidebar.add(menu, BorderLayout.CENTER);

        JPanel chan = new JPanel(new BorderLayout(0, 10));
        chan.setOpaque(false);
        chan.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.SIDEBAR_LINE),
                new EmptyBorder(10, 12, 14, 12)));
        chan.add(new MucMenu("Đăng xuất", Icons.LOGOUT, this::dangXuat), BorderLayout.NORTH);
        JPanel chuChan = new JPanel(new BorderLayout(8, 0));
        chuChan.setOpaque(false);
        chuChan.add(new JLabel(Icons.of(Icons.CAP, 20, Theme.SIDEBAR_TEXT)), BorderLayout.WEST);
        JPanel dongChu = new JPanel();
        dongChu.setOpaque(false);
        dongChu.setLayout(new BoxLayout(dongChu, BoxLayout.Y_AXIS));
        dongChu.add(UI.nhan("Đại học Điện lực", Theme.font(Font.PLAIN, 11), Color.WHITE));
        dongChu.add(UI.nhan("Liên Chi đoàn Công nghệ thông tin", Theme.font(Font.PLAIN, 9), Theme.SIDEBAR_TEXT));
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

    // ======================= THANH TRÊN CÙNG =======================
    private JPanel taoThanhTren() {
        JPanel tren = new JPanel(new BorderLayout());
        tren.setBackground(Color.WHITE);
        tren.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                new EmptyBorder(10, 18, 10, 22)));

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

        // Gõ tên sự kiện rồi Enter -> mở Tra cứu với từ khóa đó
        UI.TextFieldDep timNhanh = UI.oTimKiem("Tìm sự kiện rồi nhấn Enter...");
        timNhanh.setPreferredSize(new Dimension(280, 36));
        timNhanh.addActionListener(e -> {
            timSuKien(timNhanh.getText().trim());
            timNhanh.setText("");
        });

        lblTaiKhoan.setFont(Theme.BODY_BOLD);
        lblTaiKhoan.setForeground(Theme.TEXT);
        lblTaiKhoan.setIconTextGap(10);
        JPanel khoiTk = UI.hang(8, lblTaiKhoan, new JLabel(Icons.of(Icons.CHEVRON_DOWN, 16, Theme.TEXT_2)));
        khoiTk.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPopupMenu popup = new JPopupMenu();
        JMenuItem miCaNhan = new JMenuItem("Thông tin cá nhân", Icons.of(Icons.USER, 16, Theme.TEXT_2));
        miCaNhan.addActionListener(e -> moTrang(CA_NHAN));
        JMenuItem miDangXuat = new JMenuItem("Đăng xuất", Icons.of(Icons.LOGOUT, 16, Theme.DANGER));
        miDangXuat.addActionListener(e -> dangXuat());
        for (JMenuItem mi : new JMenuItem[]{miCaNhan, miDangXuat}) {
            mi.setFont(Theme.BODY);
            mi.setBorder(new EmptyBorder(8, 12, 8, 24));
            mi.setBackground(Color.WHITE);
            popup.add(mi);
        }
        khoiTk.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                popup.show(khoiTk, 0, khoiTk.getHeight() + 6);
            }
        });

        tren.add(UI.hangPhai(16, timNhanh, khoiTk), BorderLayout.EAST);
        return tren;
    }

    /** Hiện tên đăng nhập cạnh avatar (giống DashboardCanbo hiện "canbo01"). */
    private void capNhatTenHienThi() {
        lblTaiKhoan.setText(currentUsername);
        lblTaiKhoan.setIcon(UI.avatar("SV", 34, Theme.SIDEBAR));
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
        TrangSinhVien trang = cacTrang.get(ma);
        if (trang != null) trang.taiLai();
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void timSuKien(String tuKhoa) {
        traCuuSuKien.datTuKhoa(tuKhoa);
        moTrang(TRA_CUU);
    }

    @Override
    public void danhGia(long suKienId) {
        danhGiaPhanHoi.chonSuKienKhiTai(suKienId);
        moTrang(DANH_GIA);
    }

    @Override
    public String tenDangNhap() {
        return currentUsername;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    /** Một mục trên menu bên trái: bo góc, đổi màu khi rê chuột / đang chọn (giống DashboardCanbo). */
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