package com.eventmanager.fe.gui.Canbo;

import com.eventmanager.fe.gui.Admin.common.Api;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý thông báo của Cán bộ, liên kết với Admin và Sinh viên:
 * Cán bộ tạo thông báo -> lưu DB ở trạng thái "Chờ duyệt" -> Admin duyệt / đăng
 * -> Sinh viên thấy (nếu gửi cho "Tất cả người tham gia" hoặc "Sinh viên").
 * Trạng thái do Admin quyết định nên ô Trạng thái chỉ để xem.
 */
public class Quanlythongbao {
    private JPanel mainPanel;
    private JTextField txtTimKiem;
    private JButton btnTimKiem;
    private JTable tableThongBao;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtTieuDe;
    private JTextArea txtNoiDung;
    private JComboBox<String> cbDoiTuong;
    private JTextField txtNgayGui;
    private JComboBox<String> cbTrangThai;

    private JButton btnTaoThongBao;
    private JButton btnCapNhat;
    private JButton btnXoa;
    private JButton btnLamMoi;

    private final String tenDangNhap;
    // Dữ liệu đầy đủ của từng dòng trong bảng (cùng thứ tự với tableModel), giữ id và nội dung
    private final List<JsonObject> dsThongBao = new ArrayList<>();

    public Quanlythongbao() {
        this("canbo01");
    }

    public Quanlythongbao(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap == null || tenDangNhap.isBlank() ? "canbo01" : tenDangNhap;
        initUI();
        loadData();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Header
        JLabel lblTitle = new JLabel("QUẢN LÝ THÔNG BÁO SỰ KIỆN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(30, 30, 30));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Center: 2-Column Split
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 20, 0));
        contentGrid.setBackground(Color.WHITE);

