package com.eventmanager.fe.gui.Canbo;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * QUẢN LÝ CÔNG VIỆC — Cán bộ phụ trách sự kiện
 *
 * Đáp ứng đầy đủ 4 Use Case:
 *  1. Lập kế hoạch sự kiện        → Thêm / sửa công việc gắn với sự kiện, đặt hạn chót & mức ưu tiên
 *  2. Phân công nhiệm vụ          → Gán / thay đổi cán bộ phụ trách từ danh sách nhân sự hệ thống
 *  3. Theo dõi tiến độ            → Thẻ thống kê, thanh tiến độ tổng quan, progress bar từng công việc, cảnh báo quá hạn
 *  4. Cập nhật trạng thái công việc → Chuyển trạng thái nhanh (Chưa bắt đầu / Đang thực hiện / Hoàn thành / Tạm hoãn) & tiến độ %
 */
public class Quanlycongviec {

    private static final String[] TRANG_THAI_LIST = {
            "Tất cả", "Chưa bắt đầu", "Đang thực hiện", "Hoàn thành", "Tạm hoãn"
    };
    private static final String[] TRANG_THAI_EDIT = {
            "Chưa bắt đầu", "Đang thực hiện", "Hoàn thành", "Tạm hoãn"
    };
    private static final String[] UU_TIEN_LIST = {"Trung bình", "Cao", "Thấp"};

    private JPanel mainPanel;
    private BangDep bang;
    private UI.StatCard cardTong, cardDang, cardXong, cardQuaHan;
    private JLabel lblTienDoTB;
    private JPanel pnlThanhTienDoTB;
    private int tienDoTrungBinh = 0;

    private UI.TabBar tabTrangThai;
    private JComboBox<SuKienOption> cbLocSuKien;
    private UI.TextFieldDep oTim;

    private JsonArray danhSachGoc = new JsonArray();
    private final List<SuKienOption> dsSuKien = new ArrayList<>();
    private final List<String> dsNhanSu = new ArrayList<>();
    private boolean dangCapNhatBoLoc = false;

    private static final int PAGE_SIZE = 12;
    private int trangHienTai = 0;
    private UI.PhanTrang phanTrang;

    private final String tenDangNhap;   // cán bộ đang đăng nhập
    private JCheckBox chkCuaToi;        // chỉ hiện việc được giao cho mình

    public Quanlycongviec() {
        this("canbo01");
    }

    public Quanlycongviec(String tenDangNhap) {
        this.tenDangNhap = (tenDangNhap == null || tenDangNhap.isBlank()) ? "canbo01" : tenDangNhap.trim();
        initUI();
    }

    // ==================== KHỞI TẠO GIAO DIỆN ====================

