package com.eventmanager.fe.gui.Canbo;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý người tham gia — REDESIGN:
 * Bên trái hiện danh sách sự kiện dạng card, click vào sự kiện → bên phải hiện bảng người tham gia.
 * Cán bộ có thể điểm danh, xem chi tiết từng sự kiện.
 */
public class Quanlynguoithamgia {
    private JPanel mainPanel;

    // ===== Left panel: danh sách sự kiện =====
    private JPanel pnlDanhSachSK;
    private final List<SuKienInfo> dsSuKien = new ArrayList<>();
    private int suKienDangChon = -1; // index trong dsSuKien

    // ===== Right panel: bảng người tham gia =====
    private BangDep bang;
    private TableRowSorter<?> sorter;
    private UI.TextFieldDep txtTimKiem;
    private JLabel lblTieuDePhải;
    private JLabel lblThongKe;
    private JPanel pnlNoContent;
    private JPanel pnlContent;

    // Dữ liệu người tham gia của sự kiện đang chọn
    private final List<JsonObject> dsDangKy = new ArrayList<>();
    private final List<String> diemDanhGoc = new ArrayList<>();

    // ===== Stat cards =====
    private UI.StatCard cardTongSK, cardTongNTG, cardDaDiemDanh, cardChuaDiemDanh;

    private record SuKienInfo(long id, String ten, String thoiGian, String diaDiem, int soNguoi) {}

    public Quanlynguoithamgia() {
        initUI();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Theme.BG);

