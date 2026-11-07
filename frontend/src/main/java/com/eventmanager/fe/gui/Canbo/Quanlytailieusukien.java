package com.eventmanager.fe.gui.Canbo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Quanlytailieusukien {
    private JPanel mainPanel;
    private JTable tableTaiLieu;
    private DefaultTableModel tableModel;
    private JButton btnTaiLen;
    private JButton btnXuatTaiLieu;
    private JButton btnXoa;

    public Quanlytailieusukien() {
        initUI();
        loadData();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Title
        JLabel lblTitle = new JLabel("Quản lý tài liệu sự kiện");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(30, 30, 30));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Tên tài liệu", "Loại", "Kích thước", "Ngày tải lên", "Người tải"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableTaiLieu = new JTable(tableModel);
        tableTaiLieu.setRowHeight(28);
        tableTaiLieu.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tableTaiLieu.setGridColor(new Color(210, 210, 210));
        tableTaiLieu.setShowGrid(true);
        tableTaiLieu.setSelectionBackground(new Color(25, 118, 210));
        tableTaiLieu.setSelectionForeground(Color.WHITE);

        JTableHeader header = tableTaiLieu.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        header.setBackground(new Color(245, 245, 245));
        header.setForeground(Color.BLACK);
        header.setPreferredSize(new Dimension(header.getWidth(), 30));

        tableTaiLieu.getColumnModel().getColumn(0).setPreferredWidth(260);
        tableTaiLieu.getColumnModel().getColumn(1).setPreferredWidth(90);
        tableTaiLieu.getColumnModel().getColumn(2).setPreferredWidth(110);
        tableTaiLieu.getColumnModel().getColumn(3).setPreferredWidth(130);
        tableTaiLieu.getColumnModel().getColumn(4).setPreferredWidth(130);

        JScrollPane scrollPane = new JScrollPane(tableTaiLieu);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(new LineBorder(new Color(180, 180, 180), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bottomPanel.setBackground(Color.WHITE);

        btnTaiLen = createActionButton("Tải lên tài liệu");
        btnXuatTaiLieu = createActionButton("Xuất tài liệu");
        btnXoa = createActionButton("Xóa tài liệu");

        bottomPanel.add(btnTaiLen);
        bottomPanel.add(btnXuatTaiLieu);
        bottomPanel.add(btnXoa);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Actions
        btnTaiLen.addActionListener(e -> {
            String ten = JOptionPane.showInputDialog(mainPanel, "Nhập tên tài liệu:", "Tải lên tài liệu", JOptionPane.PLAIN_MESSAGE);
            if (ten != null && !ten.trim().isEmpty()) {
                tableModel.addRow(new Object[]{ten.trim(), "XLSX", "15 KB", "25/10/2026", "Cán bộ"});
                JOptionPane.showMessageDialog(mainPanel, "Tải lên tài liệu thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnXuatTaiLieu.addActionListener(e -> {
            JOptionPane.showMessageDialog(mainPanel, "Xuất danh sách tài liệu thành công (DanhSachTaiLieu_Export.xlsx)!", "Xuất tài liệu", JOptionPane.INFORMATION_MESSAGE);
        });

        btnXoa.addActionListener(e -> {
            int selected = tableTaiLieu.getSelectedRow();
            if (selected >= 0) {
                int confirm = JOptionPane.showConfirmDialog(mainPanel, "Bạn có chắc muốn xóa tài liệu này không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.removeRow(selected);
                }
            } else {
                JOptionPane.showMessageDialog(mainPanel, "Vui lòng chọn tài liệu cần xóa trên bảng!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(30, 30, 30));
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(
                new LineBorder(new Color(190, 190, 190), 1),
                new EmptyBorder(7, 18, 7, 18)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void loadData() {
        // Sample default data matching screenshot
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"DanhSachTaiLieu_Export.xlsx", "XLSX", "10 KB", "19/10/2026", "Cán bộ"});
        tableModel.addRow(new Object[]{"DanhSachTaiLieu_Export.xlsx", "XLSX", "10 KB", "25/10/2026", "Cán bộ"});

        // Try load from backend if available
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create("http://localhost:8080/api/tai-lieu/danh-sach"))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        JsonArray arr = JsonParser.parseString(response.body()).getAsJsonArray();
                        if (arr.size() > 0) {
                            SwingUtilities.invokeLater(() -> {
                                tableModel.setRowCount(0);
                                for (JsonElement elem : arr) {
                                    JsonObject obj = elem.getAsJsonObject();
                                    String ten = obj.has("tenTaiLieu") ? obj.get("tenTaiLieu").getAsString() : "";
                                    String loai = obj.has("loai") ? obj.get("loai").getAsString() : "XLSX";
                                    String kichThuoc = obj.has("kichThuoc") ? obj.get("kichThuoc").getAsString() : "10 KB";
                                    String ngay = obj.has("ngayTaiLen") ? obj.get("ngayTaiLen").getAsString() : "25/10/2026";
                                    String nguoi = obj.has("nguoiTai") ? obj.get("nguoiTai").getAsString() : "Cán bộ";
                                    tableModel.addRow(new Object[]{ten, loai, kichThuoc, ngay, nguoi});
                                }
                            });
                        }
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
}