    private void initUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.BG);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(new EmptyBorder(26, 30, 22, 30));

        // ===== 1. HEADER: TIÊU ĐỀ + HÀNH ĐỘNG CHÍNH =====
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));

        JPanel tieuDe = UI.tieuDeTrang(
                "Quản lý công việc",
                "Lập kế hoạch sự kiện, phân công nhiệm vụ, theo dõi tiến độ và cập nhật trạng thái"
        );
        header.add(tieuDe, BorderLayout.WEST);

        UI.FlatButton btnLamMoi = UI.nut("Làm mới", Icons.REFRESH, UI.Kieu.VIEN);
        btnLamMoi.addActionListener(e -> taiLai());

        UI.FlatButton btnLapKeHoach = UI.nut("Lập kế hoạch công việc", Icons.PLUS, UI.Kieu.CHINH);
        btnLapKeHoach.addActionListener(e -> hienFormThem());

        JPanel headerActions = new JPanel(new GridBagLayout());
        headerActions.setOpaque(false);
        headerActions.add(UI.hang(10, btnLamMoi, btnLapKeHoach));
        header.add(headerActions, BorderLayout.EAST);

        noiDung.add(header);
        noiDung.add(Box.createVerticalStrut(18));

        // ===== 2. THỐNG KÊ & THEO DÕI TIẾN ĐỘ TỔNG QUAN =====
        JPanel hangThe = new JPanel(new GridLayout(1, 4, 16, 0));
        hangThe.setOpaque(false);
        hangThe.setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
        hangThe.setAlignmentX(Component.LEFT_ALIGNMENT);

        cardTong = new UI.StatCard("Tổng công việc", Icons.LIST, Theme.PRIMARY, Theme.PRIMARY_SOFT);
        cardDang = new UI.StatCard("Đang thực hiện", Icons.CLOCK, Theme.WARNING, Theme.WARNING_SOFT);
        cardXong = new UI.StatCard("Hoàn thành", Icons.CHECK_CIRCLE, Theme.SUCCESS, Theme.SUCCESS_SOFT);
        cardQuaHan = new UI.StatCard("Quá hạn", Icons.BELL, Theme.DANGER, Theme.DANGER_SOFT);

        hangThe.add(cardTong);
        hangThe.add(cardDang);
        hangThe.add(cardXong);
        hangThe.add(cardQuaHan);
        noiDung.add(hangThe);
        noiDung.add(Box.createVerticalStrut(14));

        // Thanh tiến độ trung bình toàn kế hoạch
        UI.RoundPanel cardTienDoTong = UI.card(new BorderLayout(16, 0), 14);
        cardTienDoTong.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardTienDoTong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        JLabel lblTieuDeTD = UI.nhan("Tiến độ trung bình toàn bộ kế hoạch", Theme.BODY_BOLD, Theme.TEXT);
        lblTieuDeTD.setIcon(Icons.of(Icons.CHART, 18, Theme.PRIMARY));
        lblTieuDeTD.setIconTextGap(8);
        cardTienDoTong.add(lblTieuDeTD, BorderLayout.WEST);

        pnlThanhTienDoTB = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = Theme.muot(g);
                int h = 10;
                int y = (getHeight() - h) / 2;
                int w = getWidth();
                g2.setColor(Theme.GRAY_SOFT);
                g2.fillRoundRect(0, y, w, h, h, h);
                Color mau = tienDoTrungBinh >= 80 ? Theme.SUCCESS
                        : tienDoTrungBinh >= 40 ? Theme.PRIMARY : Theme.WARNING;
                g2.setColor(mau);
                int fillW = (int) Math.round(w * Math.max(0, Math.min(100, tienDoTrungBinh)) / 100.0);
                if (fillW > 0) g2.fillRoundRect(0, y, fillW, h, h, h);
                g2.dispose();
            }
        };
        pnlThanhTienDoTB.setOpaque(false);
        cardTienDoTong.add(pnlThanhTienDoTB, BorderLayout.CENTER);

        lblTienDoTB = UI.nhan("0%", Theme.H2, Theme.PRIMARY);
        lblTienDoTB.setPreferredSize(new Dimension(54, 24));
        lblTienDoTB.setHorizontalAlignment(SwingConstants.RIGHT);
        cardTienDoTong.add(lblTienDoTB, BorderLayout.EAST);

        noiDung.add(cardTienDoTong);
        noiDung.add(Box.createVerticalStrut(16));

        // ===== 3. KHỐI DANH SÁCH CÔNG VIỆC (TAB + BỘ LỌC + BẢNG + PHÂN TRANG) =====
        UI.RoundPanel cardBang = UI.card(new BorderLayout(0, 0), 0);
        cardBang.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Phần đầu card: Tab trạng thái + Thanh lọc sự kiện & tìm kiếm
        JPanel dauCard = new JPanel();
        dauCard.setOpaque(false);
        dauCard.setLayout(new BoxLayout(dauCard, BoxLayout.Y_AXIS));

        tabTrangThai = new UI.TabBar(idx -> locVaHienThi(), TRANG_THAI_LIST);
        tabTrangThai.setBorder(new EmptyBorder(4, 12, 0, 12));
        tabTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        dauCard.add(tabTrangThai);

        JPanel thanhLoc = new JPanel(new BorderLayout(12, 0));
        thanhLoc.setOpaque(false);
        thanhLoc.setBorder(new EmptyBorder(12, 16, 12, 16));
        thanhLoc.setAlignmentX(Component.LEFT_ALIGNMENT);

        cbLocSuKien = UI.hopChon(new SuKienOption[]{new SuKienOption(null, "Tất cả sự kiện")}, Icons.CALENDAR);
        cbLocSuKien.setPreferredSize(new Dimension(260, 36));
        cbLocSuKien.addActionListener(e -> {
            if (!dangCapNhatBoLoc) locVaHienThi();
        });

        oTim = UI.oTimKiem("Tìm theo mã CV, tên công việc, nhân sự...");
        oTim.setPreferredSize(new Dimension(290, 36));
        oTim.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locVaHienThi(); }
            @Override public void removeUpdate(DocumentEvent e) { locVaHienThi(); }
            @Override public void changedUpdate(DocumentEvent e) { locVaHienThi(); }
        });

        chkCuaToi = new JCheckBox("Việc giao cho tôi");
        chkCuaToi.setOpaque(false);
        chkCuaToi.setFont(Theme.BODY);
        chkCuaToi.setToolTipText("Chỉ hiện công việc Admin / cán bộ khác đã phân công cho " + tenDangNhap);
        chkCuaToi.addActionListener(e -> locVaHienThi());

        thanhLoc.add(UI.hang(10, cbLocSuKien, oTim, chkCuaToi), BorderLayout.WEST);

        JLabel lblGoiY = UI.nhan("Bấm các nút bên phải từng dòng để sửa, phân công hoặc cập nhật trạng thái",
                Theme.SMALL, Theme.MUTED);
        thanhLoc.add(lblGoiY, BorderLayout.EAST);

        dauCard.add(thanhLoc);
        cardBang.add(dauCard, BorderLayout.NORTH);

        // Bảng dữ liệu
        bang = new BangDep("ID", "Mã CV", "Công việc", "Sự kiện", "Nhân sự phụ trách",
                "Ưu tiên", "Hạn chót", "Tiến độ", "Trạng thái", "Thao tác");
        bang.setRowHeight(52);
        bang.anCot(0);
        bang.coDinhCot(1, 78);    // Mã CV
        bang.doRongCot(2, 230);   // Công việc (2 dòng)
        bang.doRongCot(3, 165);   // Sự kiện
        bang.doRongCot(4, 155);   // Nhân sự
        bang.coDinhCot(5, 96);    // Ưu tiên
        bang.coDinhCot(6, 100);   // Hạn chót
        bang.coDinhCot(7, 130);   // Tiến độ
        bang.coDinhCot(8, 132);   // Trạng thái
        bang.coDinhCot(9, 156);   // Thao tác (4 nút)

        // Cột 2: Tên công việc (đậm) + Mô tả ngắn (mờ)
        bang.cotHaiDong(2);

        // Cột 5: Badge Mức ưu tiên
        bang.getColumnModel().getColumn(5).setCellRenderer((t, v, s, f, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
            p.setBackground(bang.nenDong(r, s));
            String text = v == null ? "" : v.toString();
            p.add(taoPill(text, mucDoMau(text), false));
            return p;
        });

        // Cột 6: Hạn chót (tô đỏ nếu quá hạn & chưa hoàn thành)
        bang.getColumnModel().getColumn(6).setCellRenderer((t, v, s, f, r, c) -> {
            JLabel l = new JLabel(v == null ? "—" : v.toString());
            l.setBorder(new EmptyBorder(0, 10, 0, 6));
            l.setOpaque(true);
            l.setBackground(bang.nenDong(r, s));
            int modelRow = bang.convertRowIndexToModel(r);
            Object ttVal = bang.getModelBang().getValueAt(modelRow, 8);
            boolean quaHan = kiemTraQuaHan(v == null ? "" : v.toString(), ttVal == null ? "" : ttVal.toString());
            l.setFont(quaHan ? Theme.BODY_BOLD : Theme.BODY);
            l.setForeground(quaHan ? Theme.DANGER : Theme.TEXT_2);
            return l;
        });

        // Cột 7: Thanh tiến độ + %
        bang.getColumnModel().getColumn(7).setCellRenderer((t, v, s, f, r, c) -> {
            JPanel p = new JPanel(new BorderLayout(8, 0));
            p.setBackground(bang.nenDong(r, s));
            p.setBorder(new EmptyBorder(0, 8, 0, 8));
            int pct = (v instanceof Number n) ? n.intValue() : 0;
            Color mau = pct >= 100 ? Theme.SUCCESS : pct >= 50 ? Theme.PRIMARY : Theme.WARNING;
            p.add(UI.thanhTienDo(pct / 100.0, mau), BorderLayout.CENTER);
            JLabel lbl = new JLabel(pct + "%");
            lbl.setFont(Theme.SMALL_BOLD);
            lbl.setForeground(Theme.TEXT_2);
            lbl.setPreferredSize(new Dimension(34, 16));
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            p.add(lbl, BorderLayout.EAST);
            return p;
        });

        // Cột 8: Badge Trạng thái (giữ nguyên đúng tên tiếng Việt)
        bang.getColumnModel().getColumn(8).setCellRenderer((t, v, s, f, r, c) -> {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 14));
            p.setBackground(bang.nenDong(r, s));
            String text = v == null ? "" : v.toString();
            p.add(taoPill(text, trangThaiMau(text), true));
            return p;
        });

        // Cột 9: 4 nút thao tác tương ứng Use Case
        bang.cotThaoTac(9, List.of(
                new BangDep.NutThaoTac("sua", null, Icons.EDIT, "Sửa kế hoạch công việc"),
                new BangDep.NutThaoTac("phanCong", null, Icons.USER_PLUS, "Phân công nhiệm vụ"),
                new BangDep.NutThaoTac("trangThai", null, Icons.CHECK, "Cập nhật trạng thái & tiến độ"),
                new BangDep.NutThaoTac("xoa", null, Icons.TRASH, "Xóa công việc")
        ), null, (ma, dongModel) -> {
            long id = bang.idDong(bang.convertRowIndexToView(dongModel));
            switch (ma) {
                case "sua" -> hienFormSua(id);
                case "phanCong" -> hienFormPhanCong(id);
                case "trangThai" -> hienDoiTrangThai(id);
                case "xoa" -> xoaCongViec(id);
            }
        });

        JScrollPane bangScroll = bang.trongKhungCuon();
        bangScroll.setPreferredSize(new Dimension(800, 420));
        cardBang.add(bangScroll, BorderLayout.CENTER);

        // Phân trang
        phanTrang = new UI.PhanTrang(trang -> {
            trangHienTai = trang;
            hienThiTrang();
        });
        phanTrang.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
                new EmptyBorder(10, 16, 10, 16)
        ));
        cardBang.add(phanTrang, BorderLayout.SOUTH);

        noiDung.add(cardBang);

        mainPanel.add(UI.cuon(noiDung), BorderLayout.CENTER);
    }

    // ==================== TẢI DỮ LIỆU TỪ BACKEND ====================

    public void taiLai() {
        taiDanhMucHoTro();
        taiDanhSachCongViec();
        taiThongKe();
    }

    private void taiDanhSachCongViec() {
        Api.chayNen(mainPanel,
                () -> Api.get("/api/cong-viec/danh-sach"),
                result -> {
                    danhSachGoc = Api.arr(result);
                    capNhatDemTrenTab();
                    capNhatTienDoTrungBinhTuDanhSach();
                    hienThiTrang();
                });
    }

    private void capNhatTienDoTrungBinhTuDanhSach() {
        if (danhSachGoc.isEmpty()) {
            tienDoTrungBinh = 0;
        } else {
            long tong = 0;
            for (JsonElement el : danhSachGoc) {
                tong += Api.n(el.getAsJsonObject(), "tienDo");
            }
            tienDoTrungBinh = (int) Math.round((double) tong / danhSachGoc.size());
        }
        lblTienDoTB.setText(tienDoTrungBinh + "%");
        pnlThanhTienDoTB.repaint();
    }

    private void taiThongKe() {
        Api.chayNen(mainPanel,
                () -> Api.get("/api/cong-viec/thong-ke"),
                result -> {
                    JsonObject o = Api.obj(result);
                    cardTong.setSo(Api.n(o, "tongCong"));
                    cardDang.setSo(Api.n(o, "dangThucHien"));
                    cardXong.setSo(Api.n(o, "hoanThanh"));
                    cardQuaHan.setSo(Api.n(o, "quaHan"));
                    if (o.has("tienDoTrungBinh") && !o.get("tienDoTrungBinh").isJsonNull()) {
                        tienDoTrungBinh = (int) Api.n(o, "tienDoTrungBinh");
                        lblTienDoTB.setText(tienDoTrungBinh + "%");
                        pnlThanhTienDoTB.repaint();
                    }
                });
    }

    private void taiDanhMucHoTro() {
        // Tải danh sách sự kiện ở luồng nền (không bật hộp thoại lỗi nếu endpoint chưa sẵn sàng)
        new SwingWorker<JsonArray, Void>() {
            @Override
            protected JsonArray doInBackground() {
                try {
                    return Api.arr(Api.get("/api/cong-viec/su-kien"));
                } catch (Exception ex) {
                    try {
                        return Api.arr(Api.get("/api/su-kien/danh-sach"));
                    } catch (Exception ignored) {
                        return new JsonArray();
                    }
                }
            }

            @Override
            protected void done() {
                try {
                    JsonArray arr = get();
                    SuKienOption dangChon = (SuKienOption) cbLocSuKien.getSelectedItem();
                    Long idDangChon = dangChon == null ? null : dangChon.id;

                    dsSuKien.clear();
                    dangCapNhatBoLoc = true;
                    cbLocSuKien.removeAllItems();
                    cbLocSuKien.addItem(new SuKienOption(null, "Tất cả sự kiện"));
                    SuKienOption chonLai = null;
                    for (JsonElement el : arr) {
                        JsonObject o = el.getAsJsonObject();
                        SuKienOption opt = new SuKienOption(Api.n(o, "id"), Api.s(o, "tenSuKien"));
                        dsSuKien.add(opt);
                        cbLocSuKien.addItem(opt);
                        if (idDangChon != null && idDangChon.equals(opt.id)) {
                            chonLai = opt;
                        }
                    }
                    if (chonLai != null) cbLocSuKien.setSelectedItem(chonLai);
                    dangCapNhatBoLoc = false;
                } catch (Exception ignored) {
                }
            }
        }.execute();

        // Tải danh sách cán bộ phục vụ Phân công nhiệm vụ
        new SwingWorker<JsonArray, Void>() {
            @Override
            protected JsonArray doInBackground() {
                try {
                    return Api.arr(Api.get("/api/cong-viec/nhan-su"));
                } catch (Exception ex) {
                    return new JsonArray();
                }
            }

            @Override
            protected void done() {
                try {
                    JsonArray arr = get();
                    dsNhanSu.clear();
                    for (JsonElement el : arr) {
                        JsonObject o = el.getAsJsonObject();
                        String ht = Api.s(o, "hienThi");
                        if (!ht.isEmpty()) dsNhanSu.add(ht);
                    }
                } catch (Exception ignored) {
                }
            }
        }.execute();
    }

    // ==================== LỌC & HIỂN THỊ BẢNG ====================

    private void capNhatDemTrenTab() {
        int[] dem = new int[TRANG_THAI_LIST.length];
        dem[0] = danhSachGoc.size();
        for (JsonElement el : danhSachGoc) {
            String tt = Api.s(el.getAsJsonObject(), "trangThai");
            for (int i = 1; i < TRANG_THAI_LIST.length; i++) {
                if (TRANG_THAI_LIST[i].equals(tt)) {
                    dem[i]++;
                    break;
                }
            }
        }
        for (int i = 0; i < TRANG_THAI_LIST.length; i++) {
            tabTrangThai.doiTen(i, TRANG_THAI_LIST[i] + " (" + dem[i] + ")");
        }
    }

    private JsonArray danhSachDaLoc() {
        String tuKhoa = oTim.getText().trim().toLowerCase();
        int idxTab = tabTrangThai.getChon();
        String locTT = idxTab >= 0 && idxTab < TRANG_THAI_LIST.length ? TRANG_THAI_LIST[idxTab] : "Tất cả";
        SuKienOption locSK = (SuKienOption) cbLocSuKien.getSelectedItem();

        JsonArray kq = new JsonArray();
        for (JsonElement e : danhSachGoc) {
            JsonObject o = e.getAsJsonObject();

            if (!"Tất cả".equals(locTT) && !locTT.equals(Api.s(o, "trangThai"))) continue;
            if (chkCuaToi.isSelected() && !laViecCuaToi(Api.s(o, "nhanSu"))) continue;

            if (locSK != null && locSK.id != null) {
                long skId = Api.n(o, "suKienId");
                String tenSk = Api.s(o, "tenSuKien");
                if (skId != locSK.id && !locSK.ten.equalsIgnoreCase(tenSk)) continue;
            }

            if (!tuKhoa.isEmpty()) {
                String text = (Api.s(o, "maCv") + " " + Api.s(o, "tenCongViec") + " " +
                        Api.s(o, "moTa") + " " + Api.s(o, "nhanSu") + " " + Api.s(o, "tenSuKien")).toLowerCase();
                if (!text.contains(tuKhoa)) continue;
            }
            kq.add(o);
        }
        return kq;
    }

    /** nhanSu lưu dạng "Họ tên (canbo01)" hoặc "canbo01". */
    private boolean laViecCuaToi(String nhanSu) {
        String ns = nhanSu.toLowerCase();
        String ten = tenDangNhap.toLowerCase();
        return ns.equals(ten) || ns.contains("(" + ten + ")");
    }

    private void locVaHienThi() {
        trangHienTai = 0;
        hienThiTrang();
    }

    private void hienThiTrang() {
        JsonArray daLoc = danhSachDaLoc();
        int tong = daLoc.size();
        int tongTrang = Math.max(1, (int) Math.ceil((double) tong / PAGE_SIZE));
        if (trangHienTai >= tongTrang) trangHienTai = tongTrang - 1;
        if (trangHienTai < 0) trangHienTai = 0;

        bang.xoaHet();
        int tu = trangHienTai * PAGE_SIZE;
        int den = Math.min(tu + PAGE_SIZE, tong);
        for (int i = tu; i < den; i++) {
            JsonObject o = daLoc.get(i).getAsJsonObject();
            String tenCv = Api.s(o, "tenCongViec");
            String moTa = Api.s(o, "moTa");
            String nguoiGiao = Api.s(o, "nguoiGiao");
            String dong2 = "admin".equalsIgnoreCase(nguoiGiao) ? "[Admin giao] " + moTa : moTa;
            String cotCongViec = dong2.isEmpty() ? tenCv : tenCv + "\n" + rutGon(dong2, 56);
            String tenSk = Api.s(o, "tenSuKien");
            String nhanSu = Api.s(o, "nhanSu");
            String hanChot = Api.s(o, "hanChot");

            bang.themDong(
                    Api.n(o, "id"),
                    Api.s(o, "maCv"),
                    cotCongViec,
                    tenSk.isEmpty() ? "—" : tenSk,
                    nhanSu.isEmpty() ? "Chưa phân công" : nhanSu,
                    Api.s(o, "mucDoUuTien"),
                    hanChot.isEmpty() ? "—" : hanChot,
                    (int) Api.n(o, "tienDo"),
                    Api.s(o, "trangThai"),
                    ""
            );
        }
        phanTrang.capNhat(trangHienTai, tongTrang, tong, PAGE_SIZE, "công việc");
    }

    // ==================== USE CASE 1: LẬP KẾ HOẠCH SỰ KIỆN (THÊM / SỬA) ====================

    private void hienFormThem() {
        HopThoai.FormDialog d = new HopThoai.FormDialog(mainPanel,
                "Lập kế hoạch công việc",
                "Tạo đầu mục công việc mới gắn với sự kiện được phân công", 580);

        UI.TextFieldDep tfMaCv = UI.oNhap("Để trống để tự sinh (VD: CV009)");
        UI.TextFieldDep tfTen = UI.oNhap("VD: Chuẩn bị hội trường, kịch bản MC...");

        JComboBox<SuKienOption> cbSuKien = taoComboSuKien(null, null);
        JComboBox<String> cbNhanSu = taoComboNhanSu("");
        JComboBox<String> cbUuTien = UI.hopChon(UU_TIEN_LIST, null);
        UI.TextFieldDep tfHanChot = UI.oNhap("yyyy-MM-dd (VD: " + LocalDate.now().plusDays(7) + ")");
        JTextArea taMoTa = UI.vungChu(3);

        d.truong("Mã công việc", tfMaCv, false);
        d.truong("Mức độ ưu tiên", cbUuTien, false);
        d.truong("Tên công việc *", tfTen, true);
        d.truong("Sự kiện liên quan", cbSuKien, false);
        d.truong("Hạn chót (yyyy-MM-dd)", tfHanChot, false);
        d.truong("Phân công nhân sự", cbNhanSu, true);
        d.truong("Mô tả kế hoạch chi tiết", UI.cuonVungChu(taMoTa), true);

        UI.FlatButton btnHuy = UI.nut("Hủy", UI.Kieu.VIEN);
        btnHuy.addActionListener(e -> d.dispose());

        UI.FlatButton btnLuu = UI.nut("Lưu kế hoạch", Icons.CHECK, UI.Kieu.CHINH);
        btnLuu.addActionListener(e -> {
            if (tfTen.getText().trim().isEmpty()) {
                HopThoai.loi(mainPanel, "Vui lòng nhập tên công việc!");
                return;
            }
            String hc = tfHanChot.getText().trim();
            if (!hc.isEmpty() && !hopLeNgay(hc)) {
                HopThoai.loi(mainPanel, "Hạn chót không hợp lệ! Vui lòng nhập theo định dạng yyyy-MM-dd (VD: 2026-12-01).");
                return;
            }

            JsonObject body = new JsonObject();
            if (!tfMaCv.getText().trim().isEmpty()) {
                body.addProperty("maCv", tfMaCv.getText().trim());
            }
            body.addProperty("tenCongViec", tfTen.getText().trim());
            SuKienOption skChon = (SuKienOption) cbSuKien.getSelectedItem();
            if (skChon != null && skChon.id != null) {
                body.addProperty("suKienId", skChon.id);
                body.addProperty("tenSuKien", skChon.ten);
            }
            body.addProperty("nhanSu", layGiaTriCombo(cbNhanSu));
            body.addProperty("mucDoUuTien", (String) cbUuTien.getSelectedItem());
            body.addProperty("moTa", taMoTa.getText().trim());
            body.addProperty("trangThai", "Chưa bắt đầu");
            body.addProperty("tienDo", 0);
            if (!hc.isEmpty()) body.addProperty("hanChot", hc);

            Api.chayNen(mainPanel,
                    () -> Api.post("/api/cong-viec/them", body),
                    result -> {
                        d.dispose();
                        HopThoai.thanhCong(mainPanel, "Đã lập kế hoạch công việc thành công!");
                        taiLai();
                    });
        });

        d.nut(btnHuy);
        d.nut(btnLuu);
        d.hien();
    }

    private void hienFormSua(long id) {
        JsonObject data = timTheoId(id);
        if (data == null) return;

        HopThoai.FormDialog d = new HopThoai.FormDialog(mainPanel,
                "Cập nhật kế hoạch công việc",
                "Chỉnh sửa thông tin chi tiết công việc " + Api.s(data, "maCv"), 580);

        UI.TextFieldDep tfMaCv = UI.oNhap(null);
        tfMaCv.setText(Api.s(data, "maCv"));

        UI.TextFieldDep tfTen = UI.oNhap(null);
        tfTen.setText(Api.s(data, "tenCongViec"));

        Long skId = data.has("suKienId") && !data.get("suKienId").isJsonNull() ? Api.n(data, "suKienId") : null;
        JComboBox<SuKienOption> cbSuKien = taoComboSuKien(skId, Api.s(data, "tenSuKien"));
        JComboBox<String> cbNhanSu = taoComboNhanSu(Api.s(data, "nhanSu"));

        JComboBox<String> cbUuTien = UI.hopChon(UU_TIEN_LIST, null);
        cbUuTien.setSelectedItem(Api.s(data, "mucDoUuTien"));

        UI.TextFieldDep tfHanChot = UI.oNhap("yyyy-MM-dd");
        tfHanChot.setText(Api.s(data, "hanChot"));

        JComboBox<String> cbTrangThai = UI.hopChon(TRANG_THAI_EDIT, null);
        cbTrangThai.setSelectedItem(Api.s(data, "trangThai"));

        JSlider slTienDo = taoSliderTienDo((int) Api.n(data, "tienDo"));
        JLabel lblPct = UI.nhan(slTienDo.getValue() + "%", Theme.BODY_BOLD, Theme.PRIMARY);
        slTienDo.addChangeListener(ev -> lblPct.setText(slTienDo.getValue() + "%"));
        cbTrangThai.addActionListener(ev -> {
            if ("Hoàn thành".equals(cbTrangThai.getSelectedItem())) slTienDo.setValue(100);
        });

        JPanel pnlSlider = new JPanel(new BorderLayout(8, 0));
        pnlSlider.setOpaque(false);
        pnlSlider.add(slTienDo, BorderLayout.CENTER);
        pnlSlider.add(lblPct, BorderLayout.EAST);

        JTextArea taMoTa = UI.vungChu(3);
        taMoTa.setText(Api.s(data, "moTa"));

        d.truong("Mã công việc *", tfMaCv, false);
        d.truong("Mức độ ưu tiên", cbUuTien, false);
        d.truong("Tên công việc *", tfTen, true);
        d.truong("Sự kiện liên quan", cbSuKien, false);
        d.truong("Hạn chót (yyyy-MM-dd)", tfHanChot, false);
        d.truong("Nhân sự phụ trách", cbNhanSu, true);
        d.truong("Trạng thái", cbTrangThai, false);
        d.truong("Tiến độ thực hiện", pnlSlider, false);
        d.truong("Mô tả chi tiết", UI.cuonVungChu(taMoTa), true);

        UI.FlatButton btnHuy = UI.nut("Hủy", UI.Kieu.VIEN);
        btnHuy.addActionListener(e -> d.dispose());

        UI.FlatButton btnLuu = UI.nut("Lưu thay đổi", Icons.CHECK, UI.Kieu.CHINH);
        btnLuu.addActionListener(e -> {
            if (tfMaCv.getText().trim().isEmpty() || tfTen.getText().trim().isEmpty()) {
                HopThoai.loi(mainPanel, "Vui lòng nhập mã công việc và tên công việc!");
                return;
            }
            String hc = tfHanChot.getText().trim();
            if (!hc.isEmpty() && !hopLeNgay(hc)) {
                HopThoai.loi(mainPanel, "Hạn chót không hợp lệ! Nhập theo định dạng yyyy-MM-dd.");
                return;
            }

            JsonObject body = new JsonObject();
            body.addProperty("maCv", tfMaCv.getText().trim());
            body.addProperty("tenCongViec", tfTen.getText().trim());
            SuKienOption skChon = (SuKienOption) cbSuKien.getSelectedItem();
            if (skChon != null && skChon.id != null) {
                body.addProperty("suKienId", skChon.id);
                body.addProperty("tenSuKien", skChon.ten);
            } else if (skChon != null && !skChon.ten.startsWith("--")) {
                body.addProperty("tenSuKien", skChon.ten);
            }
            body.addProperty("nhanSu", layGiaTriCombo(cbNhanSu));
            body.addProperty("mucDoUuTien", (String) cbUuTien.getSelectedItem());
            body.addProperty("moTa", taMoTa.getText().trim());
            body.addProperty("trangThai", (String) cbTrangThai.getSelectedItem());
            body.addProperty("tienDo", slTienDo.getValue());
            if (!hc.isEmpty()) body.addProperty("hanChot", hc);

            Api.chayNen(mainPanel,
                    () -> Api.put("/api/cong-viec/cap-nhat/" + id, body),
                    result -> {
                        d.dispose();
                        HopThoai.thanhCong(mainPanel, "Đã cập nhật công việc thành công!");
                        taiLai();
                    });
        });

        d.nut(btnHuy);
        d.nut(btnLuu);
        d.hien();
    }

    // ==================== USE CASE 2: PHÂN CÔNG NHIỆM VỤ ====================

    private void hienFormPhanCong(long id) {
        JsonObject cv = timTheoId(id);
        if (cv == null) return;

        HopThoai.FormDialog d = new HopThoai.FormDialog(mainPanel,
                "Phân công nhiệm vụ",
                "Giao nhiệm vụ [" + Api.s(cv, "maCv") + "] cho cán bộ phụ trách", 480);

        d.truong("Công việc", HopThoai.oChiXem(Api.s(cv, "tenCongViec")), true);
        d.truong("Sự kiện", HopThoai.oChiXem(Api.s(cv, "tenSuKien")), true);

        JComboBox<String> cbNhanSu = taoComboNhanSu(Api.s(cv, "nhanSu"));
        JComboBox<String> cbUuTien = UI.hopChon(UU_TIEN_LIST, null);
        cbUuTien.setSelectedItem(Api.s(cv, "mucDoUuTien"));

        JTextArea taGhiChu = UI.vungChu(3);
        taGhiChu.setText(Api.s(cv, "moTa"));

        d.truong("Cán bộ phụ trách *", cbNhanSu, true);
        d.truong("Mức độ ưu tiên", cbUuTien, true);
        d.truong("Ghi chú / Yêu cầu nhiệm vụ", UI.cuonVungChu(taGhiChu), true);

        UI.FlatButton btnHuy = UI.nut("Hủy", UI.Kieu.VIEN);
        btnHuy.addActionListener(e -> d.dispose());

        UI.FlatButton btnXacNhan = UI.nut("Xác nhận phân công", Icons.USER_PLUS, UI.Kieu.CHINH);
        btnXacNhan.addActionListener(e -> {
            String ns = layGiaTriCombo(cbNhanSu);
            if (ns.isEmpty()) {
                HopThoai.loi(mainPanel, "Vui lòng chọn hoặc nhập tên nhân sự phụ trách!");
                return;
            }
            JsonObject body = new JsonObject();
            body.addProperty("nhanSu", ns);
            body.addProperty("mucDoUuTien", (String) cbUuTien.getSelectedItem());
            body.addProperty("moTa", taGhiChu.getText().trim());

            Api.chayNen(mainPanel,
                    () -> Api.put("/api/cong-viec/phan-cong/" + id, body),
                    result -> {
                        d.dispose();
                        HopThoai.thanhCong(mainPanel, "Đã phân công nhiệm vụ cho " + ns + "!");
                        taiLai();
                    });
        });

        d.nut(btnHuy);
        d.nut(btnXacNhan);
        d.hien();
    }

    // ==================== USE CASE 3 & 4: THEO DÕI TIẾN ĐỘ & CẬP NHẬT TRẠNG THÁI ====================

    private void hienDoiTrangThai(long id) {
        JsonObject cv = timTheoId(id);
        if (cv == null) return;

        HopThoai.FormDialog d = new HopThoai.FormDialog(mainPanel,
                "Cập nhật trạng thái & tiến độ",
                "[" + Api.s(cv, "maCv") + "] " + Api.s(cv, "tenCongViec"), 450);

        JComboBox<String> cbTT = UI.hopChon(TRANG_THAI_EDIT, null);
        cbTT.setSelectedItem(Api.s(cv, "trangThai"));

        JSlider slTienDo = taoSliderTienDo((int) Api.n(cv, "tienDo"));
        JLabel lblPct = UI.nhan(slTienDo.getValue() + "%", Theme.H2, Theme.PRIMARY);
        lblPct.setPreferredSize(new Dimension(50, 24));
        lblPct.setHorizontalAlignment(SwingConstants.RIGHT);
        slTienDo.addChangeListener(ev -> {
            int val = slTienDo.getValue();
            lblPct.setText(val + "%");
            if (val == 100 && !"Hoàn thành".equals(cbTT.getSelectedItem())) {
                cbTT.setSelectedItem("Hoàn thành");
            } else if (val > 0 && val < 100 && "Chưa bắt đầu".equals(cbTT.getSelectedItem())) {
                cbTT.setSelectedItem("Đang thực hiện");
            }
        });

        cbTT.addActionListener(e -> {
            String sel = (String) cbTT.getSelectedItem();
            if ("Hoàn thành".equals(sel)) {
                slTienDo.setValue(100);
            } else if ("Chưa bắt đầu".equals(sel)) {
                slTienDo.setValue(0);
            }
        });

        JPanel pnlSlider = new JPanel(new BorderLayout(10, 0));
        pnlSlider.setOpaque(false);
        pnlSlider.add(slTienDo, BorderLayout.CENTER);
        pnlSlider.add(lblPct, BorderLayout.EAST);

        d.truong("Trạng thái công việc", cbTT, true);
        d.truong("Tiến độ hoàn thành (%)", pnlSlider, true);

        UI.FlatButton btnHuy = UI.nut("Hủy", UI.Kieu.VIEN);
        btnHuy.addActionListener(e -> d.dispose());

        UI.FlatButton btnLuu = UI.nut("Cập nhật", Icons.CHECK, UI.Kieu.THANH_CONG);
        btnLuu.addActionListener(e -> {
            String tt = (String) cbTT.getSelectedItem();
            int td = slTienDo.getValue();
            String url = "/api/cong-viec/cap-nhat-trang-thai/" + id
                    + "?trangThai=" + Api.e(tt) + "&tienDo=" + td;
            Api.chayNen(mainPanel,
                    () -> Api.put(url, null),
                    result -> {
                        d.dispose();
                        HopThoai.thanhCong(mainPanel, "Đã cập nhật trạng thái: " + tt + " (" + td + "%)");
                        taiLai();
                    });
        });

        d.nut(btnHuy);
        d.nut(btnLuu);
        d.hien();
    }

    // ==================== XÓA CÔNG VIỆC ====================

    private void xoaCongViec(long id) {
        JsonObject cv = timTheoId(id);
        String ten = cv == null ? "" : " \"" + Api.s(cv, "tenCongViec") + "\"";
        if (HopThoai.xacNhan(mainPanel, "Xóa công việc",
                "Bạn có chắc chắn muốn xóa công việc" + ten + "? Hành động này không thể hoàn tác.",
                "Xóa", UI.Kieu.NGUY_HIEM)) {
            Api.chayNen(mainPanel,
                    () -> Api.delete("/api/cong-viec/xoa/" + id),
                    result -> {
                        HopThoai.thanhCong(mainPanel, "Đã xóa công việc!");
                        taiLai();
                    });
        }
    }

    // ==================== TIỆN ÍCH HIỂN THỊ & HELPER ====================

    private JsonObject timTheoId(long id) {
        for (JsonElement e : danhSachGoc) {
            JsonObject o = e.getAsJsonObject();
            if (Api.n(o, "id") == id) return o;
        }
        return null;
    }

    private JComboBox<SuKienOption> taoComboSuKien(Long selectedId, String fallbackTen) {
        List<SuKienOption> opts = new ArrayList<>();
        opts.add(new SuKienOption(null, "-- Chọn sự kiện --"));
        opts.addAll(dsSuKien);
        SuKienOption chon = opts.get(0);
        if (selectedId != null) {
            for (SuKienOption o : dsSuKien) {
                if (selectedId.equals(o.id)) {
                    chon = o;
                    break;
                }
            }
        } else if (fallbackTen != null && !fallbackTen.isBlank()) {
            boolean found = false;
            for (SuKienOption o : dsSuKien) {
                if (fallbackTen.equalsIgnoreCase(o.ten)) {
                    chon = o;
                    found = true;
                    break;
                }
            }
            if (!found) {
                chon = new SuKienOption(null, fallbackTen);
                opts.add(chon);
            }
        }
        JComboBox<SuKienOption> cb = UI.hopChon(opts.toArray(new SuKienOption[0]), null);
        cb.setSelectedItem(chon);
        return cb;
    }

    private JComboBox<String> taoComboNhanSu(String giaTriHienTai) {
        List<String> items = new ArrayList<>();
        if (giaTriHienTai != null && !giaTriHienTai.isBlank() && !dsNhanSu.contains(giaTriHienTai)) {
            items.add(giaTriHienTai);
        }
        items.addAll(dsNhanSu);
        JComboBox<String> cb = new JComboBox<>(items.toArray(new String[0]));
        cb.setEditable(true);
        cb.setFont(Theme.BODY);
        cb.setForeground(Theme.TEXT);
        cb.setBackground(Color.WHITE);
        if (giaTriHienTai != null && !giaTriHienTai.isBlank()) {
            cb.setSelectedItem(giaTriHienTai);
        } else {
            cb.setSelectedItem("");
        }
        return cb;
    }

    private String layGiaTriCombo(JComboBox<String> cb) {
        Object val = cb.isEditable() ? cb.getEditor().getItem() : cb.getSelectedItem();
        return val == null ? "" : val.toString().trim();
    }

    private JSlider taoSliderTienDo(int giaTri) {
        JSlider sl = new JSlider(0, 100, Math.max(0, Math.min(100, giaTri)));
        sl.setOpaque(false);
        sl.setPaintLabels(true);
        sl.setPaintTicks(true);
        sl.setMajorTickSpacing(25);
        sl.setFont(Theme.SMALL);
        return sl;
    }

    private static JComponent taoPill(String text, Color[] mau, boolean coCham) {
        JComponent pill = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                FontMetrics fm = getFontMetrics(Theme.SMALL);
                int w = fm.stringWidth(text) + 18 + (coCham ? 11 : 0);
                return new Dimension(w, 24);
            }

            @Override
            protected void paintComponent(Graphics g) {
                if (text.isEmpty()) return;
                Graphics2D g2 = Theme.muot(g);
                Dimension d = getPreferredSize();
                g2.setColor(mau[1]);
                g2.fill(new RoundRectangle2D.Double(0, 0, d.width, d.height, d.height, d.height));
                int x = 9;
                if (coCham) {
                    g2.setColor(mau[0]);
                    g2.fillOval(x, d.height / 2 - 3, 6, 6);
                    x += 11;
                }
                g2.setColor(mau[0]);
                g2.setFont(Theme.SMALL);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, x, (d.height - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        return pill;
    }

    private static Color[] trangThaiMau(String tt) {
        return switch (tt == null ? "" : tt) {
            case "Hoàn thành" -> new Color[]{Theme.SUCCESS, Theme.SUCCESS_SOFT};
            case "Đang thực hiện" -> new Color[]{Theme.PRIMARY, Theme.PRIMARY_SOFT};
            case "Chưa bắt đầu" -> new Color[]{Theme.WARNING, Theme.WARNING_SOFT};
            case "Tạm hoãn" -> new Color[]{Theme.DANGER, Theme.DANGER_SOFT};
            default -> new Color[]{Theme.TEXT_2, Theme.GRAY_SOFT};
        };
    }

    private static Color[] mucDoMau(String mucDo) {
        return switch (mucDo == null ? "" : mucDo) {
            case "Cao" -> new Color[]{Theme.DANGER, Theme.DANGER_SOFT};
            case "Trung bình" -> new Color[]{Theme.WARNING, Theme.WARNING_SOFT};
            case "Thấp" -> new Color[]{Theme.SUCCESS, Theme.SUCCESS_SOFT};
            default -> new Color[]{Theme.TEXT_2, Theme.GRAY_SOFT};
        };
    }

    private static boolean kiemTraQuaHan(String hanChot, String trangThai) {
        if (hanChot == null || hanChot.isBlank() || "—".equals(hanChot) || "Hoàn thành".equals(trangThai)) {
            return false;
        }
        try {
            return LocalDate.parse(hanChot).isBefore(LocalDate.now());
        } catch (Exception ex) {
            return false;
        }
    }

    private static boolean hopLeNgay(String s) {
        try {
            LocalDate.parse(s);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static String rutGon(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }

    private static final class SuKienOption {
        final Long id;
        final String ten;

        SuKienOption(Long id, String ten) {
            this.id = id;
            this.ten = ten == null ? "" : ten;
        }

        @Override
        public String toString() {
            return ten;
        }
    }

    // ==================== GETTER ====================

    public JPanel getMainPanel() {
        return mainPanel;
    }

    private void $$$setupUI$$$() {
    }
}
