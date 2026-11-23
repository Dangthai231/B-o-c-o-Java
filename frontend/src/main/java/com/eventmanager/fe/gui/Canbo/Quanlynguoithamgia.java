package com.eventmanager.fe.gui.Canbo;

import com.eventmanager.fe.gui.Admin.common.Api;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý người tham gia, liên kết với Sinh viên:
 * danh sách lấy từ lượt đăng ký thật của sinh viên (bảng dang_ky_su_kien) theo từng sự kiện;
 * "Lưu điểm danh" ghi vào DB nên sinh viên thấy trạng thái Đã tham gia / Vắng mặt ở "Sự kiện của tôi".
 */
public class Quanlynguoithamgia {
    private JPanel mainPanel;
    private JTextField txtTimkiem;
    private JButton btnTimKiem;
    private JButton btnHuyTimKiem;
    private JLabel lblTongSo;
    private JLabel lblVangMat;
    private JLabel lblChuaDiemDanh;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JButton btnXemChiTiet;
    private JButton btnCapNhatDiemDanh;
    private JButton btnLamMoi;
    private JComboBox<SuKienItem> cbSuKien;

    // Dữ liệu từng dòng (cùng thứ tự tableModel) và giá trị điểm danh lúc tải, để biết dòng nào đã sửa
    private final List<JsonObject> dsDangKy = new ArrayList<>();
    private final List<String> diemDanhGoc = new ArrayList<>();

    private static final String TAT_CA = "Tất cả sự kiện";

    private record SuKienItem(long id, String ten) {
        @Override
        public String toString() {
            return ten;
        }
    }

    public Quanlynguoithamgia() {
        initUI();
        loadData();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // 1. Top Panel: Header Title + Search Bar
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("QUẢN LÝ NGƯỜI THAM GIA SỰ KIỆN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 30, 30));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        topContainer.add(lblTitle);
        topContainer.add(Box.createVerticalStrut(12));

        // Search & Stats Row
        JPanel searchRow = new JPanel(new BorderLayout(15, 0));
        searchRow.setBackground(Color.WHITE);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel leftSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftSearch.setBackground(Color.WHITE);

        // Chọn sự kiện để xem / điểm danh sinh viên đã đăng ký sự kiện đó
        JLabel lblSuKien = new JLabel("Sự kiện");
        lblSuKien.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftSearch.add(lblSuKien);
        cbSuKien = new JComboBox<>(new SuKienItem[]{new SuKienItem(0, TAT_CA)});
        cbSuKien.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbSuKien.setBackground(Color.WHITE);
        cbSuKien.setPreferredSize(new Dimension(240, 28));
        cbSuKien.addActionListener(e -> loadData());
        leftSearch.add(cbSuKien);

        JLabel lblSearch = new JLabel("Tìm kiếm");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        leftSearch.add(lblSearch);

        txtTimkiem = new JTextField(20);
        txtTimkiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimkiem.setBorder(new CompoundBorder(new LineBorder(new Color(180, 180, 180)), new EmptyBorder(4, 6, 4, 6)));
        leftSearch.add(txtTimkiem);

        btnTimKiem = createButton("Tìm kiếm");
        btnHuyTimKiem = createButton("Hủy tìm kiếm");
        leftSearch.add(btnTimKiem);
        leftSearch.add(btnHuyTimKiem);

        searchRow.add(leftSearch, BorderLayout.WEST);

        // Stats labels on the right
        JPanel rightStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4));
        rightStats.setBackground(Color.WHITE);

        lblTongSo = new JLabel("Tổng: 0");
        lblTongSo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rightStats.add(lblTongSo);

        lblVangMat = new JLabel("Đã điểm danh Vắng: 0");
        lblVangMat.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rightStats.add(lblVangMat);

        lblChuaDiemDanh = new JLabel("Chưa điểm danh: 0");
        lblChuaDiemDanh.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rightStats.add(lblChuaDiemDanh);

        searchRow.add(rightStats, BorderLayout.EAST);

