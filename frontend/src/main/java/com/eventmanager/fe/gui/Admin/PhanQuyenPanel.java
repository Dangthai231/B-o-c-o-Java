package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * MÀN HÌNH "PHÂN QUYỀN NGƯỜI DÙNG" (frmCapQuyen + frmThayDoiQuyen + frmTraCuuQuyen)
 *
 *  Tab 1 - Cấp / thay đổi quyền:
 *     ┌ Danh sách tài khoản ┐ ┌ Thông tin tài khoản + vai trò [Đổi vai trò]      ┐
 *     │ [Tìm...]            │ │ ☑ Quyền theo vai trò (khóa, không bỏ được)        │
 *     │ ● Nguyễn Hải Nam    │ │ ☐ Quyền cấp thêm (tích để cấp)       [Lưu quyền] │
 *     └─────────────────────┘ └──────────────────────────────────────────────────┘
 *  Tab 2 - Tra cứu quyền: chọn 1 quyền -> xem ai đang có quyền đó
 *  Tab 3 - Danh mục quyền
 */
public class PhanQuyenPanel extends TrangAdmin {

    private static final String[] VAI_TRO_HIEN = {"Quản trị viên", "Cán bộ sự kiện", "Sinh viên"};
    private static final String[] VAI_TRO_MA = {"ADMIN", "CAN_BO", "HOC_SINH"};


    // Tab 1
    private final DefaultListModel<JsonObject> modelDanhSachTaiKhoan = new DefaultListModel<>();
    private final javax.swing.Timer henGio;
    private Long idChonSan = null;
    private List<JsonObject> danhMucQuyen = new ArrayList<>();
    private boolean dangNapQuyen = false; // chặn tra cứu thừa khi đang nạp combobox

    // Tab 2

    // Tab 3