        // ==================== TOP: HEADER + STAT CARDS ====================
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(24, 28, 0, 28));

        // Tiêu đề trang
        JPanel headerRow = UI.tieuDeTrang("Quản lý người tham gia", "Điểm danh và quản lý sinh viên theo từng sự kiện");
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(headerRow);
        topPanel.add(Box.createVerticalStrut(20));

        // 4 Stat cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        cardTongSK = new UI.StatCard("Tổng sự kiện", Icons.CALENDAR, Theme.PRIMARY, Theme.PRIMARY_SOFT);
        cardTongNTG = new UI.StatCard("Tổng người tham gia", Icons.USERS, Theme.SUCCESS, Theme.SUCCESS_SOFT);
        cardDaDiemDanh = new UI.StatCard("Đã điểm danh", Icons.CHECK_CIRCLE, new Color(0x0E7C86), new Color(0xE1F4F5));
        cardChuaDiemDanh = new UI.StatCard("Chưa điểm danh", Icons.CLOCK, Theme.WARNING, Theme.WARNING_SOFT);

        statsRow.add(cardTongSK);
        statsRow.add(cardTongNTG);
        statsRow.add(cardDaDiemDanh);
        statsRow.add(cardChuaDiemDanh);
        topPanel.add(statsRow);
        topPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ==================== CENTER: LEFT (sự kiện) + RIGHT (bảng) ====================
        JPanel centerPanel = new JPanel(new BorderLayout(16, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 28, 20, 28));

        // ----- LEFT: Danh sách sự kiện -----
        JPanel leftPanel = UI.card(new BorderLayout(0, 12), 16);
        leftPanel.setPreferredSize(new Dimension(290, 0));
        leftPanel.setMinimumSize(new Dimension(290, 0));

        JLabel lblDSSK = UI.nhan("Danh sách sự kiện", Theme.H2, Theme.TEXT);
        leftPanel.add(lblDSSK, BorderLayout.NORTH);

        pnlDanhSachSK = new JPanel();
        pnlDanhSachSK.setLayout(new BoxLayout(pnlDanhSachSK, BoxLayout.Y_AXIS));
        pnlDanhSachSK.setOpaque(false);

        JScrollPane spLeft = UI.cuon(pnlDanhSachSK);
        leftPanel.add(spLeft, BorderLayout.CENTER);

        centerPanel.add(leftPanel, BorderLayout.WEST);

        // ----- RIGHT: Bảng người tham gia -----
        JPanel rightPanel = UI.card(new BorderLayout(0, 12), 16);

        // Header phải: tiêu đề + tìm kiếm + nút
        JPanel rightHeader = new JPanel(new BorderLayout(12, 0));
        rightHeader.setOpaque(false);

        JPanel rightTitleBlock = new JPanel();
        rightTitleBlock.setOpaque(false);
        rightTitleBlock.setLayout(new BoxLayout(rightTitleBlock, BoxLayout.Y_AXIS));
        lblTieuDePhải = UI.nhan("Chọn một sự kiện", Theme.H2, Theme.TEXT);
        lblThongKe = UI.nhan("", Theme.SMALL, Theme.MUTED);
        rightTitleBlock.add(lblTieuDePhải);
        rightTitleBlock.add(Box.createVerticalStrut(2));
        rightTitleBlock.add(lblThongKe);
        rightHeader.add(rightTitleBlock, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightActions.setOpaque(false);

        txtTimKiem = UI.oTimKiem("Tìm theo tên, mã SV...");
        txtTimKiem.setPreferredSize(new Dimension(200, 36));
        txtTimKiem.addActionListener(e -> timKiem());
        rightActions.add(txtTimKiem);

        UI.FlatButton btnLamMoi = UI.nut("Làm mới", Icons.REFRESH, UI.Kieu.VIEN);
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            if (suKienDangChon >= 0) loadNguoiThamGia(dsSuKien.get(suKienDangChon));
        });
        rightActions.add(btnLamMoi);

        rightHeader.add(rightActions, BorderLayout.EAST);
        rightPanel.add(rightHeader, BorderLayout.NORTH);

        // Bảng người tham gia
        bang = new BangDep("ID", "Mã SV", "Họ tên", "Email", "Số điện thoại", "Điểm danh", "Trạng thái", "Thao tác");
        bang.anCot(0); // ẩn cột ID
        bang.doRongCot(1, 90);
        bang.doRongCot(2, 150);
        bang.doRongCot(3, 180);
        bang.doRongCot(4, 110);
        bang.doRongCot(5, 130);
        bang.doRongCot(6, 100);
        bang.coDinhCot(7, 160);

        // Renderer cho cột Điểm danh (5)
        bang.getColumnModel().getColumn(5).setCellRenderer((t, v, s, f, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 11));
            p.setBackground(bang.nenDong(r, s));
            String text = v == null ? "" : v.toString();
            Color[] mau = mauDiemDanh(text);
            UI.Badge b = new UI.Badge() {
                @Override
                protected void paintComponent(Graphics g) {
                    if (text.isEmpty()) return;
                    Graphics2D g2 = Theme.muot(g);
                    Dimension d = getPreferredSize();
                    int y2 = (getHeight() - d.height) / 2;
                    g2.setColor(mau[1]);
                    g2.fill(new RoundRectangle2D.Double(0, y2, d.width, d.height, d.height, d.height));
                    int x2 = 10;
                    // Chấm tròn
                    g2.setColor(mau[0]);
                    g2.fillOval(x2, y2 + d.height / 2 - 3, 6, 6);
                    x2 += 12;
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(text, x2, y2 + (d.height - fm.getHeight()) / 2 + fm.getAscent());
                    g2.dispose();
                }
            };
            b.setText(text);
            p.add(b);
            return p;
        });

        // Renderer cho cột Trạng thái (6)
        bang.cotNhan(6);

        // Cột thao tác (7)
        bang.cotThaoTac(7, List.of(
                new BangDep.NutThaoTac("xem", null, Icons.EYE, "Xem chi tiết"),
                new BangDep.NutThaoTac("diemDanh", null, Icons.CHECK, "Đổi điểm danh")
        ), null, (ma, dong) -> {
            if ("xem".equals(ma)) xemChiTiet(dong);
            else if ("diemDanh".equals(ma)) doiDiemDanh(dong);
        });

        pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);
        pnlContent.add(bang.trongKhungCuon(), BorderLayout.CENTER);

        // Bottom: nút Lưu điểm danh
        JPanel bottomRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottomRight.setOpaque(false);
        UI.FlatButton btnLuuDiemDanh = UI.nut("Lưu điểm danh", Icons.CHECK_CIRCLE, UI.Kieu.CHINH);
        btnLuuDiemDanh.addActionListener(e -> luuDiemDanh());
        bottomRight.add(btnLuuDiemDanh);
        pnlContent.add(bottomRight, BorderLayout.SOUTH);

        // Panel khi chưa chọn sự kiện
        pnlNoContent = new JPanel(new GridBagLayout());
        pnlNoContent.setOpaque(false);
        JPanel emptyState = new JPanel();
        emptyState.setOpaque(false);
        emptyState.setLayout(new BoxLayout(emptyState, BoxLayout.Y_AXIS));

        JLabel iconLabel = new JLabel(Icons.of(Icons.USERS, 48, Theme.BORDER));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyState.add(iconLabel);
        emptyState.add(Box.createVerticalStrut(12));

        JLabel emptyText = UI.nhan("Chọn một sự kiện ở bên trái", Theme.BODY, Theme.MUTED);
        emptyText.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyState.add(emptyText);

        JLabel emptyText2 = UI.nhan("để xem danh sách người tham gia", Theme.SMALL, Theme.MUTED);
        emptyText2.setAlignmentX(Component.CENTER_ALIGNMENT);
        emptyState.add(emptyText2);

        pnlNoContent.add(emptyState);

        // CardLayout để chuyển giữa noContent và content
        JPanel cardPanel = new JPanel(new CardLayout());
        cardPanel.setOpaque(false);
        cardPanel.add(pnlNoContent, "empty");
        cardPanel.add(pnlContent, "data");
        rightPanel.add(cardPanel, BorderLayout.CENTER);

        centerPanel.add(rightPanel, BorderLayout.CENTER);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    // ==================== Load data ====================

    /** Được gọi khi cán bộ chuyển sang trang này (từ DashboardCanbo). */
    public void taiLai() {
        loadDanhSachSuKien();
    }

    private void loadDanhSachSuKien() {
        Api.chayNen(mainPanel, () -> {
            JsonObject kq = new JsonObject();
            kq.add("suKien", Api.get("/api/su-kien/danh-sach"));
            kq.add("tatCa", Api.get("/api/dang-ky/theo-su-kien"));
            return kq;
        }, kq -> {
            dsSuKien.clear();
            suKienDangChon = -1;

            // Đếm số người theo từng sự kiện
            java.util.Map<Long, Integer> demNguoi = new java.util.HashMap<>();
            JsonArray tatCa = Api.arr(kq, "tatCa");
            int tongNTG = 0, tongDaDiem = 0, tongChuaDiem = 0;

            for (JsonElement el : tatCa) {
                JsonObject dk = el.getAsJsonObject();
                long skId = Api.n(dk, "suKienId");
                demNguoi.merge(skId, 1, Integer::sum);
                tongNTG++;
                String tt = Api.s(dk, "trangThai");
                if ("Đã tham gia".equals(tt)) tongDaDiem++;
                else if (!"Đã hủy".equals(tt) && !"Vắng mặt".equals(tt)) tongChuaDiem++;
            }

            for (JsonElement el : Api.arr(kq, "suKien")) {
                JsonObject sk = el.getAsJsonObject();
                long id = Api.n(sk, "id");
                String ten = Api.s(sk, "tenSuKien");
                String thoiGian = Api.s(sk, "thoiGianBatDau");
                if (thoiGian.contains("T")) thoiGian = thoiGian.replace('T', ' ').substring(0, Math.min(16, thoiGian.length()));
                String diaDiem = Api.s(sk, "diaDiem");
                int soNguoi = demNguoi.getOrDefault(id, 0);
                dsSuKien.add(new SuKienInfo(id, ten, thoiGian, diaDiem, soNguoi));
            }

            // Cập nhật stats
            cardTongSK.setSo(dsSuKien.size());
            cardTongNTG.setSo(tongNTG);
            cardDaDiemDanh.setSo(tongDaDiem);
            cardChuaDiemDanh.setSo(tongChuaDiem);

            // Render danh sách sự kiện
            renderDanhSachSK();

            // Hiện empty state
            showPanel("empty");
            lblTieuDePhải.setText("Chọn một sự kiện");
            lblThongKe.setText("");
        });
    }

    private void renderDanhSachSK() {
        pnlDanhSachSK.removeAll();
        for (int i = 0; i < dsSuKien.size(); i++) {
            SuKienInfo sk = dsSuKien.get(i);
            final int idx = i;
            JPanel card = taoCardSuKien(sk, i == suKienDangChon);
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    chonSuKien(idx);
                }
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (idx != suKienDangChon)
                        card.setBackground(Theme.ROW_HOVER);
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (idx != suKienDangChon)
                        card.setBackground(Theme.CARD);
                }
            });
            pnlDanhSachSK.add(card);
            pnlDanhSachSK.add(Box.createVerticalStrut(8));
        }
        pnlDanhSachSK.add(Box.createVerticalGlue());
        pnlDanhSachSK.revalidate();
        pnlDanhSachSK.repaint();
    }

    private JPanel taoCardSuKien(SuKienInfo sk, boolean dangChon) {
        JPanel card = new JPanel(new BorderLayout(8, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.muot(g);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                if (dangChon) {
                    g2.setColor(Theme.PRIMARY);
                    g2.setStroke(new BasicStroke(2f));
                    g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 3, getHeight() - 3, 12, 12));
                } else {
                    g2.setColor(Theme.BORDER);
                    g2.draw(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 12, 12));
                }
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBackground(dangChon ? Theme.PRIMARY_SOFT : Theme.CARD);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Trên: tên sự kiện + badge số người
        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);
        JLabel lblTen = UI.nhan(sk.ten(), Theme.BODY_BOLD, dangChon ? Theme.PRIMARY_DARK : Theme.TEXT);
        topRow.add(lblTen, BorderLayout.CENTER);

        // Badge số người
        JLabel lblSoNguoi = new JLabel(sk.soNguoi() + " người") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.muot(g);
                g2.setColor(dangChon ? Theme.PRIMARY : Theme.GRAY_SOFT);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSoNguoi.setFont(Theme.SMALL_BOLD);
        lblSoNguoi.setForeground(dangChon ? Color.WHITE : Theme.TEXT_2);
        lblSoNguoi.setHorizontalAlignment(SwingConstants.CENTER);
        lblSoNguoi.setBorder(new EmptyBorder(2, 10, 2, 10));
        lblSoNguoi.setOpaque(false);
        topRow.add(lblSoNguoi, BorderLayout.EAST);

        card.add(topRow, BorderLayout.NORTH);

        // Dưới: thời gian + địa điểm
        JPanel bottomRow = new JPanel();
        bottomRow.setOpaque(false);
        bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.Y_AXIS));

        if (sk.thoiGian() != null && !sk.thoiGian().isEmpty()) {
            JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            timeRow.setOpaque(false);
            timeRow.add(new JLabel(Icons.of(Icons.CLOCK, 12, Theme.MUTED)));
            timeRow.add(UI.nhan(sk.thoiGian(), Theme.SMALL, Theme.MUTED));
            bottomRow.add(timeRow);
        }
        if (sk.diaDiem() != null && !sk.diaDiem().isEmpty()) {
            JPanel placeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            placeRow.setOpaque(false);
            placeRow.add(new JLabel(Icons.of(Icons.PIN, 12, Theme.MUTED)));
            placeRow.add(UI.nhan(sk.diaDiem(), Theme.SMALL, Theme.MUTED));
            bottomRow.add(placeRow);
        }

        card.add(bottomRow, BorderLayout.CENTER);
        return card;
    }

    private void chonSuKien(int idx) {
        suKienDangChon = idx;
        renderDanhSachSK();
        SuKienInfo sk = dsSuKien.get(idx);
        lblTieuDePhải.setText(sk.ten());
        loadNguoiThamGia(sk);
    }

    private void loadNguoiThamGia(SuKienInfo sk) {
        Api.chayNen(mainPanel, () -> Api.get("/api/dang-ky/theo-su-kien?suKienId=" + sk.id()), arr -> {
            bang.xoaHet();
            dsDangKy.clear();
            diemDanhGoc.clear();

            int daDiem = 0, vang = 0, chua = 0;

            for (JsonElement el : arr.getAsJsonArray()) {
                JsonObject dk = el.getAsJsonObject();
                String tt = Api.s(dk, "trangThai");
                String diemDanh = sangDiemDanh(tt);
                String trangThai = "Đã hủy".equals(tt) ? "Đã hủy" : "Đã xác nhận";

                dsDangKy.add(dk);
                diemDanhGoc.add(diemDanh);

                bang.themDong(
                        Api.n(dk, "id"),
                        Api.s(dk, "maSinhVien"),
                        Api.s(dk, "hoTen"),
                        Api.s(dk, "email"),
                        Api.s(dk, "soDienThoai"),
                        diemDanh,
                        trangThai,
                        ""
                );

                if ("Đã điểm danh".equals(diemDanh)) daDiem++;
                else if ("Vắng".equals(diemDanh)) vang++;
                else chua++;
            }

            int total = dsDangKy.size();
            lblThongKe.setText(String.format("Tổng: %d  |  Đã điểm danh: %d  |  Vắng: %d  |  Chưa: %d", total, daDiem, vang, chua));

            showPanel("data");
            txtTimKiem.setText("");
        });
    }

    // ==================== Actions ====================

    private void timKiem() {
        String query = txtTimKiem.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            if (suKienDangChon >= 0) loadNguoiThamGia(dsSuKien.get(suKienDangChon));
            return;
        }
        // Filter trong model hiện tại
        for (int i = bang.getModelBang().getRowCount() - 1; i >= 0; i--) {
            String maSV = String.valueOf(bang.getModelBang().getValueAt(i, 1)).toLowerCase();
            String hoTen = String.valueOf(bang.getModelBang().getValueAt(i, 2)).toLowerCase();
            String email = String.valueOf(bang.getModelBang().getValueAt(i, 3)).toLowerCase();
            if (!maSV.contains(query) && !hoTen.contains(query) && !email.contains(query)) {
                // Nếu không dùng RowSorter, ta sẽ dùng cách khác: ẩn/hiện
                // Thay vì xóa, ta sẽ dùng RowFilter
            }
        }
        // Dùng RowSorter filter
        javax.swing.table.TableRowSorter<javax.swing.table.DefaultTableModel> rs =
                new javax.swing.table.TableRowSorter<>(bang.getModelBang());
        bang.setRowSorter(rs);
        rs.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
    }

    private void xemChiTiet(int modelRow) {
        if (modelRow < 0 || modelRow >= dsDangKy.size()) return;
        JsonObject dk = dsDangKy.get(modelRow);
        String diemDanh = String.valueOf(bang.getModelBang().getValueAt(modelRow, 5));
        String trangThai = String.valueOf(bang.getModelBang().getValueAt(modelRow, 6));

        HopThoai.FormDialog d = new HopThoai.FormDialog(mainPanel, "Chi tiết người tham gia",
                "Thông tin sinh viên đăng ký sự kiện", 480);
        d.truong("Mã sinh viên", HopThoai.oChiXem(Api.s(dk, "maSinhVien")), false);
        d.truong("Họ tên", HopThoai.oChiXem(Api.s(dk, "hoTen")), false);
        d.truong("Email", HopThoai.oChiXem(Api.s(dk, "email")), true);
        d.truong("Số điện thoại", HopThoai.oChiXem(Api.s(dk, "soDienThoai")), false);
        d.truong("Lớp", HopThoai.oChiXem(Api.s(dk, "lop")), false);
        String ngayDK = Api.s(dk, "ngayDangKy");
        if (ngayDK.contains("T")) ngayDK = ngayDK.replace('T', ' ');
        d.truong("Ngày đăng ký", HopThoai.oChiXem(ngayDK), true);
        d.truong("Điểm danh", HopThoai.oChiXem(diemDanh), false);
        d.truong("Trạng thái", HopThoai.oChiXem(trangThai), false);

        UI.FlatButton btnDong = UI.nut("Đóng", UI.Kieu.VIEN);
        btnDong.addActionListener(e -> d.dispose());
        d.nut(btnDong);
        d.hien();
    }

    private void doiDiemDanh(int modelRow) {
        if (modelRow < 0 || modelRow >= dsDangKy.size()) return;
        String trangThaiHienTai = String.valueOf(bang.getModelBang().getValueAt(modelRow, 6));
        if ("Đã hủy".equals(trangThaiHienTai)) {
            HopThoai.loi(mainPanel, "Sinh viên đã hủy đăng ký, không thể điểm danh.");
            return;
        }

        String[] options = {"Đã điểm danh", "Vắng", "Chưa điểm danh"};
        String current = String.valueOf(bang.getModelBang().getValueAt(modelRow, 5));

        HopThoai.FormDialog d = new HopThoai.FormDialog(mainPanel, "Đổi điểm danh",
                Api.s(dsDangKy.get(modelRow), "hoTen"), 380);
        JComboBox<String> cbDD = UI.hopChon(options, null);
        cbDD.setSelectedItem(current);
        d.truong("Trạng thái điểm danh", cbDD, true);

        UI.FlatButton btnHuy = UI.nut("Hủy", UI.Kieu.VIEN);
        btnHuy.addActionListener(e -> d.dispose());
        d.nut(btnHuy);

        UI.FlatButton btnLuu = UI.nut("Lưu", UI.Kieu.CHINH);
        btnLuu.addActionListener(e -> {
            String moi = (String) cbDD.getSelectedItem();
            bang.getModelBang().setValueAt(moi, modelRow, 5);
            d.dispose();
        });
        d.nut(btnLuu);
        d.hien();
    }

    private void luuDiemDanh() {
        List<long[]> canLuu = new ArrayList<>();
        for (int i = 0; i < bang.getModelBang().getRowCount(); i++) {
            String hienTai = String.valueOf(bang.getModelBang().getValueAt(i, 5));
            if (i < diemDanhGoc.size() && !hienTai.equals(diemDanhGoc.get(i))) {
                canLuu.add(new long[]{Api.n(dsDangKy.get(i), "id"), i});
            }
        }
        if (canLuu.isEmpty()) {
            HopThoai.loi(mainPanel, "Chưa có thay đổi điểm danh nào để lưu.");
            return;
        }
        if (!HopThoai.xacNhan(mainPanel, "Lưu điểm danh",
                "Bạn sắp cập nhật điểm danh cho <b>" + canLuu.size() + "</b> sinh viên. Tiếp tục?",
                "Lưu", UI.Kieu.CHINH)) {
            return;
        }

        List<String> giaTri = new ArrayList<>();
        for (long[] r : canLuu) {
            giaTri.add(sangTrangThaiDangKy(String.valueOf(bang.getModelBang().getValueAt((int) r[1], 5))));
        }

        Api.chayNen(mainPanel, () -> {
            for (int i = 0; i < canLuu.size(); i++) {
                JsonObject body = new JsonObject();
                body.addProperty("trangThai", giaTri.get(i));
                Api.put("/api/dang-ky/diem-danh/" + canLuu.get(i)[0], body);
            }
            return canLuu.size();
        }, n -> {
            HopThoai.thanhCong(mainPanel, "Đã lưu điểm danh cho " + n + " sinh viên");
            // Reload
            if (suKienDangChon >= 0) {
                loadDanhSachSuKien();
                // Re-chọn sự kiện đang xem sau khi reload xong
                SwingUtilities.invokeLater(() -> {
                    if (suKienDangChon >= 0 && suKienDangChon < dsSuKien.size()) {
                        // Sẽ được chọn lại bởi loadDanhSachSuKien ở callback
                    }
                });
            }
        });
    }

    // ==================== Utility ====================

    private void showPanel(String name) {
        Container parent = pnlNoContent.getParent();
        if (parent != null && parent.getLayout() instanceof CardLayout cl) {
            cl.show(parent, name);
        }
    }

    private static String sangDiemDanh(String trangThaiDangKy) {
        return switch (trangThaiDangKy) {
            case "Đã tham gia" -> "Đã điểm danh";
            case "Vắng mặt" -> "Vắng";
            default -> "Chưa điểm danh";
        };
    }

    private static String sangTrangThaiDangKy(String diemDanh) {
        return switch (diemDanh) {
            case "Đã điểm danh" -> "Đã tham gia";
            case "Vắng" -> "Vắng mặt";
            default -> "Đã đăng ký";
        };
    }

    private static Color[] mauDiemDanh(String s) {
        return switch (s) {
            case "Đã điểm danh" -> new Color[]{Theme.SUCCESS, Theme.SUCCESS_SOFT};
            case "Vắng" -> new Color[]{Theme.DANGER, Theme.DANGER_SOFT};
            default -> new Color[]{Theme.WARNING, Theme.WARNING_SOFT};
        };
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void $$$setupUI$$$() {
    }
}