        topContainer.add(searchRow);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // 2. Center Panel: Table with GroupBox Title
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Danh sách người tham gia",
                0, 0,
                new Font("Segoe UI", Font.PLAIN, 13)
        ));

        String[] columns = {"Mã SV", "Họ tên", "Email", "Số điện thoại", "Sự kiện", "Điểm danh", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Chỉ sửa cột Điểm danh; sinh viên đã hủy đăng ký thì không điểm danh
                return col == 5 && !"Đã hủy".equals(getValueAt(row, 6));
            }
        };

        table = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(220, 220, 220));
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(25, 118, 210));
        table.setSelectionForeground(Color.WHITE);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));

        table.getColumnModel().getColumn(0).setPreferredWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);

        // Custom badge renderer for "Điểm danh" (Column 5)
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String text = (value != null) ? value.toString() : "";

                if (!isSelected) {
                    if (text.contains("Đã điểm danh")) {
                        c.setBackground(new Color(232, 245, 233)); // light green
                        c.setForeground(new Color(46, 125, 50));   // dark green
                    } else if (text.contains("Vắng")) {
                        c.setBackground(new Color(255, 235, 238)); // light red
                        c.setForeground(new Color(198, 40, 40));   // dark red
                    } else if (text.contains("Chưa")) {
                        c.setBackground(new Color(255, 248, 225)); // light amber
                        c.setForeground(new Color(230, 81, 0));    // dark orange
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                }
                setText(text + "  ▼");
                return c;
            }
        });

        // Custom renderer for "Trạng thái" (Column 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String text = (value != null) ? value.toString() : "";
                if (!isSelected) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(new Color(40, 40, 40));
                }
                setText(text);
                return c;
            }
        });

        // Điểm danh chọn bằng hộp xổ xuống; Trạng thái (đã xác nhận / đã hủy) do sinh viên đăng ký / hủy
        JComboBox<String> cbDiemDanh = new JComboBox<>(new String[]{"Đã điểm danh", "Vắng", "Chưa điểm danh"});
        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(cbDiemDanh));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        bottomPanel.setBackground(Color.WHITE);

        btnXemChiTiet = createButton("Xem chi tiết");
        btnCapNhatDiemDanh = createButton("Lưu điểm danh");
        btnLamMoi = createButton("Làm mới");

        bottomPanel.add(btnXemChiTiet);
        bottomPanel.add(btnCapNhatDiemDanh);
        bottomPanel.add(btnLamMoi);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Events
        btnTimKiem.addActionListener(e -> {
            String query = txtTimkiem.getText().trim();
            if (query.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
            }
            updateStats();
        });

        btnHuyTimKiem.addActionListener(e -> {
            txtTimkiem.setText("");
            sorter.setRowFilter(null);
            updateStats();
        });

        btnXemChiTiet.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int modelRow = table.convertRowIndexToModel(row);
                JsonObject dk = dsDangKy.get(modelRow);
                String msg = String.format("Mã SV: %s\nHọ tên: %s\nTài khoản: %s\nLớp: %s\nEmail: %s\nSố điện thoại: %s\n\nSự kiện: %s\nNgày đăng ký: %s\nĐiểm danh: %s\nTrạng thái: %s",
                        Api.s(dk, "maSinhVien"), Api.s(dk, "hoTen"), Api.s(dk, "tenDangNhap"), Api.s(dk, "lop"),
                        Api.s(dk, "email"), Api.s(dk, "soDienThoai"), Api.s(dk, "tenSuKien"),
                        Api.s(dk, "ngayDangKy").replace('T', ' '),
                        tableModel.getValueAt(modelRow, 5), tableModel.getValueAt(modelRow, 6));
                JOptionPane.showMessageDialog(mainPanel, msg, "Chi tiết người tham gia", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Vui lòng chọn một người tham gia trong danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Lưu các dòng đã đổi điểm danh: PUT /api/dang-ky/diem-danh/{id}
        btnCapNhatDiemDanh.addActionListener(e -> {
            if (table.isEditing()) {
                table.getCellEditor().stopCellEditing();
            }
            List<long[]> canLuu = new ArrayList<>();   // {id, chỉ số dòng}
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (!String.valueOf(tableModel.getValueAt(i, 5)).equals(diemDanhGoc.get(i))) {
                    canLuu.add(new long[]{Api.n(dsDangKy.get(i), "id"), i});
                }
            }
            if (canLuu.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "Chưa có thay đổi điểm danh nào để lưu.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            List<String> giaTri = new ArrayList<>();
            for (long[] r : canLuu) giaTri.add(sangTrangThaiDangKy(String.valueOf(tableModel.getValueAt((int) r[1], 5))));
            Api.chayNen(mainPanel, () -> {
                for (int i = 0; i < canLuu.size(); i++) {
                    JsonObject body = new JsonObject();
                    body.addProperty("trangThai", giaTri.get(i));
                    Api.put("/api/dang-ky/diem-danh/" + canLuu.get(i)[0], body);
                }
                return canLuu.size();
            }, n -> {
                JOptionPane.showMessageDialog(mainPanel, "Đã lưu điểm danh cho " + n + " sinh viên.\nSinh viên sẽ thấy trạng thái mới ở mục Sự kiện của tôi.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            });
        });

        btnLamMoi.addActionListener(e -> {
            loadData();
            txtTimkiem.setText("");
            sorter.setRowFilter(null);
        });
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(30, 30, 30));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(190, 190, 190), 1),
                new EmptyBorder(6, 20, 6, 20)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void updateStats() {
        int total = tableModel.getRowCount();
        int vang = 0;
        int chua = 0;
        for (int i = 0; i < total; i++) {
            String dd = (String) tableModel.getValueAt(i, 5);
            if (dd != null) {
                if (dd.contains("Vắng")) vang++;
                else if (dd.contains("Chưa")) chua++;
            }
        }
        lblTongSo.setText("Tổng: " + total);
        lblVangMat.setText("Đã điểm danh Vắng: " + vang);
        lblChuaDiemDanh.setText("Chưa điểm danh: " + chua);
    }

    /**
     * Tải danh sách sự kiện (lần đầu) và sinh viên đăng ký: GET /api/dang-ky/theo-su-kien[?suKienId=].
     * Trạng thái đăng ký của sinh viên được hiển thị theo ngôn ngữ điểm danh của cán bộ.
     */
    private void loadData() {
        SuKienItem chon = cbSuKien == null ? null : (SuKienItem) cbSuKien.getSelectedItem();
        boolean canTaiSuKien = cbSuKien != null && cbSuKien.getItemCount() <= 1;
        String url = "/api/dang-ky/theo-su-kien" + (chon == null || chon.id() == 0 ? "" : "?suKienId=" + chon.id());

        Api.chayNen(mainPanel, () -> {
            JsonObject kq = new JsonObject();
            kq.add("dangKy", Api.get(url));
            if (canTaiSuKien) kq.add("suKien", Api.get("/api/su-kien/danh-sach"));
            return kq;
        }, kq -> {
            if (canTaiSuKien) {
                for (JsonElement el : Api.arr(kq, "suKien")) {
                    JsonObject sk = el.getAsJsonObject();
                    cbSuKien.addItem(new SuKienItem(Api.n(sk, "id"), Api.s(sk, "tenSuKien")));
                }
            }
            tableModel.setRowCount(0);
            dsDangKy.clear();
            diemDanhGoc.clear();
            for (JsonElement el : Api.arr(kq, "dangKy")) {
                JsonObject dk = el.getAsJsonObject();
                String tt = Api.s(dk, "trangThai");
                String diemDanh = sangDiemDanh(tt);
                dsDangKy.add(dk);
                diemDanhGoc.add(diemDanh);
                tableModel.addRow(new Object[]{Api.s(dk, "maSinhVien"), Api.s(dk, "hoTen"), Api.s(dk, "email"),
                        Api.s(dk, "soDienThoai"), Api.s(dk, "tenSuKien"), diemDanh,
                        "Đã hủy".equals(tt) ? "Đã hủy" : "Đã xác nhận"});
            }
            updateStats();
        });
    }

    // Trạng thái đăng ký (DB) <-> chữ điểm danh trên màn hình cán bộ
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

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void $$$setupUI$$$() {
    }
}