        // ================= LEFT: DANH SÁCH THÔNG BÁO =================
        JPanel leftPanel = new JPanel(new BorderLayout(0, 10));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Danh sách thông báo",
                0, 0,
                new Font("Segoe UI", Font.PLAIN, 13)
        ));

        // Search row
        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        searchRow.setBackground(Color.WHITE);
        searchRow.add(new JLabel("Tìm kiếm:"));

        txtTimKiem = new JTextField(20);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTimKiem.setBorder(new CompoundBorder(new LineBorder(new Color(180, 180, 180)), new EmptyBorder(3, 6, 3, 6)));
        searchRow.add(txtTimKiem);

        btnTimKiem = createSmallButton("Tìm");
        searchRow.add(btnTimKiem);
        leftPanel.add(searchRow, BorderLayout.NORTH);

        // Table
        String[] columns = {"Mã", "Tiêu đề", "Đối tượng", "Ngày gửi", "Trạng thái"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tableThongBao = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        tableThongBao.setRowSorter(sorter);
        tableThongBao.setRowHeight(26);
        tableThongBao.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tableThongBao.setGridColor(new Color(220, 220, 220));
        tableThongBao.setShowGrid(true);
        tableThongBao.setSelectionBackground(new Color(25, 118, 210));
        tableThongBao.setSelectionForeground(Color.WHITE);

        JTableHeader header = tableThongBao.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(Color.BLACK);

        tableThongBao.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableThongBao.getColumnModel().getColumn(1).setPreferredWidth(120);
        tableThongBao.getColumnModel().getColumn(2).setPreferredWidth(90);
        tableThongBao.getColumnModel().getColumn(3).setPreferredWidth(90);
        tableThongBao.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scrollTable = new JScrollPane(tableThongBao);
        scrollTable.getViewport().setBackground(Color.WHITE);
        scrollTable.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        leftPanel.add(scrollTable, BorderLayout.CENTER);

        contentGrid.add(leftPanel);

        // ================= RIGHT: THÔNG TIN THÔNG BÁO =================
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "Thông tin thông báo",
                0, 0,
                new Font("Segoe UI", Font.PLAIN, 13)
        ));

        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setBackground(Color.WHITE);
        formFields.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Tiêu đề
        JLabel lblTieuDe = new JLabel("Tiêu đề:");
        lblTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(lblTieuDe);
        formFields.add(Box.createVerticalStrut(4));

        txtTieuDe = new JTextField();
        txtTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtTieuDe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtTieuDe.setBorder(new CompoundBorder(new LineBorder(new Color(180, 180, 180)), new EmptyBorder(3, 6, 3, 6)));
        txtTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(txtTieuDe);
        formFields.add(Box.createVerticalStrut(10));

        // Nội dung
        JLabel lblNoiDung = new JLabel("Nội dung:");
        lblNoiDung.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNoiDung.setAlignmentX(Component.LEFT_ALIGNMENT);
        formFields.add(lblNoiDung);
        formFields.add(Box.createVerticalStrut(4));

        txtNoiDung = new JTextArea(8, 20);
        txtNoiDung.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNoiDung.setLineWrap(true);
        txtNoiDung.setWrapStyleWord(true);
        JScrollPane scrollNoiDung = new JScrollPane(txtNoiDung);
        scrollNoiDung.setBorder(new LineBorder(new Color(180, 180, 180)));
        scrollNoiDung.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollNoiDung.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        scrollNoiDung.setPreferredSize(new Dimension(300, 140));
        formFields.add(scrollNoiDung);
        formFields.add(Box.createVerticalStrut(12));

        // Đối tượng
        JPanel rowDoiTuong = new JPanel(new BorderLayout(10, 0));
        rowDoiTuong.setBackground(Color.WHITE);
        rowDoiTuong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        rowDoiTuong.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblDoiTuong = new JLabel("Đối tượng:");
        lblDoiTuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDoiTuong.setPreferredSize(new Dimension(80, 25));
        rowDoiTuong.add(lblDoiTuong, BorderLayout.WEST);

        cbDoiTuong = new JComboBox<>(new String[]{"Tất cả người tham gia", "Sinh viên", "Ban tổ chức", "Cán bộ sự kiện", "Đại biểu", "Khách mời"});
        cbDoiTuong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbDoiTuong.setBackground(Color.WHITE);
        rowDoiTuong.add(cbDoiTuong, BorderLayout.CENTER);
        formFields.add(rowDoiTuong);
        formFields.add(Box.createVerticalStrut(8));

        // Ngày gửi
        JPanel rowNgayGui = new JPanel(new BorderLayout(10, 0));
        rowNgayGui.setBackground(Color.WHITE);
        rowNgayGui.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        rowNgayGui.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblNgayGui = new JLabel("Ngày gửi:");
        lblNgayGui.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNgayGui.setPreferredSize(new Dimension(80, 25));
        rowNgayGui.add(lblNgayGui, BorderLayout.WEST);

        txtNgayGui = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtNgayGui.setEditable(false); // hệ thống tự ghi ngày gửi
        txtNgayGui.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNgayGui.setBorder(new CompoundBorder(new LineBorder(new Color(180, 180, 180)), new EmptyBorder(3, 6, 3, 6)));
        rowNgayGui.add(txtNgayGui, BorderLayout.CENTER);
        formFields.add(rowNgayGui);
        formFields.add(Box.createVerticalStrut(8));

        // Trạng thái
        JPanel rowTrangThai = new JPanel(new BorderLayout(10, 0));
        rowTrangThai.setBackground(Color.WHITE);
        rowTrangThai.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        rowTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTrangThai.setPreferredSize(new Dimension(80, 25));
        rowTrangThai.add(lblTrangThai, BorderLayout.WEST);

        cbTrangThai = new JComboBox<>(new String[]{"Chờ duyệt", "Đã duyệt", "Đã đăng", "Từ chối", "Đã gỡ"});
        cbTrangThai.setEnabled(false); // trạng thái do Admin duyệt, cán bộ chỉ xem
        cbTrangThai.setToolTipText("Thông báo mới tạo sẽ ở trạng thái Chờ duyệt cho tới khi Admin duyệt");
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbTrangThai.setBackground(Color.WHITE);
        rowTrangThai.add(cbTrangThai, BorderLayout.CENTER);
        formFields.add(rowTrangThai);

        rightPanel.add(formFields, BorderLayout.CENTER);

        // Buttons row
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonsPanel.setBackground(Color.WHITE);

        btnTaoThongBao = createButton("Tạo thông báo");
        btnCapNhat = createButton("Cập nhật");
        btnXoa = createButton("Xóa");
        btnLamMoi = createButton("Làm mới");

        buttonsPanel.add(btnTaoThongBao);
        buttonsPanel.add(btnCapNhat);
        buttonsPanel.add(btnXoa);
        buttonsPanel.add(btnLamMoi);

        rightPanel.add(buttonsPanel, BorderLayout.SOUTH);

        contentGrid.add(rightPanel);
        mainPanel.add(contentGrid, BorderLayout.CENTER);

        // Selection Listener: đổ thông báo đang chọn lên form (cả nội dung, lý do từ chối nếu có)
        tableThongBao.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                JsonObject tb = thongBaoDangChon();
                if (tb != null) {
                    txtTieuDe.setText(Api.s(tb, "tieuDe"));
                    txtNoiDung.setText(Api.s(tb, "noiDung"));
                    txtNoiDung.setCaretPosition(0);
                    String dt = Api.s(tb, "doiTuong");
                    for (int i = 0; i < cbDoiTuong.getItemCount(); i++) {
                        if (cbDoiTuong.getItemAt(i).equalsIgnoreCase(dt)) {
                            cbDoiTuong.setSelectedIndex(i);
                            break;
                        }
                    }
                    txtNgayGui.setText(Api.s(tb, "ngayGui"));
                    cbTrangThai.setSelectedItem(Api.s(tb, "trangThai"));
                    String lyDo = Api.s(tb, "lyDoTuChoi");
                    cbTrangThai.setToolTipText(lyDo.isEmpty() ? null : "Lý do Admin từ chối: " + lyDo);
                }
            }
        });

        // Search Action
        btnTimKiem.addActionListener(e -> {
            String query = txtTimKiem.getText().trim();
            if (query.isEmpty()) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + query));
            }
        });

        // Create Action: POST /api/admin/thong-bao -> lưu DB ở trạng thái "Chờ duyệt" để Admin duyệt
        btnTaoThongBao.addActionListener(e -> {
            JsonObject body = layDuLieuForm();
            if (body == null) return;
            Api.chayNen(mainPanel, () -> Api.post("/api/admin/thong-bao?nguoiTao=" + Api.e(tenDangNhap), body), kq -> {
                JOptionPane.showMessageDialog(mainPanel, "Đã tạo thông báo " + Api.s(Api.obj(kq), "maThongBao")
                        + " và gửi Admin duyệt.\nSau khi Admin duyệt, người nhận sẽ thấy thông báo.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                resetForm();
                loadData();
            });
        });

        // Update Action: PUT /api/admin/thong-bao/{id} (thông báo bị từ chối, sửa xong sẽ quay lại Chờ duyệt)
        btnCapNhat.addActionListener(e -> {
            JsonObject tb = thongBaoDangChon();
            if (tb == null) {
                JOptionPane.showMessageDialog(mainPanel, "Vui lòng chọn thông báo cần cập nhật từ danh sách!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            JsonObject body = layDuLieuForm();
            if (body == null) return;
            Api.chayNen(mainPanel, () -> Api.put("/api/admin/thong-bao/" + Api.n(tb, "id"), body), kq -> {
                String tt = Api.s(Api.obj(kq), "trangThai");
                JOptionPane.showMessageDialog(mainPanel, "Cập nhật thông báo thành công! Trạng thái: " + tt,
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            });
        });

        // Delete Action: chỉ xóa được thông báo chưa được duyệt (Chờ duyệt / Từ chối)
        btnXoa.addActionListener(e -> {
            JsonObject tb = thongBaoDangChon();
            if (tb == null) {
                JOptionPane.showMessageDialog(mainPanel, "Vui lòng chọn thông báo cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String tt = Api.s(tb, "trangThai");
            if (!"Chờ duyệt".equals(tt) && !"Từ chối".equals(tt)) {
                JOptionPane.showMessageDialog(mainPanel, "Thông báo đã được Admin duyệt (" + tt + "), không thể xóa.\nHãy nhờ Admin gỡ thông báo.",
                        "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(mainPanel, "Bạn có chắc muốn xóa thông báo này không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                Api.chayNen(mainPanel, () -> Api.delete("/api/thong-bao/xoa/" + Api.n(tb, "id")), kq -> {
                    resetForm();
                    loadData();
                });
            }
        });

        // Reset Action
        btnLamMoi.addActionListener(e -> {
            resetForm();
            loadData();
            txtTimKiem.setText("");
            sorter.setRowFilter(null);
        });
    }

    private void resetForm() {
        txtTieuDe.setText("");
        txtNoiDung.setText("");
        cbDoiTuong.setSelectedIndex(0);
        cbTrangThai.setSelectedIndex(0);
        cbTrangThai.setToolTipText("Thông báo mới tạo sẽ ở trạng thái Chờ duyệt cho tới khi Admin duyệt");
        txtNgayGui.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        tableThongBao.clearSelection();
    }

    private JsonObject thongBaoDangChon() {
        int row = tableThongBao.getSelectedRow();
        if (row < 0) return null;
        int modelRow = tableThongBao.convertRowIndexToModel(row);
        return modelRow < dsThongBao.size() ? dsThongBao.get(modelRow) : null;
    }

    /** Kiểm tra form rồi tạo body {tieuDe, noiDung, doiTuong}; thiếu thì báo và trả null. */
    private JsonObject layDuLieuForm() {
        String tieuDe = txtTieuDe.getText().trim();
        String noiDung = txtNoiDung.getText().trim();
        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            JOptionPane.showMessageDialog(mainPanel, "Vui lòng nhập đủ tiêu đề và nội dung thông báo!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        JsonObject body = new JsonObject();
        body.addProperty("tieuDe", tieuDe);
        body.addProperty("noiDung", noiDung);
        body.addProperty("doiTuong", (String) cbDoiTuong.getSelectedItem());
        return body;
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(30, 30, 30));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(190, 190, 190), 1),
                new EmptyBorder(5, 12, 5, 12)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(30, 30, 30));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(190, 190, 190), 1),
                new EmptyBorder(3, 14, 3, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** GET /api/admin/thong-bao: toàn bộ thông báo trong DB, trạng thái theo kết quả Admin duyệt. */
    private void loadData() {
        Api.chayNen(mainPanel, () -> Api.get("/api/admin/thong-bao"), kq -> {
            tableModel.setRowCount(0);
            dsThongBao.clear();
            for (JsonElement el : Api.arr(kq)) {
                JsonObject tb = el.getAsJsonObject();
                dsThongBao.add(tb);
                tableModel.addRow(new Object[]{Api.s(tb, "maThongBao"), Api.s(tb, "tieuDe"),
                        Api.s(tb, "doiTuong"), Api.s(tb, "ngayGui"), Api.s(tb, "trangThai")});
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void $$$setupUI$$$() {
    }
}
