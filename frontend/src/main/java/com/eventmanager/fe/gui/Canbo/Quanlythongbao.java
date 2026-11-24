package com.eventmanager.fe.gui.Canbo;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Quản lý thông báo của Cán bộ (Redesign hiện đại):
 * - Có nút "Gửi thông báo" để gửi ngay cho học sinh / sinh viên thấy trên app.
 * - Có nút "Lưu nháp" / "Cập nhật" / "Xóa" / "Làm mới".
 * - Bảng danh sách thông báo phân loại trạng thái bằng Badge màu trực quan.
 * - Hỗ trợ liên kết thông báo với sự kiện cụ thể.
 */
public class Quanlythongbao {
    private JPanel mainPanel;

    // Stat cards
    private UI.StatCard cardTong, cardDaGui, cardChoDuyet, cardNhap;

    // Left: Danh sách thông báo
    private BangDep bang;
    private UI.TextFieldDep txtTimKiem;
    private UI.TabBar tabBar;
    private int tabHienTai = 0; // 0: Tất cả, 1: Đã gửi/đăng, 2: Chờ duyệt, 3: Nháp

    // Right: Form thông tin thông báo
    private JLabel lblFormTitle;
    private UI.Badge badgeTrangThai;
    private JComboBox<SuKienItem> cbSuKien;
    private UI.TextFieldDep txtTieuDe;
    private JTextArea txtNoiDung;
    private JComboBox<String> cbDoiTuong;
    private JTextField txtNgayGui;
    private JTextField txtTrangThaiHienThi;

    // Action buttons
    private UI.FlatButton btnGuiThongBao;
    private UI.FlatButton btnLuuNhap;
    private UI.FlatButton btnCapNhat;
    private UI.FlatButton btnXoa;
    private UI.FlatButton btnLamMoi;

    private final String tenDangNhap;
    private final List<JsonObject> dsThongBaoGoc = new ArrayList<>();
    private final List<JsonObject> dsHienThi = new ArrayList<>();
    private Long idThongBaoDangChon = null;

    private record SuKienItem(long id, String ten) {
        @Override
        public String toString() {
            return ten;
        }
    }

    public Quanlythongbao() {
        this("canbo01");
    }

    public Quanlythongbao(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap == null || tenDangNhap.isBlank() ? "canbo01" : tenDangNhap;
        initUI();
        loadSuKien();
        loadData();
    }