    public PhanQuyenPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        $$$setupUI$$$();
        vungNoiDung.add(mainPanel, BorderLayout.CENTER);
        danhSachTaiKhoan.setModel(modelDanhSachTaiKhoan);
        tabChucNang.addChangeListener(e -> { if (tabChucNang.getSelectedIndex() == 1) traCuu(); });
        btnTraCuu.addActionListener(e -> traCuu());
        cboQuyen.addActionListener(e -> traCuu());
        txtTimTraCuu.addActionListener(e -> traCuu());
        hienTrongPhai();
        danhSachTaiKhoan.setFixedCellHeight(58);
        danhSachTaiKhoan.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        danhSachTaiKhoan.setCellRenderer((list, tk, idx, chon, focus) -> {
            JPanel o = new JPanel(new BorderLayout(10, 0));
            o.setBorder(new EmptyBorder(8, 10, 8, 10));
            o.setBackground(chon ? Theme.PRIMARY_SOFT : Color.WHITE);
            o.add(new JLabel(UI.avatar(UI.vietTat(Api.s(tk, "hoTen")), 36,
                    "BI_KHOA".equals(Api.s(tk, "trangThai")) ? Theme.MUTED : Theme.SIDEBAR_ACTIVE)), BorderLayout.WEST);
            JPanel chu = new JPanel(new GridLayout(2, 1));
            chu.setOpaque(false);
            chu.add(UI.nhan(Api.s(tk, "hoTen"), Theme.BODY_BOLD, Theme.TEXT));
            chu.add(UI.nhan(Api.s(tk, "tenVaiTro") + " - " + Api.s(tk, "tenDangNhap"), Theme.SMALL, Theme.MUTED));
            o.add(chu, BorderLayout.CENTER);
            return o;
        });
        danhSachTaiKhoan.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && danhSachTaiKhoan.getSelectedValue() != null) {
                taiQuyenCua(Api.n(danhSachTaiKhoan.getSelectedValue(), "id"));
            }
        });
        bangTraCuu.anCot(0);
        bangTraCuu.cotHaiDong(1);
        bangTraCuu.cotNhan(3);
        bangTraCuu.cotNhan(4);
        bangTraCuu.cotNhan(5);
        bangDanhMuc.doRongCot(0, 140);
        bangDanhMuc.doRongCot(1, 190);
        bangDanhMuc.doRongCot(2, 90);
        bangDanhMuc.doRongCot(3, 300);
        bangDanhMuc.cotNhan(2);
        henGio = new javax.swing.Timer(350, e -> taiDanhSachTk());
        henGio.setRepeats(false);
        txtTimTaiKhoan.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
        });

    }

    // ======================= TAB 1 =======================


    private void hienTrongPhai() {
        khungPhai.removeAll();
        JPanel rong = new JPanel(new GridBagLayout());
        rong.setBackground(Theme.TABLE_HEAD);
        rong.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        JLabel l = new JLabel("Chọn một tài khoản bên trái để xem và cấp quyền", Icons.of(Icons.SHIELD, 20, Theme.MUTED),
                SwingConstants.CENTER);
        l.setFont(Theme.BODY);
        l.setForeground(Theme.MUTED);
        rong.add(l);
        khungPhai.add(rong);
        khungPhai.revalidate();
        khungPhai.repaint();
    }

    private void taiQuyenCua(long id) {
        Api.chayNen(this, () -> Api.get("/api/admin/phan-quyen/tai-khoan/" + id), kq -> hienQuyen(Api.obj(kq)));
    }

    /** Hiện thông tin tài khoản + bảng tích quyền theo từng nhóm. */
    private void hienQuyen(JsonObject kq) {
        JsonObject tk = Api.obj(kq, "taiKhoan");
        long id = Api.n(tk, "id");
        Set<String> theoVaiTro = new HashSet<>();
        Api.arr(kq, "quyenTheoVaiTro").forEach(e -> theoVaiTro.add(e.getAsString()));
        Set<String> rieng = new HashSet<>();
        Api.arr(kq, "quyenRieng").forEach(e -> rieng.add(e.getAsString()));

        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);

        // --- Đầu: thông tin + đổi vai trò ---
        UI.RoundPanel dau = new UI.RoundPanel(new BorderLayout(14, 0), 12, Theme.TABLE_HEAD, Theme.BORDER);
        dau.setBorder(new EmptyBorder(12, 14, 12, 14));
        dau.add(new JLabel(UI.avatar(UI.vietTat(Api.s(tk, "hoTen")), 44, Theme.PRIMARY)), BorderLayout.WEST);
        JPanel chu = new JPanel(new GridLayout(2, 1, 0, 2));
        chu.setOpaque(false);
        chu.add(UI.nhan(Api.s(tk, "hoTen"), Theme.H2, Theme.TEXT));
        chu.add(UI.nhan(Api.s(tk, "email") + "  |  " + Api.s(tk, "maSo"), Theme.SMALL, Theme.MUTED));
        dau.add(chu, BorderLayout.CENTER);
        JComboBox<String> cboVaiTro = UI.hopChon(VAI_TRO_HIEN, null);
        cboVaiTro.setSelectedIndex(Math.max(0, Arrays.asList(VAI_TRO_MA).indexOf(Api.s(tk, "vaiTro"))));
        cboVaiTro.setPreferredSize(new Dimension(160, 36));
        UI.FlatButton doiVaiTro = UI.nut("Đổi vai trò", Icons.REFRESH, UI.Kieu.VIEN);
        doiVaiTro.addActionListener(e -> {
            String moi = VAI_TRO_MA[cboVaiTro.getSelectedIndex()];
            if (moi.equals(Api.s(tk, "vaiTro"))) {
                HopThoai.loi(this, "Hãy chọn một vai trò khác với vai trò hiện tại");
                return;
            }
            if (!HopThoai.xacNhan(this, "Thay đổi quyền", "Đổi vai trò của <b>" + Api.s(tk, "hoTen") + "</b> từ <b>"
                            + Api.s(tk, "tenVaiTro") + "</b> sang <b>" + cboVaiTro.getSelectedItem()
                            + "</b>?<br>Bộ quyền mặc định sẽ thay đổi theo vai trò mới.",
                    "Thay đổi quyền", UI.Kieu.CHINH)) return;
            Api.chayThaoTac(this, () -> Api.put("/api/admin/phan-quyen/tai-khoan/" + id + "/vai-tro",
                    Api.body("vaiTro", moi)), r -> {
                HopThoai.thanhCong(this, "Thay đổi quyền thành công");
                idChonSan = id;
                taiDanhSachTk();
            });
        });
        JPanel phaiDau = new JPanel(new GridBagLayout());
        phaiDau.setOpaque(false);
        phaiDau.add(UI.hang(8, cboVaiTro, doiVaiTro));
        dau.add(phaiDau, BorderLayout.EAST);
        p.add(dau, BorderLayout.NORTH);

        // --- Giữa: danh sách quyền theo nhóm ---
        JPanel luoi = new JPanel();
        luoi.setLayout(new BoxLayout(luoi, BoxLayout.Y_AXIS));
        luoi.setBackground(Color.WHITE);
        Map<String, JCheckBox> oTich = new LinkedHashMap<>();
        Map<String, List<JsonObject>> theoNhom = new LinkedHashMap<>();
        for (JsonObject q : danhMucQuyen) theoNhom.computeIfAbsent(Api.s(q, "nhom"), k -> new ArrayList<>()).add(q);
        for (Map.Entry<String, List<JsonObject>> nhom : theoNhom.entrySet()) {
            JLabel tenNhom = UI.nhan(nhom.getKey(), Theme.SMALL_BOLD, Theme.MUTED);
            tenNhom.setBorder(new EmptyBorder(10, 4, 4, 0));
            tenNhom.setAlignmentX(Component.LEFT_ALIGNMENT);
            luoi.add(tenNhom);
            JPanel hang = new JPanel(new GridLayout(0, 2, 10, 6));
            hang.setOpaque(false);
            hang.setAlignmentX(Component.LEFT_ALIGNMENT);
            for (JsonObject q : nhom.getValue()) {
                String ma = Api.s(q, "maQuyen");
                boolean macDinh = theoVaiTro.contains(ma);
                JCheckBox cb = new JCheckBox("<html><b>" + Api.s(q, "tenQuyen") + "</b>"
                        + (macDinh ? " <font color='#1E9E5A'>· theo vai trò</font>" : rieng.contains(ma)
                        ? " <font color='#1565D8'>· cấp riêng</font>" : "")
                        + "<br><font color='#7A889E'>" + Api.s(q, "moTa") + "</font></html>");
                cb.setFont(Theme.SMALL);
                cb.setForeground(Theme.TEXT);
                cb.setOpaque(true);
                cb.setBackground(macDinh ? Theme.SUCCESS_SOFT : rieng.contains(ma) ? Theme.PRIMARY_SOFT : Theme.TABLE_HEAD);
                cb.setBorder(new EmptyBorder(8, 8, 8, 8));
                cb.setBorderPainted(true);
                cb.setSelected(macDinh || rieng.contains(ma));
                cb.setEnabled(!macDinh); // quyền theo vai trò không bỏ tích được
                cb.setFocusPainted(false);
                oTich.put(ma, cb);
                hang.add(cb);
            }
            hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, hang.getPreferredSize().height));
            luoi.add(hang);
        }
        JPanel bocLuoi = new JPanel(new BorderLayout());
        bocLuoi.setBackground(Color.WHITE);
        bocLuoi.setBorder(new EmptyBorder(0, 4, 8, 8));
        bocLuoi.add(luoi, BorderLayout.NORTH);
        JScrollPane sp = UI.cuon(bocLuoi);
        sp.getViewport().setOpaque(true);
        sp.getViewport().setBackground(Color.WHITE);
        p.add(sp, BorderLayout.CENTER);

        // --- Chân: chú thích + Lưu ---
        JPanel chan = new JPanel(new BorderLayout());
        chan.setOpaque(false);
        chan.add(UI.nhan("Xanh lá = quyền có sẵn theo vai trò.  Tích thêm ô khác để cấp quyền riêng.",
                Theme.SMALL, Theme.MUTED), BorderLayout.CENTER);
        UI.FlatButton luu = UI.nut("Cấp quyền", Icons.SHIELD, UI.Kieu.CHINH);
        luu.addActionListener(e -> {
            JsonArray ds = new JsonArray();
            oTich.forEach((ma, cb) -> {
                if (cb.isSelected() && cb.isEnabled()) ds.add(ma);
            });
            if (!HopThoai.xacNhan(this, "Cấp quyền", "Lưu <b>" + ds.size() + "</b> quyền cấp riêng cho <b>"
                    + Api.s(tk, "hoTen") + "</b>?", "Xác nhận", UI.Kieu.CHINH)) return;
            JsonObject body = new JsonObject();
            body.add("dsMaQuyen", ds);
            Api.chayThaoTac(this, () -> Api.put("/api/admin/phan-quyen/tai-khoan/" + id + "/quyen", body), r -> {
                HopThoai.thanhCong(this, "Cấp quyền thành công");
                hienQuyen(Api.obj(r));
            });
        });
        chan.add(luu, BorderLayout.EAST);
        p.add(chan, BorderLayout.SOUTH);

        khungPhai.removeAll();
        khungPhai.add(p);
        khungPhai.revalidate();
        khungPhai.repaint();
    }

    private void taiDanhSachTk() {
        String url = "/api/admin/tai-khoan?kichThuoc=100&tuKhoa=" + Api.e(txtTimTaiKhoan.getText().trim());
        Api.chayNen(this, () -> Api.get(url), kq -> {
            Long giuLai = idChonSan != null ? idChonSan
                    : danhSachTaiKhoan.getSelectedValue() != null ? Api.n(danhSachTaiKhoan.getSelectedValue(), "id") : null;
            idChonSan = null;
            modelDanhSachTaiKhoan.clear();
            int chon = -1;
            for (JsonElement el : Api.arr(Api.obj(kq), "noiDung")) {
                JsonObject o = el.getAsJsonObject();
                if (giuLai != null && Api.n(o, "id") == giuLai) chon = modelDanhSachTaiKhoan.size();
                modelDanhSachTaiKhoan.addElement(o);
            }
            if (chon >= 0) {
                danhSachTaiKhoan.setSelectedIndex(chon);
                danhSachTaiKhoan.ensureIndexIsVisible(chon);
                taiQuyenCua(giuLai);
            } else {
                hienTrongPhai();
            }
        });
    }

    /** Gọi từ màn Quản lý tài khoản: mở sẵn tài khoản cần phân quyền. */
    public void chonSan(long taiKhoanId) {
        idChonSan = taiKhoanId;
        txtTimTaiKhoan.setText("");
        tabChucNang.setSelectedIndex(0);
    }

    // ======================= TAB 2: TRA CỨU QUYỀN =======================


    private void traCuu() {
        if (dangNapQuyen) return;
        int i = cboQuyen.getSelectedIndex();
        if (i < 0 || i >= danhMucQuyen.size()) return;
        String ma = Api.s(danhMucQuyen.get(i), "maQuyen");
        String url = "/api/admin/phan-quyen/tra-cuu?maQuyen=" + Api.e(ma) + "&tuKhoa=" + Api.e(txtTimTraCuu.getText().trim());
        Api.chayNen(this, () -> Api.get(url), kq -> {
            JsonObject o = Api.obj(kq);
            List<String> vt = new ArrayList<>();
            Api.arr(o, "vaiTroMacDinh").forEach(e -> vt.add(e.getAsString()));
            JsonArray ds = Api.arr(o, "taiKhoan");
            lblQuyenMacDinh.setText(ds.isEmpty() ? "Không có dữ liệu: chưa tài khoản nào có quyền này"
                    : ds.size() + " tài khoản có quyền này.  Mặc định cho: " + (vt.isEmpty() ? "(không vai trò nào)" : String.join(", ", vt)));
            bangTraCuu.xoaHet();
            for (JsonElement el : ds) {
                JsonObject t = el.getAsJsonObject();
                bangTraCuu.themDong(Api.n(t, "id"), Api.s(t, "hoTen") + "\n" + Api.s(t, "tenDangNhap"), Api.s(t, "email"),
                        Api.s(t, "tenVaiTro"), Api.s(t, "nguon"),
                        "BI_KHOA".equals(Api.s(t, "trangThai")) ? "Bị khóa" : "Hoạt động");
            }
        });
    }

    // ======================= TAB 3: DANH MỤC QUYỀN =======================


    // ======================= TẢI DỮ LIỆU =======================
    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/phan-quyen/quyen"), kq -> {
            dangNapQuyen = true;
            danhMucQuyen = new ArrayList<>();
            int chonCu = Math.max(0, cboQuyen.getSelectedIndex());
            cboQuyen.removeAllItems();
            bangDanhMuc.xoaHet();
            for (JsonElement el : Api.arr(kq)) {
                JsonObject q = el.getAsJsonObject();
                danhMucQuyen.add(q);
                cboQuyen.addItem(Api.s(q, "tenQuyen"));
                bangDanhMuc.themDong(Api.s(q, "maQuyen"), Api.s(q, "tenQuyen"), Api.s(q, "nhom"),
                        Api.s(q, "moTa"), Api.s(q, "vaiTroMacDinh"));
            }
            if (cboQuyen.getItemCount() > chonCu) cboQuyen.setSelectedIndex(chonCu);
            dangNapQuyen = false;
            taiDanhSachTk();
            if (tabChucNang.getSelectedIndex() == 1) traCuu();
        });
    }

    // GUI Designer: bố cục nằm trong file .form cùng tên.
    private javax.swing.JPanel mainPanel;
    private javax.swing.JTabbedPane tabChucNang;
    private javax.swing.JTextField txtTimTaiKhoan;
    private javax.swing.JList<JsonObject> danhSachTaiKhoan;
    private javax.swing.JPanel khungPhai;
    private javax.swing.JComboBox<String> cboQuyen;
    private javax.swing.JTextField txtTimTraCuu;
    private javax.swing.JButton btnTraCuu;
    private javax.swing.JLabel lblQuyenMacDinh;
    private com.eventmanager.fe.gui.Admin.common.BangDep bangTraCuu;
    private com.eventmanager.fe.gui.Admin.common.BangDep bangDanhMuc;

    private javax.swing.JPanel pnlTieuDe;
    private javax.swing.JPanel pnlNoiDung;
    private javax.swing.JPanel pnlTimTaiKhoan;
    private javax.swing.JPanel pnlTraCuu;

    private javax.swing.JPanel pnlTruongTxtTimTaiKhoan;
    private javax.swing.JLabel lblNhanTxtTimTaiKhoan;
    private javax.swing.JPanel pnlTruongCboQuyen;
    private javax.swing.JLabel lblNhanCboQuyen;
    private javax.swing.JPanel pnlTruongTxtTimTraCuu;
    private javax.swing.JLabel lblNhanTxtTimTraCuu;

    private void $$$setupUI$$$() {
        createUIComponents();
        mainPanel = new javax.swing.JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 16, false, false));
        mainPanel.setBackground(new java.awt.Color(0xF5F7FA));
        pnlTieuDe = new javax.swing.JPanel();
        pnlTieuDe.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlTieuDe.setBackground(new java.awt.Color(0xF5F7FA));
        mainPanel.add(pnlTieuDe, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 0, null, null, null));
        javax.swing.JLabel panela238 = new javax.swing.JLabel();
        panela238.setFont(new java.awt.Font("Segoe UI", 1, 24));
        panela238.setForeground(new java.awt.Color(0x0B2B50));
        panela238.setText("Phân quyền người dùng");
        pnlTieuDe.add(panela238, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JLabel panela239 = new javax.swing.JLabel();
        panela239.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela239.setForeground(new java.awt.Color(0x627D98));
        panela239.setText("Cấp quyền, thay đổi vai trò và tra cứu quyền của từng tài khoản");
        pnlTieuDe.add(panela239, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        pnlNoiDung = new javax.swing.JPanel();
        pnlNoiDung.setLayout(new java.awt.BorderLayout(12, 16));
        pnlNoiDung.setBorder(javax.swing.BorderFactory.createEmptyBorder(14, 14, 14, 14));
        pnlNoiDung.setBackground(new java.awt.Color(0xFFFFFF));
        mainPanel.add(pnlNoiDung, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 7, null, null, null));
        tabChucNang = new javax.swing.JTabbedPane();
        pnlNoiDung.add(tabChucNang, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela224 = new javax.swing.JPanel();
        panela224.setLayout(new java.awt.BorderLayout(12, 16));
        panela224.setBackground(new java.awt.Color(0xFFFFFF));
        tabChucNang.addTab("Cấp / thay đổi quyền", panela224);
        pnlTimTaiKhoan = new javax.swing.JPanel();
        pnlTimTaiKhoan.setLayout(new java.awt.BorderLayout(12, 16));
        pnlTimTaiKhoan.setBackground(new java.awt.Color(0xFFFFFF));
        pnlTimTaiKhoan.setPreferredSize(new java.awt.Dimension(260, 300));
        panela224.add(pnlTimTaiKhoan, java.awt.BorderLayout.WEST);
        txtTimTaiKhoan.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTimTaiKhoan.setToolTipText("Tên hoặc email tài khoản");
        txtTimTaiKhoan.setPreferredSize(new java.awt.Dimension(180, 32));
        txtTimTaiKhoan.setMinimumSize(new java.awt.Dimension(50, 32));
        pnlTruongTxtTimTaiKhoan = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongTxtTimTaiKhoan.setOpaque(false);
        lblNhanTxtTimTaiKhoan = new javax.swing.JLabel("Tìm tài khoản");
        lblNhanTxtTimTaiKhoan.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanTxtTimTaiKhoan.setForeground(new java.awt.Color(0x627D98));
        lblNhanTxtTimTaiKhoan.setLabelFor(txtTimTaiKhoan);
        pnlTruongTxtTimTaiKhoan.add(lblNhanTxtTimTaiKhoan, java.awt.BorderLayout.NORTH);
        pnlTruongTxtTimTaiKhoan.add(txtTimTaiKhoan, java.awt.BorderLayout.CENTER);
        pnlTimTaiKhoan.add(pnlTruongTxtTimTaiKhoan, java.awt.BorderLayout.NORTH);
        javax.swing.JScrollPane panela221 = new javax.swing.JScrollPane();
        pnlTimTaiKhoan.add(panela221, java.awt.BorderLayout.CENTER);
        danhSachTaiKhoan = new javax.swing.JList();
        danhSachTaiKhoan.setFont(new java.awt.Font("Segoe UI", 0, 13));
        panela221.setViewportView(danhSachTaiKhoan);
        khungPhai = new javax.swing.JPanel();
        khungPhai.setLayout(new java.awt.BorderLayout(12, 16));
        khungPhai.setBackground(new java.awt.Color(0xFFFFFF));
        panela224.add(khungPhai, java.awt.BorderLayout.CENTER);
        javax.swing.JPanel panela233 = new javax.swing.JPanel();
        panela233.setLayout(new java.awt.BorderLayout(12, 16));
        panela233.setBackground(new java.awt.Color(0xFFFFFF));
        tabChucNang.addTab("Tra cứu quyền", panela233);
        javax.swing.JPanel panela230 = new javax.swing.JPanel();
        panela230.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        panela230.setBackground(new java.awt.Color(0xFFFFFF));
        panela233.add(panela230, java.awt.BorderLayout.NORTH);
        pnlTraCuu = new javax.swing.JPanel();
        pnlTraCuu.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new java.awt.Insets(0, 0, 0, 0), 12, 10, false, false));
        pnlTraCuu.setBackground(new java.awt.Color(0xFFFFFF));
        panela230.add(pnlTraCuu, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        cboQuyen = new javax.swing.JComboBox();
        cboQuyen.setFont(new java.awt.Font("Segoe UI", 0, 13));
        cboQuyen.setToolTipText("Chọn quyền cần tra cứu");
        cboQuyen.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{}));
        cboQuyen.setPreferredSize(new java.awt.Dimension(175, 32));
        cboQuyen.setMinimumSize(new java.awt.Dimension(110, 32));
        pnlTruongCboQuyen = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongCboQuyen.setOpaque(false);
        lblNhanCboQuyen = new javax.swing.JLabel("Quyền cần tra cứu");
        lblNhanCboQuyen.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanCboQuyen.setForeground(new java.awt.Color(0x627D98));
        lblNhanCboQuyen.setLabelFor(cboQuyen);
        pnlTruongCboQuyen.add(lblNhanCboQuyen, java.awt.BorderLayout.NORTH);
        pnlTruongCboQuyen.add(cboQuyen, java.awt.BorderLayout.CENTER);
        pnlTraCuu.add(pnlTruongCboQuyen, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, 0, 1, 7, 0, null, null, null));
        txtTimTraCuu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        txtTimTraCuu.setToolTipText("Tên hoặc email người dùng");
        txtTimTraCuu.setPreferredSize(new java.awt.Dimension(180, 32));
        txtTimTraCuu.setMinimumSize(new java.awt.Dimension(50, 32));
        pnlTruongTxtTimTraCuu = new javax.swing.JPanel(new java.awt.BorderLayout(0, 4));
        pnlTruongTxtTimTraCuu.setOpaque(false);
        lblNhanTxtTimTraCuu = new javax.swing.JLabel("Lọc người có quyền");
        lblNhanTxtTimTraCuu.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblNhanTxtTimTraCuu.setForeground(new java.awt.Color(0x627D98));
        lblNhanTxtTimTraCuu.setLabelFor(txtTimTraCuu);
        pnlTruongTxtTimTraCuu.add(lblNhanTxtTimTraCuu, java.awt.BorderLayout.NORTH);
        pnlTruongTxtTimTraCuu.add(txtTimTraCuu, java.awt.BorderLayout.CENTER);
        pnlTraCuu.add(pnlTruongTxtTimTraCuu, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, 0, 1, 7, 0, null, null, null));
        btnTraCuu = new javax.swing.JButton();
        btnTraCuu.setFont(new java.awt.Font("Segoe UI", 0, 13));
        btnTraCuu.setBackground(new java.awt.Color(0x1565C0));
        btnTraCuu.setForeground(new java.awt.Color(0xFFFFFF));
        btnTraCuu.setFocusPainted(false);
        btnTraCuu.setContentAreaFilled(false);
        btnTraCuu.setOpaque(true);
        btnTraCuu.setBorderPainted(false);
        btnTraCuu.setPreferredSize(new java.awt.Dimension(130, 30));
        btnTraCuu.setText("Tra cứu");
        pnlTraCuu.add(btnTraCuu, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, 4, 0, 0, 0, null, null, null));
        lblQuyenMacDinh = new javax.swing.JLabel();
        lblQuyenMacDinh.setFont(new java.awt.Font("Segoe UI", 0, 12));
        lblQuyenMacDinh.setForeground(new java.awt.Color(0x243B53));
        lblQuyenMacDinh.setText(" ");
        panela230.add(lblQuyenMacDinh, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, 0, 3, 7, 3, null, null, null));
        javax.swing.JScrollPane panela232 = new javax.swing.JScrollPane();
        panela233.add(panela232, java.awt.BorderLayout.CENTER);
        panela232.setViewportView(bangTraCuu);
        javax.swing.JScrollPane panela235 = new javax.swing.JScrollPane();
        tabChucNang.addTab("Danh mục quyền", panela235);
        panela235.setViewportView(bangDanhMuc);
    }

    private void createUIComponents() {
        txtTimTraCuu = UI.oTimKiem("Tên hoặc email người dùng");
        txtTimTaiKhoan = UI.oTimKiem("Tên hoặc email tài khoản");
        bangTraCuu = new BangDep("id", "Họ và tên", "Email", "Vai trò", "Nguồn quyền", "Trạng thái");
        bangDanhMuc = new BangDep("Mã quyền", "Tên quyền", "Nhóm", "Mô tả", "Vai trò mặc định");
    }
}
