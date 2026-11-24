package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.api.Api;

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
            TAI_KHOAN = "taikhoan", PHAN_QUYEN = "phanquyen", BAO_CAO = "baocao", CAI_DAT = "caidat",
            CONG_VIEC = "congviec";

    private final String tenDangNhap;
    private CardLayout cardLayout;
    private boolean dangTaiChuong;
    private final Map<String, TrangAdmin> cacTrang = new LinkedHashMap<>();
    private final Map<String, JButton> cacMuc = new LinkedHashMap<>();

    private QuanLyTaiKhoanPanel trangTaiKhoan;
    private PhanQuyenPanel trangPhanQuyen;

    public DashboardAdmin() {
        this("");
    }

    public DashboardAdmin(String tenDangNhap) {
        this.tenDangNhap = (tenDangNhap == null || tenDangNhap.isBlank()) ? "" : tenDangNhap.trim();
        initComponents();
    }

    private void initComponents() {
        $$$setupUI$$$();
        trangTriChanMenu();
        cardLayout = (CardLayout) noiDung.getLayout();
        trangTaiKhoan = new QuanLyTaiKhoanPanel(this);
        trangPhanQuyen = new PhanQuyenPanel(this);
        cacTrang.put(TONG_QUAN, new TongQuanPanel(this));
        cacTrang.put(SU_KIEN, new QuanLySuKienPanel(this));
        cacTrang.put(THONG_BAO, new QuanLyThongBaoPanel(this));
        cacTrang.put(TAI_KHOAN, trangTaiKhoan);
        cacTrang.put(PHAN_QUYEN, trangPhanQuyen);
        cacTrang.put(CONG_VIEC, new QuanLyCongViecPanel(this));
        cacTrang.put(BAO_CAO, new BaoCaoPanel(this));
        cacTrang.put(CAI_DAT, new CaiDatPanel(this));
        cacTrang.forEach((ma, trang) -> noiDung.add(trang, ma));
        cacMuc.put(TONG_QUAN, btnTongQuan);
        btnTongQuan.setIcon(Icons.of(Icons.HOME, 18, Color.WHITE));
        btnTongQuan.addActionListener(e -> moTrang(TONG_QUAN));
        cacMuc.put(SU_KIEN, btnQuanLySuKien);
        btnQuanLySuKien.setIcon(Icons.of(Icons.CALENDAR, 18, Color.WHITE));
        btnQuanLySuKien.addActionListener(e -> moTrang(SU_KIEN));
        cacMuc.put(THONG_BAO, btnQuanLyThongBao);
        btnQuanLyThongBao.setIcon(Icons.of(Icons.BELL, 18, Color.WHITE));
        btnQuanLyThongBao.addActionListener(e -> moTrang(THONG_BAO));
        cacMuc.put(TAI_KHOAN, btnQuanLyTaiKhoan);
        btnQuanLyTaiKhoan.setIcon(Icons.of(Icons.USER, 18, Color.WHITE));
        btnQuanLyTaiKhoan.addActionListener(e -> moTrang(TAI_KHOAN));
        cacMuc.put(PHAN_QUYEN, btnPhanQuyen);
        btnPhanQuyen.setIcon(Icons.of(Icons.SHIELD, 18, Color.WHITE));
        btnPhanQuyen.addActionListener(e -> moTrang(PHAN_QUYEN));
        cacMuc.put(CONG_VIEC, btnCongViec);
        btnCongViec.setIcon(Icons.of(Icons.LIST, 18, Color.WHITE));
        btnCongViec.addActionListener(e -> moTrang(CONG_VIEC));
        cacMuc.put(BAO_CAO, btnBaoCao);
        btnBaoCao.setIcon(Icons.of(Icons.CHART, 18, Color.WHITE));
        btnBaoCao.addActionListener(e -> moTrang(BAO_CAO));
        cacMuc.put(CAI_DAT, btnCaiDat);
        btnCaiDat.setIcon(Icons.of(Icons.GEAR, 18, Color.WHITE));
        btnCaiDat.addActionListener(e -> moTrang(CAI_DAT));
        btnThuGonMenu.addActionListener(e -> { sidebar.setVisible(!sidebar.isVisible()); mainPanel.revalidate(); });
        btnThuGonMenu.setIcon(Icons.of(Icons.MENU, 20, Theme.TEXT));
        txtTimNhanh.addActionListener(e -> timTaiKhoan(txtTimNhanh.getText().trim()));
        btnThongBao.setIcon(Icons.of(Icons.BELL, 18, Theme.TEXT));
        btnThongBao.addActionListener(e -> moTrang(TONG_QUAN));
        btnTaiKhoan.setText(tenDangNhap);
        btnTaiKhoan.setIcon(UI.avatar(UI.vietTat(tenDangNhap), 30, Theme.SIDEBAR));
        JPopupMenu popup = new JPopupMenu();
        JMenuItem settings = new JMenuItem("Thông tin tài khoản / Đổi mật khẩu");
        settings.addActionListener(e -> moTrang(CAI_DAT)); popup.add(settings);
        JMenuItem logout = new JMenuItem("Đăng xuất");
        logout.addActionListener(e -> dangXuat()); popup.add(logout);
        btnTaiKhoan.addActionListener(e -> popup.show(btnTaiKhoan, 0, btnTaiKhoan.getHeight()));
        btnDangXuat.addActionListener(e -> dangXuat());
        moTrang(TONG_QUAN);
    }

    private void trangTriChanMenu() {
        Color nen = sidebar.getBackground();
        pnlChanMenu.removeAll();
        pnlChanMenu.setLayout(new BorderLayout(0, 16));
        pnlChanMenu.setBackground(nen);
        pnlChanMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x224568)),
                new EmptyBorder(18, 16, 18, 16)));

        JPanel thongTin = new JPanel(new BorderLayout(10, 0));
        thongTin.setOpaque(false);
        thongTin.add(new JLabel(Icons.of(Icons.CAP, 28, new Color(0xB8D4F5))), BorderLayout.WEST);
        JPanel chu = new JPanel();
        chu.setOpaque(false);
        chu.setLayout(new BoxLayout(chu, BoxLayout.Y_AXIS));
        chu.add(UI.nhan("Đại học Điện lực", Theme.font(Font.BOLD, 13), Color.WHITE));
        chu.add(Box.createVerticalStrut(5));
        chu.add(UI.nhan("Kết nối tri thức", Theme.font(Font.PLAIN, 11), new Color(0xAAC0DA)));
        chu.add(Box.createVerticalStrut(2));
        chu.add(UI.nhan("Kiến tạo tương lai", Theme.font(Font.PLAIN, 11), new Color(0xAAC0DA)));
        thongTin.add(chu, BorderLayout.CENTER);
        pnlChanMenu.add(thongTin, BorderLayout.CENTER);

        btnDangXuat = new JButton("Đăng xuất", Icons.of(Icons.LOGOUT, 18, new Color(0xE4EDF9))) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.muot(g);
                g2.setColor(getModel().isPressed() ? new Color(0x244F7D)
                        : getModel().isRollover() ? new Color(0x20456E) : new Color(0x16385D));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(new Color(0x365777));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnDangXuat.setFont(Theme.BODY_BOLD);
        btnDangXuat.setForeground(new Color(0xE4EDF9));
        btnDangXuat.setIconTextGap(10);
        btnDangXuat.setContentAreaFilled(false);
        btnDangXuat.setOpaque(false);
        btnDangXuat.setBorder(new EmptyBorder(11, 16, 11, 16));
        btnDangXuat.setFocusPainted(false);
        btnDangXuat.setRolloverEnabled(true);
        btnDangXuat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDangXuat.setPreferredSize(new Dimension(218, 42));
        pnlChanMenu.add(btnDangXuat, BorderLayout.SOUTH);
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
        if (!cacTrang.containsKey(ma)) return;
        cacMuc.forEach((k, m) -> {
            m.setBackground(new Color(k.equals(ma) ? 0x1565C0 : 0x0B2B50));
            m.setForeground(Color.WHITE);
        });
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
        if (dangTaiChuong) return;
        dangTaiChuong = true;
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
                    lblSoViecCanXuLy.setText(so <= 0 ? "" : (so > 9 ? "9+" : String.valueOf(so)));
                } catch (Exception ignored) {
                    lblSoViecCanXuLy.setText("");
                }
                dangTaiChuong = false;
                lblSoViecCanXuLy.repaint();
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

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel sidebar;
    private javax.swing.JButton btnTongQuan;
    private javax.swing.JButton btnQuanLySuKien;
    private javax.swing.JButton btnQuanLyThongBao;
    private javax.swing.JButton btnQuanLyTaiKhoan;
    private javax.swing.JButton btnPhanQuyen;
    private javax.swing.JButton btnBaoCao;
    private javax.swing.JButton btnCaiDat;
    private javax.swing.JButton btnCongViec;
    private javax.swing.JButton btnDangXuat;
    private javax.swing.JButton btnThuGonMenu;
    private javax.swing.JTextField txtTimNhanh;
    private javax.swing.JButton btnThongBao;
    private javax.swing.JLabel lblSoViecCanXuLy;
    private javax.swing.JButton btnTaiKhoan;
    private javax.swing.JPanel noiDung;

    private javax.swing.JPanel pnlBenPhai;
    private javax.swing.JPanel pnlThanhTren;
    private javax.swing.JPanel pnlLogo;
    private javax.swing.JPanel pnlChanMenu;

    private void createUIComponents() {
        txtTimNhanh = UI.oTimKiem("T\u00ecm t\u00e0i kho\u1ea3n...");
    }

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 2, new java.awt.Insets(0, 0, 0, 0), 0, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.setPreferredSize(new java.awt.Dimension(1366, 768));
        sidebar = new javax.swing.JPanel();
        sidebar.setLayout(new java.awt.BorderLayout(12, 16));
        sidebar.setBackground(new java.awt.Color(0x0B2B50));
        sidebar.setPreferredSize(new java.awt.Dimension(250, 650));
        mainPanel.add(sidebar, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 0, 7, null, null, null));
        pnlLogo = new javax.swing.JPanel();
        pnlLogo.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(12, 12, 12, 12), 12, 10, false, false));
        pnlLogo.setBackground(new java.awt.Color(0x0B2B50));
        sidebar.add(pnlLogo, java.awt.BorderLayout.NORTH);
        javax.swing.JLabel panela250 = new javax.swing.JLabel();
        panela250.setFont(new java.awt.Font("Segoe UI", 1, 12));
        panela250.setForeground(new java.awt.Color(0xFFFFFF));
        panela250.setText("HỆ THỐNG QUẢN LÝ SỰ KIỆN");
        pnlLogo.add(panela250, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela251 = new javax.swing.JLabel();
        panela251.setFont(new java.awt.Font("Segoe UI", 0, 10));
        panela251.setForeground(new java.awt.Color(0xD7E5F4));
        panela251.setText("BCH LCĐ CNS – ĐH ĐIỆN LỰC");
        pnlLogo.add(panela251, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JPanel panela258 = new javax.swing.JPanel();
        panela258.setLayout(new java.awt.BorderLayout(12, 16));
        panela258.setBackground(new java.awt.Color(0x0B2B50));
        sidebar.add(panela258, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela253 = new javax.swing.JPanel();
        panela253.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(8, 1, new java.awt.Insets(10, 10, 10, 10), 12, 10, false, false));
        panela253.setBackground(new java.awt.Color(0x0B2B50));
        panela258.add(panela253, java.awt.BorderLayout.NORTH);
        btnTongQuan = new javax.swing.JButton();
        btnTongQuan.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnTongQuan.setBackground(new java.awt.Color(0x0B2B50));
        btnTongQuan.setForeground(new java.awt.Color(0xD7E5F4));
        btnTongQuan.setFocusPainted(false);
        btnTongQuan.setContentAreaFilled(false);
        btnTongQuan.setOpaque(true);
        btnTongQuan.setBorderPainted(false);
        btnTongQuan.setPreferredSize(new java.awt.Dimension(220, 40));
        btnTongQuan.setHorizontalAlignment(2);
        btnTongQuan.setText("Tổng quan");
        panela253.add(btnTongQuan, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        btnQuanLySuKien = new javax.swing.JButton();
        btnQuanLySuKien.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnQuanLySuKien.setBackground(new java.awt.Color(0x0B2B50));
        btnQuanLySuKien.setForeground(new java.awt.Color(0xD7E5F4));
        btnQuanLySuKien.setFocusPainted(false);
        btnQuanLySuKien.setContentAreaFilled(false);
        btnQuanLySuKien.setOpaque(true);
        btnQuanLySuKien.setBorderPainted(false);
        btnQuanLySuKien.setPreferredSize(new java.awt.Dimension(220, 40));
        btnQuanLySuKien.setHorizontalAlignment(2);
        btnQuanLySuKien.setText("Quản lý sự kiện");
        panela253.add(btnQuanLySuKien, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        btnQuanLyThongBao = new javax.swing.JButton();
        btnQuanLyThongBao.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnQuanLyThongBao.setBackground(new java.awt.Color(0x0B2B50));
        btnQuanLyThongBao.setForeground(new java.awt.Color(0xD7E5F4));
        btnQuanLyThongBao.setFocusPainted(false);
        btnQuanLyThongBao.setContentAreaFilled(false);
        btnQuanLyThongBao.setOpaque(true);
        btnQuanLyThongBao.setBorderPainted(false);
        btnQuanLyThongBao.setPreferredSize(new java.awt.Dimension(220, 40));
        btnQuanLyThongBao.setHorizontalAlignment(2);
        btnQuanLyThongBao.setText("Quản lý thông báo");
        panela253.add(btnQuanLyThongBao, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        btnQuanLyTaiKhoan = new javax.swing.JButton();
        btnQuanLyTaiKhoan.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnQuanLyTaiKhoan.setBackground(new java.awt.Color(0x0B2B50));
        btnQuanLyTaiKhoan.setForeground(new java.awt.Color(0xD7E5F4));
        btnQuanLyTaiKhoan.setFocusPainted(false);
        btnQuanLyTaiKhoan.setContentAreaFilled(false);
        btnQuanLyTaiKhoan.setOpaque(true);
        btnQuanLyTaiKhoan.setBorderPainted(false);
        btnQuanLyTaiKhoan.setPreferredSize(new java.awt.Dimension(220, 40));
        btnQuanLyTaiKhoan.setHorizontalAlignment(2);
        btnQuanLyTaiKhoan.setText("Quản lý tài khoản");
        panela253.add(btnQuanLyTaiKhoan, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        btnPhanQuyen = new javax.swing.JButton();
        btnPhanQuyen.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnPhanQuyen.setBackground(new java.awt.Color(0x0B2B50));
        btnPhanQuyen.setForeground(new java.awt.Color(0xD7E5F4));
        btnPhanQuyen.setFocusPainted(false);
        btnPhanQuyen.setContentAreaFilled(false);
        btnPhanQuyen.setOpaque(true);
        btnPhanQuyen.setBorderPainted(false);
        btnPhanQuyen.setPreferredSize(new java.awt.Dimension(220, 40));
        btnPhanQuyen.setHorizontalAlignment(2);
        btnPhanQuyen.setText("Phân quyền người dùng");
        panela253.add(btnPhanQuyen, new com.intellij.uiDesigner.core.GridConstraints(4, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        btnBaoCao = new javax.swing.JButton();
        btnBaoCao.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnBaoCao.setBackground(new java.awt.Color(0x0B2B50));
        btnBaoCao.setForeground(new java.awt.Color(0xD7E5F4));
        btnBaoCao.setFocusPainted(false);
        btnBaoCao.setContentAreaFilled(false);
        btnBaoCao.setOpaque(true);
        btnBaoCao.setBorderPainted(false);
        btnBaoCao.setPreferredSize(new java.awt.Dimension(220, 40));
        btnBaoCao.setHorizontalAlignment(2);
        btnBaoCao.setText("Báo cáo");
        panela253.add(btnBaoCao, new com.intellij.uiDesigner.core.GridConstraints(5, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        btnCongViec = new javax.swing.JButton();
        btnCongViec.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnCongViec.setBackground(new java.awt.Color(0x0B2B50));
        btnCongViec.setForeground(new java.awt.Color(0xD7E5F4));
        btnCongViec.setFocusPainted(false);
        btnCongViec.setContentAreaFilled(false);
        btnCongViec.setOpaque(true);
        btnCongViec.setBorderPainted(false);
        btnCongViec.setPreferredSize(new java.awt.Dimension(220, 40));
        btnCongViec.setHorizontalAlignment(2);
        btnCongViec.setText("Giao công việc");
        panela253.add(btnCongViec, new com.intellij.uiDesigner.core.GridConstraints(6, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        btnCaiDat = new javax.swing.JButton();
        btnCaiDat.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnCaiDat.setBackground(new java.awt.Color(0x0B2B50));
        btnCaiDat.setForeground(new java.awt.Color(0xD7E5F4));
        btnCaiDat.setFocusPainted(false);
        btnCaiDat.setContentAreaFilled(false);
        btnCaiDat.setOpaque(true);
        btnCaiDat.setBorderPainted(false);
        btnCaiDat.setPreferredSize(new java.awt.Dimension(220, 40));
        btnCaiDat.setHorizontalAlignment(2);
        btnCaiDat.setText("Cài đặt");
        panela253.add(btnCaiDat, new com.intellij.uiDesigner.core.GridConstraints(7, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        pnlChanMenu = new javax.swing.JPanel();
        pnlChanMenu.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new java.awt.Insets(12, 12, 12, 12), 12, 10, false, false));
        pnlChanMenu.setBackground(new java.awt.Color(0x0B2B50));
        sidebar.add(pnlChanMenu, java.awt.BorderLayout.SOUTH);
        javax.swing.JLabel panela254 = new javax.swing.JLabel();
        panela254.setFont(new java.awt.Font("Segoe UI", 0, 12));
        panela254.setForeground(new java.awt.Color(0xFFFFFF));
        panela254.setText("Đại học Điện lực");
        pnlChanMenu.add(panela254, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela255 = new javax.swing.JLabel();
        panela255.setFont(new java.awt.Font("Segoe UI", 0, 10));
        panela255.setForeground(new java.awt.Color(0xD7E5F4));
        panela255.setText("Kết nối tri thức – Kiến tạo tương lai");
        pnlChanMenu.add(panela255, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        btnDangXuat = new javax.swing.JButton();
        btnDangXuat.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnDangXuat.setBackground(new java.awt.Color(0xFFFFFF));
        btnDangXuat.setForeground(new java.awt.Color(0x243B53));
        btnDangXuat.setFocusPainted(false);
        btnDangXuat.setPreferredSize(new java.awt.Dimension(130, 30));
        btnDangXuat.setText("Đăng xuất");
        pnlChanMenu.add(btnDangXuat, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        pnlBenPhai = new javax.swing.JPanel();
        pnlBenPhai.setLayout(new java.awt.BorderLayout(12, 16));
        pnlBenPhai.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlBenPhai, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 7, null, null, null));
        pnlThanhTren = new javax.swing.JPanel();
        pnlThanhTren.setLayout(new java.awt.BorderLayout(12, 16));
        pnlThanhTren.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlThanhTren.setBackground(new java.awt.Color(0xFFFFFF));
        pnlBenPhai.add(pnlThanhTren, java.awt.BorderLayout.NORTH);
        btnThuGonMenu = new javax.swing.JButton();
        btnThuGonMenu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnThuGonMenu.setToolTipText("Thu gọn hoặc mở rộng menu");
        btnThuGonMenu.setBackground(new java.awt.Color(0xFFFFFF));
        btnThuGonMenu.setForeground(new java.awt.Color(0x243B53));
        btnThuGonMenu.setFocusPainted(false);
        btnThuGonMenu.setPreferredSize(new java.awt.Dimension(36, 30));
        btnThuGonMenu.setText("");
        pnlThanhTren.add(btnThuGonMenu, java.awt.BorderLayout.WEST);
        javax.swing.JPanel panela266 = new javax.swing.JPanel();
        panela266.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 0));
        panela266.setBackground(new java.awt.Color(0xFFFFFF));
        pnlThanhTren.add(panela266, java.awt.BorderLayout.CENTER);
        txtTimNhanh.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTimNhanh.setToolTipText("Tìm tài khoản theo tên, email hoặc mã số rồi nhấn Enter");
        txtTimNhanh.setPreferredSize(new java.awt.Dimension(260, 32));
        txtTimNhanh.setMinimumSize(new java.awt.Dimension(50, 32));
        panela266.add(txtTimNhanh);
        javax.swing.JPanel panela265 = new javax.swing.JPanel();
        panela265.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela265.setBackground(new java.awt.Color(0xFFFFFF));
        panela266.add(panela265);
        btnThongBao = new javax.swing.JButton();
        btnThongBao.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnThongBao.setBackground(new java.awt.Color(0xFFFFFF));
        btnThongBao.setForeground(new java.awt.Color(0x243B53));
        btnThongBao.setFocusPainted(false);
        btnThongBao.setPreferredSize(new java.awt.Dimension(130, 30));
        btnThongBao.setText("Thông báo");
        panela265.add(btnThongBao, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 4, 0, 0, 0, null, null, null));
        lblSoViecCanXuLy = new javax.swing.JLabel();
        lblSoViecCanXuLy.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblSoViecCanXuLy.setForeground(new java.awt.Color(0xDC3545));
        lblSoViecCanXuLy.setText("");
        panela265.add(lblSoViecCanXuLy, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 3, 7, 3, null, null, null));
        btnTaiKhoan = new javax.swing.JButton();
        btnTaiKhoan.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnTaiKhoan.setToolTipText("Thông tin tài khoản / Đăng xuất");
        btnTaiKhoan.setBackground(new java.awt.Color(0xFFFFFF));
        btnTaiKhoan.setForeground(new java.awt.Color(0x243B53));
        btnTaiKhoan.setFocusPainted(false);
        btnTaiKhoan.setPreferredSize(new java.awt.Dimension(130, 30));
        btnTaiKhoan.setText("");
        panela265.add(btnTaiKhoan, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, 4, 0, 0, 0, null, null, null));
        noiDung = new javax.swing.JPanel();
        noiDung.setLayout(new java.awt.CardLayout());
        noiDung.setBackground(new java.awt.Color(0xF5F7FA));
        pnlBenPhai.add(noiDung, java.awt.BorderLayout.CENTER);
    }
}