    public void taiLai() {
        loadData();
    }

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Theme.BG);

        // ==================== TOP: HEADER + STAT CARDS ====================
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(24, 28, 0, 28));

        JPanel headerRow = UI.tieuDeTrang("Quản lý thông báo sự kiện",
                "Soạn thảo và gửi thông báo trực tiếp tới sinh viên và ban tổ chức");
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        topPanel.add(headerRow);
        topPanel.add(Box.createVerticalStrut(20));

        // 4 Stat Cards
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);
        statsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        cardTong = new UI.StatCard("Tổng thông báo", Icons.BELL, Theme.PRIMARY, Theme.PRIMARY_SOFT);
        cardDaGui = new UI.StatCard("Đã gửi / Đã đăng", Icons.SEND, Theme.SUCCESS, Theme.SUCCESS_SOFT);
        cardChoDuyet = new UI.StatCard("Chờ duyệt", Icons.CLOCK, Theme.WARNING, Theme.WARNING_SOFT);
        cardNhap = new UI.StatCard("Bản nháp / Khác", Icons.EDIT, Theme.MUTED, Theme.GRAY_SOFT);

        statsRow.add(cardTong);
        statsRow.add(cardDaGui);
        statsRow.add(cardChoDuyet);
        statsRow.add(cardNhap);
        topPanel.add(statsRow);
        topPanel.add(Box.createVerticalStrut(20));

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ==================== CENTER: 2-COLUMN SPLIT ====================
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 28, 20, 28));

        // ---------- CỘT TRÁI: DANH SÁCH THÔNG BÁO ----------
        JPanel leftCard = UI.card(new BorderLayout(0, 12), 16);

        // Search + Tab filter
        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));

        JPanel searchRow = new JPanel(new BorderLayout(10, 0));
        searchRow.setOpaque(false);
        txtTimKiem = UI.oTimKiem("Tìm theo tiêu đề, đối tượng, mã...");
        txtTimKiem.setPreferredSize(new Dimension(240, 36));
        txtTimKiem.addActionListener(e -> locDanhSach());
        txtTimKiem.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { locDanhSach(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { locDanhSach(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { locDanhSach(); }
        });
        searchRow.add(txtTimKiem, BorderLayout.CENTER);

        UI.FlatButton btnLamMoiTable = UI.nutIcon(Icons.REFRESH, "Tải lại danh sách");
        btnLamMoiTable.addActionListener(e -> loadData());
        searchRow.add(btnLamMoiTable, BorderLayout.EAST);
        leftHeader.add(searchRow);
        leftHeader.add(Box.createVerticalStrut(10));

        tabBar = new UI.TabBar(idx -> {
            tabHienTai = idx;
            locDanhSach();
        }, "Tất cả", "Đã gửi", "Chờ duyệt", "Nháp");
        leftHeader.add(tabBar);

        leftCard.add(leftHeader, BorderLayout.NORTH);

        // Bảng BangDep
        bang = new BangDep("ID", "Mã", "Tiêu đề", "Đối tượng", "Ngày gửi", "Trạng thái", "Thao tác");
        bang.anCot(0); // Ẩn ID
        bang.doRongCot(1, 65);  // Mã
        bang.doRongCot(2, 170); // Tiêu đề
        bang.doRongCot(3, 110); // Đối tượng
        bang.doRongCot(4, 90);  // Ngày gửi
        bang.cotNhan(5);        // Trạng thái (Badge tự động định dạng)
        bang.coDinhCot(6, 90);  // Thao tác

        // Cột thao tác trên dòng: Nút Gửi nhanh + Nút Xóa
        bang.cotThaoTac(6, List.of(
                new BangDep.NutThaoTac("gui", null, Icons.SEND, "Gửi ngay cho sinh viên"),
                new BangDep.NutThaoTac("xoa", null, Icons.TRASH, "Xóa thông báo")
        ), (ma, dong) -> {
            if ("gui".equals(ma)) {
                // Chỉ hiện nút gửi nếu chưa đăng / chưa gửi
                String tt = String.valueOf(bang.getModelBang().getValueAt(dong, 5));
                return !"Đã đăng".equals(tt);
            }
            return true;
        }, (ma, dong) -> {
            if (dong < 0 || dong >= dsHienThi.size()) return;
            JsonObject tb = dsHienThi.get(dong);
            if ("gui".equals(ma)) {
                guiThongBaoTheoId(Api.n(tb, "id"), Api.s(tb, "tieuDe"));
            } else if ("xoa".equals(ma)) {
                xoaThongBaoTheoId(Api.n(tb, "id"), Api.s(tb, "tieuDe"));
            }
        });

        // Bấm chọn dòng trong bảng để hiển thị lên form bên phải
        bang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = bang.getSelectedRow();
                if (row >= 0) {
                    int modelRow = bang.convertRowIndexToModel(row);
                    if (modelRow < dsHienThi.size()) {
                        hienThiLenForm(dsHienThi.get(modelRow));
                    }
                }
            }
        });

        leftCard.add(bang.trongKhungCuon(), BorderLayout.CENTER);
        centerPanel.add(leftCard);

        // ---------- CỘT PHẢI: SOẠN THẢO VÀ GỬI THÔNG BÁO ----------
        JPanel rightCard = UI.card(new BorderLayout(0, 12), 18);

        // Header cột phải: Tiêu đề form + Badge trạng thái
        JPanel rightTop = new JPanel(new BorderLayout(8, 0));
        rightTop.setOpaque(false);
        lblFormTitle = UI.nhan("Soạn thông báo mới", Theme.H2, Theme.TEXT);
        rightTop.add(lblFormTitle, BorderLayout.WEST);

        badgeTrangThai = new UI.Badge();
        badgeTrangThai.setText("Mới");
        rightTop.add(badgeTrangThai, BorderLayout.EAST);
        rightCard.add(rightTop, BorderLayout.NORTH);

        // Form Fields
        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        // 1. Sự kiện liên quan
        formPanel.add(UI.nhan("Sự kiện liên quan (tùy chọn)", Theme.SMALL_BOLD, Theme.TEXT_2));
        formPanel.add(Box.createVerticalStrut(4));
        cbSuKien = UI.hopChon(new SuKienItem[]{new SuKienItem(0, "-- Không liên kết sự kiện --")}, Icons.CALENDAR);
        cbSuKien.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        cbSuKien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cbSuKien.addActionListener(e -> {
            SuKienItem sk = (SuKienItem) cbSuKien.getSelectedItem();
            if (sk != null && sk.id() > 0 && idThongBaoDangChon == null) {
                // Gợi ý tiêu đề gắn với sự kiện
                if (txtTieuDe.getText().trim().isEmpty() || txtTieuDe.getText().startsWith("[")) {
                    txtTieuDe.setText("[" + sk.ten() + "] ");
                    txtTieuDe.requestFocus();
                }
            }
        });
        formPanel.add(cbSuKien);
        formPanel.add(Box.createVerticalStrut(10));

        // 2. Tiêu đề
        formPanel.add(UI.nhan("Tiêu đề thông báo *", Theme.SMALL_BOLD, Theme.TEXT_2));
        formPanel.add(Box.createVerticalStrut(4));
        txtTieuDe = UI.oNhap("Nhập tiêu đề thông báo...");
        txtTieuDe.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        txtTieuDe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        formPanel.add(txtTieuDe);
        formPanel.add(Box.createVerticalStrut(10));

        // 3. Đối tượng nhận
        formPanel.add(UI.nhan("Đối tượng nhận thông báo *", Theme.SMALL_BOLD, Theme.TEXT_2));
        formPanel.add(Box.createVerticalStrut(4));
        cbDoiTuong = UI.hopChon(new String[]{
                "Tất cả người tham gia",
                "Sinh viên",
                "Ban tổ chức",
                "Cán bộ sự kiện",
                "Đại biểu",
                "Khách mời"
        }, Icons.USERS);
        cbDoiTuong.setPreferredSize(new Dimension(Integer.MAX_VALUE, 36));
        cbDoiTuong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        formPanel.add(cbDoiTuong);
        formPanel.add(Box.createVerticalStrut(10));

        // 4. Nội dung thông báo
        formPanel.add(UI.nhan("Nội dung thông báo *", Theme.SMALL_BOLD, Theme.TEXT_2));
        formPanel.add(Box.createVerticalStrut(4));
        txtNoiDung = UI.vungChu(7);
        JScrollPane spNoiDung = UI.cuonVungChu(txtNoiDung);
        spNoiDung.setPreferredSize(new Dimension(Integer.MAX_VALUE, 130));
        spNoiDung.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        formPanel.add(spNoiDung);
        formPanel.add(Box.createVerticalStrut(10));

        // 5. Thông tin kèm theo: Ngày gửi & Trạng thái (hàng 2 ô)
        JPanel metaRow = new JPanel(new GridLayout(1, 2, 12, 0));
        metaRow.setOpaque(false);
        metaRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel colNgay = new JPanel(new BorderLayout(0, 4));
        colNgay.setOpaque(false);
        colNgay.add(UI.nhan("Ngày gửi", Theme.SMALL_BOLD, Theme.TEXT_2), BorderLayout.NORTH);
        txtNgayGui = UI.oNhap(null);
        txtNgayGui.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtNgayGui.setEditable(false);
        colNgay.add(txtNgayGui, BorderLayout.CENTER);
        metaRow.add(colNgay);

        JPanel colTT = new JPanel(new BorderLayout(0, 4));
        colTT.setOpaque(false);
        colTT.add(UI.nhan("Trạng thái", Theme.SMALL_BOLD, Theme.TEXT_2), BorderLayout.NORTH);
        txtTrangThaiHienThi = UI.oNhap(null);
        txtTrangThaiHienThi.setText("Soạn mới");
        txtTrangThaiHienThi.setEditable(false);
        colTT.add(txtTrangThaiHienThi, BorderLayout.CENTER);
        metaRow.add(colTT);

        formPanel.add(metaRow);
        formPanel.add(Box.createVerticalGlue());

        rightCard.add(formPanel, BorderLayout.CENTER);

        // Buttons Action Panel
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        rightActions.setOpaque(false);

        // NÚT GỬI THÔNG BÁO: Nổi bật nhất (Primary)
        btnGuiThongBao = UI.nut("Gửi thông báo", Icons.SEND, UI.Kieu.CHINH);
        btnGuiThongBao.setToolTipText("Gửi ngay thông báo tới sinh viên / người tham gia để họ thấy trên ứng dụng");
        btnGuiThongBao.addActionListener(e -> thucHienGuiThongBao());

        // Nút Lưu nháp
        btnLuuNhap = UI.nut("Lưu nháp", Icons.EDIT, UI.Kieu.VIEN);
        btnLuuNhap.setToolTipText("Lưu thông báo ở dạng nháp / chờ duyệt");
        btnLuuNhap.addActionListener(e -> thucHienLuuNhap());

        // Nút Cập nhật
        btnCapNhat = UI.nut("Cập nhật", Icons.CHECK, UI.Kieu.VIEN);
        btnCapNhat.setToolTipText("Lưu các thay đổi của thông báo đang chọn");
        btnCapNhat.addActionListener(e -> thucHienCapNhat());

        // Nút Xóa
        btnXoa = UI.nut("Xóa", Icons.TRASH, UI.Kieu.NGUY_HIEM);
        btnXoa.setToolTipText("Xóa thông báo đang chọn");
        btnXoa.addActionListener(e -> thucHienXoaForm());

        // Nút Soạn mới
        btnLamMoi = UI.nut("Soạn mới", Icons.REFRESH, UI.Kieu.VIEN);
        btnLamMoi.setToolTipText("Làm mới form để soạn thông báo mới");
        btnLamMoi.addActionListener(e -> resetForm());

        rightActions.add(btnLamMoi);
        rightActions.add(btnXoa);
        rightActions.add(btnCapNhat);
        rightActions.add(btnLuuNhap);
        rightActions.add(btnGuiThongBao);

        rightCard.add(rightActions, BorderLayout.SOUTH);

        centerPanel.add(rightCard);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    // ==================== TẢI DỮ LIỆU ====================

    private void loadSuKien() {
        Api.chayNen(mainPanel, () -> Api.get("/api/su-kien/danh-sach"), kq -> {
            cbSuKien.removeAllItems();
            cbSuKien.addItem(new SuKienItem(0, "-- Không liên kết sự kiện --"));
            for (JsonElement el : Api.arr(kq)) {
                JsonObject sk = el.getAsJsonObject();
                cbSuKien.addItem(new SuKienItem(Api.n(sk, "id"), Api.s(sk, "tenSuKien")));
            }
        });
    }

    private void loadData() {
        Api.chayNen(mainPanel, () -> Api.get("/api/admin/thong-bao"), kq -> {
            dsThongBaoGoc.clear();
            int tong = 0, daGui = 0, choDuyet = 0, nhap = 0;

            for (JsonElement el : Api.arr(kq)) {
                JsonObject tb = el.getAsJsonObject();
                dsThongBaoGoc.add(tb);
                tong++;
                String tt = Api.s(tb, "trangThai");
                if ("Đã đăng".equals(tt) || "Đã gửi".equals(tt) || "Đã duyệt".equals(tt)) daGui++;
                else if ("Chờ duyệt".equals(tt)) choDuyet++;
                else nhap++;
            }

            // Cập nhật StatCards
            cardTong.setSo(tong);
            cardDaGui.setSo(daGui);
            cardChoDuyet.setSo(choDuyet);
            cardNhap.setSo(nhap);

            tabBar.doiTen(0, "Tất cả (" + tong + ")");
            tabBar.doiTen(1, "Đã gửi (" + daGui + ")");
            tabBar.doiTen(2, "Chờ duyệt (" + choDuyet + ")");
            tabBar.doiTen(3, "Nháp (" + nhap + ")");

            locDanhSach();
        });
    }

    private void locDanhSach() {
        String query = txtTimKiem.getText().trim().toLowerCase();
        dsHienThi.clear();
        bang.xoaHet();

        for (JsonObject tb : dsThongBaoGoc) {
            String tt = Api.s(tb, "trangThai");
            boolean matchTab = switch (tabHienTai) {
                case 1 -> "Đã đăng".equals(tt) || "Đã gửi".equals(tt) || "Đã duyệt".equals(tt);
                case 2 -> "Chờ duyệt".equals(tt);
                case 3 -> "Nháp".equals(tt) || "Từ chối".equals(tt) || "Đã hủy".equals(tt);
                default -> true;
            };
            if (!matchTab) continue;

            String searchField = (Api.s(tb, "maThongBao") + " " + Api.s(tb, "tieuDe") + " " + Api.s(tb, "doiTuong")).toLowerCase();
            if (!query.isEmpty() && !searchField.contains(query)) continue;

            dsHienThi.add(tb);
            bang.themDong(
                    Api.n(tb, "id"),
                    Api.s(tb, "maThongBao"),
                    Api.s(tb, "tieuDe"),
                    Api.s(tb, "doiTuong"),
                    Api.s(tb, "ngayGui"),
                    tt,
                    ""
            );
        }
    }

    private void hienThiLenForm(JsonObject tb) {
        idThongBaoDangChon = Api.n(tb, "id");
        String ma = Api.s(tb, "maThongBao");
        lblFormTitle.setText("Chi tiết: " + ma);

        String tt = Api.s(tb, "trangThai");
        badgeTrangThai.setText(tt);
        txtTrangThaiHienThi.setText(tt);

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

        // Nếu thông báo đã đăng/gửi thì nút Gửi đổi thành "Đã gửi" hoặc cho phép gửi lại
        if ("Đã đăng".equals(tt) || "Đã gửi".equals(tt)) {
            btnGuiThongBao.setText("Gửi lại");
        } else {
            btnGuiThongBao.setText("Gửi thông báo");
        }
    }

    private void resetForm() {
        idThongBaoDangChon = null;
        lblFormTitle.setText("Soạn thông báo mới");
        badgeTrangThai.setText("Mới");
        txtTrangThaiHienThi.setText("Soạn mới");
        txtTieuDe.setText("");
        txtNoiDung.setText("");
        cbDoiTuong.setSelectedIndex(0);
        cbSuKien.setSelectedIndex(0);
        txtNgayGui.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        btnGuiThongBao.setText("Gửi thông báo");
        bang.clearSelection();
    }

    // ==================== CÁC HÀNH ĐỘNG GỬI / TẠO / SỬA / XÓA ====================

    /**
     * Nút GỬI THÔNG BÁO:
     * - Nếu đang chọn 1 thông báo có sẵn: Gửi thông báo đó (PUT /api/thong-bao/gui/{id})
     * - Nếu đang soạn mới: Tạo mới và gửi ngay (POST /api/admin/thong-bao?guiNgay=true)
     */
    private void thucHienGuiThongBao() {
        String tieuDe = txtTieuDe.getText().trim();
        String noiDung = txtNoiDung.getText().trim();
        String doiTuong = (String) cbDoiTuong.getSelectedItem();

        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            HopThoai.loi(mainPanel, "Vui lòng nhập đầy đủ tiêu đề và nội dung thông báo!");
            return;
        }

        if (!HopThoai.xacNhan(mainPanel, "Xác nhận gửi thông báo",
                "Thông báo sẽ được gửi tới <b>" + doiTuong + "</b>.<br>Học sinh/sinh viên sẽ nhìn thấy thông báo này ngay trên ứng dụng. Tiếp tục?",
                "Gửi ngay", UI.Kieu.CHINH)) {
            return;
        }

        if (idThongBaoDangChon != null) {
            // Cập nhật nội dung trước rồi gửi
            JsonObject body = new JsonObject();
            body.addProperty("tieuDe", tieuDe);
            body.addProperty("noiDung", noiDung);
            body.addProperty("doiTuong", doiTuong);
            Api.chayNen(mainPanel, () -> {
                Api.put("/api/admin/thong-bao/" + idThongBaoDangChon, body);
                return Api.put("/api/thong-bao/gui/" + idThongBaoDangChon, new JsonObject());
            }, res -> {
                HopThoai.thanhCong(mainPanel, "Đã gửi thông báo thành công! Sinh viên đã có thể thấy thông báo này.");
                loadData();
            });
        } else {
            // Tạo mới và gửi ngay
            JsonObject body = new JsonObject();
            body.addProperty("tieuDe", tieuDe);
            body.addProperty("noiDung", noiDung);
            body.addProperty("doiTuong", doiTuong);
            Api.chayNen(mainPanel, () -> Api.post("/api/admin/thong-bao?guiNgay=true&nguoiTao=" + Api.e(tenDangNhap), body), kq -> {
                HopThoai.thanhCong(mainPanel, "Đã tạo và gửi thông báo thành công!");
                resetForm();
                loadData();
            });
        }
    }

    /** Gửi thông báo theo ID trực tiếp từ nút trên dòng bảng */
    private void guiThongBaoTheoId(long id, String tieuDe) {
        if (!HopThoai.xacNhan(mainPanel, "Gửi thông báo",
                "Bạn có chắc muốn gửi thông báo:<br><b>" + tieuDe + "</b><br>tới sinh viên ngay bây giờ?",
                "Gửi ngay", UI.Kieu.CHINH)) {
            return;
        }
        Api.chayNen(mainPanel, () -> Api.put("/api/thong-bao/gui/" + id, new JsonObject()), res -> {
            HopThoai.thanhCong(mainPanel, "Đã gửi thông báo thành công tới sinh viên!");
            loadData();
        });
    }

    /** Lưu dạng nháp / chờ duyệt (POST guiNgay=false) */
    private void thucHienLuuNhap() {
        String tieuDe = txtTieuDe.getText().trim();
        String noiDung = txtNoiDung.getText().trim();
        String doiTuong = (String) cbDoiTuong.getSelectedItem();

        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            HopThoai.loi(mainPanel, "Vui lòng nhập đủ tiêu đề và nội dung!");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("tieuDe", tieuDe);
        body.addProperty("noiDung", noiDung);
        body.addProperty("doiTuong", doiTuong);

        Api.chayNen(mainPanel, () -> Api.post("/api/admin/thong-bao?guiNgay=false&nguoiTao=" + Api.e(tenDangNhap), body), kq -> {
            HopThoai.thanhCong(mainPanel, "Đã lưu bản nháp thông báo!");
            resetForm();
            loadData();
        });
    }

    /** Cập nhật thông báo đang chọn */
    private void thucHienCapNhat() {
        if (idThongBaoDangChon == null) {
            HopThoai.loi(mainPanel, "Vui lòng chọn một thông báo từ danh sách để cập nhật!");
            return;
        }
        String tieuDe = txtTieuDe.getText().trim();
        String noiDung = txtNoiDung.getText().trim();
        String doiTuong = (String) cbDoiTuong.getSelectedItem();

        if (tieuDe.isEmpty() || noiDung.isEmpty()) {
            HopThoai.loi(mainPanel, "Vui lòng nhập đủ tiêu đề và nội dung!");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("tieuDe", tieuDe);
        body.addProperty("noiDung", noiDung);
        body.addProperty("doiTuong", doiTuong);

        Api.chayNen(mainPanel, () -> Api.put("/api/admin/thong-bao/" + idThongBaoDangChon, body), kq -> {
            HopThoai.thanhCong(mainPanel, "Cập nhật thông báo thành công!");
            loadData();
        });
    }

    /** Xóa thông báo từ form */
    private void thucHienXoaForm() {
        if (idThongBaoDangChon == null) {
            HopThoai.loi(mainPanel, "Vui lòng chọn thông báo cần xóa!");
            return;
        }
        xoaThongBaoTheoId(idThongBaoDangChon, txtTieuDe.getText());
    }

    /** Xóa thông báo theo ID */
    private void xoaThongBaoTheoId(long id, String tieuDe) {
        if (!HopThoai.xacNhan(mainPanel, "Xác nhận xóa",
                "Bạn có chắc muốn xóa thông báo: <b>" + tieuDe + "</b>?",
                "Xóa", UI.Kieu.NGUY_HIEM)) {
            return;
        }
        Api.chayNen(mainPanel, () -> Api.delete("/api/thong-bao/xoa/" + id), res -> {
            HopThoai.thanhCong(mainPanel, "Đã xóa thông báo!");
            resetForm();
            loadData();
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void $$$setupUI$$$() {
    }
}
