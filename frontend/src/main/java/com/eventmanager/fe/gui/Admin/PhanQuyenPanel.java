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

    private final UI.TabBar tabBar;
    private final CardLayout card = new CardLayout();
    private final JPanel vung = new JPanel(card);

    // Tab 1
    private final UI.TextFieldDep oTimTk = UI.oTimKiem("Tìm tài khoản...");
    private final DefaultListModel<JsonObject> modelTk = new DefaultListModel<>();
    private final JList<JsonObject> dsTk = new JList<>(modelTk);
    private final JPanel khungPhai = new JPanel(new BorderLayout());
    private final javax.swing.Timer henGio;
    private Long idChonSan = null;
    private List<JsonObject> danhMucQuyen = new ArrayList<>();
    private boolean dangNapQuyen = false; // chặn tra cứu thừa khi đang nạp combobox

    // Tab 2
    private final JComboBox<String> cbQuyen = UI.hopChon(new String[]{}, Icons.KEY);
    private final UI.TextFieldDep oTimTraCuu = UI.oTimKiem("Lọc theo tên, email...");
    private final JLabel lblMacDinh = UI.nhan(" ", Theme.SMALL, Theme.TEXT_2);
    private final BangDep bangTraCuu = new BangDep("id", "Họ và tên", "Email", "Vai trò", "Nguồn quyền", "Trạng thái");

    // Tab 3
    private final BangDep bangDanhMuc = new BangDep("Mã quyền", "Tên quyền", "Nhóm", "Mô tả", "Vai trò mặc định");

    public PhanQuyenPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        add(UI.tieuDeTrang("Phân quyền người dùng", "Cấp quyền, thay đổi vai trò và tra cứu quyền của từng tài khoản"),
                BorderLayout.NORTH);

        UI.RoundPanel the = UI.card(new BorderLayout(0, 14), 0);
        the.setBorder(new EmptyBorder(6, 16, 16, 16));
        tabBar = new UI.TabBar(i -> {
            card.show(vung, String.valueOf(i));
            if (i == 1) traCuu();
        }, "Cấp / thay đổi quyền", "Tra cứu quyền", "Danh mục quyền");
        the.add(tabBar, BorderLayout.NORTH);
        vung.setOpaque(false);
        vung.add(taoTabCapQuyen(), "0");
        vung.add(taoTabTraCuu(), "1");
        vung.add(taoTabDanhMuc(), "2");
        the.add(vung, BorderLayout.CENTER);
        add(the, BorderLayout.CENTER);

        henGio = new javax.swing.Timer(350, e -> taiDanhSachTk());
        henGio.setRepeats(false);
        oTimTk.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { henGio.restart(); }
        });
    }

    // ======================= TAB 1 =======================
    private JPanel taoTabCapQuyen() {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setOpaque(false);

        // Cột trái: danh sách tài khoản
        JPanel trai = new JPanel(new BorderLayout(0, 10));
        trai.setOpaque(false);
        trai.setPreferredSize(new Dimension(290, 0));
        oTimTk.setPreferredSize(new Dimension(0, 38));
        trai.add(oTimTk, BorderLayout.NORTH);
        dsTk.setFixedCellHeight(58);
        dsTk.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dsTk.setCellRenderer((list, tk, idx, chon, focus) -> {
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
        dsTk.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && dsTk.getSelectedValue() != null) {
                taiQuyenCua(Api.n(dsTk.getSelectedValue(), "id"));
            }
        });
        JScrollPane sp = UI.cuon(dsTk);
        sp.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        trai.add(sp, BorderLayout.CENTER);
        p.add(trai, BorderLayout.WEST);

        // Cột phải: chi tiết quyền
        khungPhai.setOpaque(false);
        hienTrongPhai();
        p.add(khungPhai, BorderLayout.CENTER);
        return p;
    }

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
        JComboBox<String> cbVaiTro = UI.hopChon(VAI_TRO_HIEN, null);
        cbVaiTro.setSelectedIndex(Math.max(0, Arrays.asList(VAI_TRO_MA).indexOf(Api.s(tk, "vaiTro"))));
        cbVaiTro.setPreferredSize(new Dimension(160, 36));
        UI.FlatButton doiVaiTro = UI.nut("Đổi vai trò", Icons.REFRESH, UI.Kieu.VIEN);
        doiVaiTro.addActionListener(e -> {
            String moi = VAI_TRO_MA[cbVaiTro.getSelectedIndex()];
            if (moi.equals(Api.s(tk, "vaiTro"))) {
                HopThoai.loi(this, "Hãy chọn một vai trò khác với vai trò hiện tại");
                return;
            }
            if (!HopThoai.xacNhan(this, "Thay đổi quyền", "Đổi vai trò của <b>" + Api.s(tk, "hoTen") + "</b> từ <b>"
                            + Api.s(tk, "tenVaiTro") + "</b> sang <b>" + cbVaiTro.getSelectedItem()
                            + "</b>?<br>Bộ quyền mặc định sẽ thay đổi theo vai trò mới.",
                    "Thay đổi quyền", UI.Kieu.CHINH)) return;
            Api.chayNen(this, () -> Api.put("/api/admin/phan-quyen/tai-khoan/" + id + "/vai-tro",
                    Api.body("vaiTro", moi)), r -> {
                HopThoai.thanhCong(this, "Thay đổi quyền thành công");
                idChonSan = id;
                taiDanhSachTk();
            });
        });
        JPanel phaiDau = new JPanel(new GridBagLayout());
        phaiDau.setOpaque(false);
        phaiDau.add(UI.hang(8, cbVaiTro, doiVaiTro));
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
            Api.chayNen(this, () -> Api.put("/api/admin/phan-quyen/tai-khoan/" + id + "/quyen", body), r -> {
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
        String url = "/api/admin/tai-khoan?kichThuoc=100&tuKhoa=" + Api.e(oTimTk.getText().trim());
        Api.chayNen(this, () -> Api.get(url), kq -> {
            Long giuLai = idChonSan != null ? idChonSan
                    : dsTk.getSelectedValue() != null ? Api.n(dsTk.getSelectedValue(), "id") : null;
            idChonSan = null;
            modelTk.clear();
            int chon = -1;
            for (JsonElement el : Api.arr(Api.obj(kq), "noiDung")) {
                JsonObject o = el.getAsJsonObject();
                if (giuLai != null && Api.n(o, "id") == giuLai) chon = modelTk.size();
                modelTk.addElement(o);
            }
            if (chon >= 0) {
                dsTk.setSelectedIndex(chon);
                dsTk.ensureIndexIsVisible(chon);
                taiQuyenCua(giuLai);
            } else {
                hienTrongPhai();
            }
        });
    }

    /** Gọi từ màn Quản lý tài khoản: mở sẵn tài khoản cần phân quyền. */
    public void chonSan(long taiKhoanId) {
        idChonSan = taiKhoanId;
        oTimTk.setText("");
        tabBar.chonTab(0);
    }

    // ======================= TAB 2: TRA CỨU QUYỀN =======================
    private JPanel taoTabTraCuu() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setOpaque(false);
        cbQuyen.setPreferredSize(new Dimension(280, 38));
        oTimTraCuu.setPreferredSize(new Dimension(240, 38));
        UI.FlatButton btn = UI.nut("Tra cứu", Icons.SEARCH, UI.Kieu.CHINH);
        btn.addActionListener(e -> traCuu());
        cbQuyen.addActionListener(e -> traCuu());
        oTimTraCuu.addActionListener(e -> traCuu());
        JPanel dau = new JPanel(new BorderLayout(0, 8));
        dau.setOpaque(false);
        dau.add(UI.hang(10, cbQuyen, oTimTraCuu, btn), BorderLayout.NORTH);
        dau.add(lblMacDinh, BorderLayout.SOUTH);
        p.add(dau, BorderLayout.NORTH);

        bangTraCuu.anCot(0);
        bangTraCuu.cotHaiDong(1);
        bangTraCuu.cotNhan(3);
        bangTraCuu.cotNhan(4);
        bangTraCuu.cotNhan(5);
        p.add(bangTraCuu.trongKhungCuon(), BorderLayout.CENTER);
        return p;
    }

    private void traCuu() {
        if (dangNapQuyen) return;
        int i = cbQuyen.getSelectedIndex();
        if (i < 0 || i >= danhMucQuyen.size()) return;
        String ma = Api.s(danhMucQuyen.get(i), "maQuyen");
        String url = "/api/admin/phan-quyen/tra-cuu?maQuyen=" + Api.e(ma) + "&tuKhoa=" + Api.e(oTimTraCuu.getText().trim());
        Api.chayNen(this, () -> Api.get(url), kq -> {
            JsonObject o = Api.obj(kq);
            List<String> vt = new ArrayList<>();
            Api.arr(o, "vaiTroMacDinh").forEach(e -> vt.add(e.getAsString()));
            JsonArray ds = Api.arr(o, "taiKhoan");
            lblMacDinh.setText(ds.isEmpty() ? "Không có dữ liệu: chưa tài khoản nào có quyền này"
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
    private JPanel taoTabDanhMuc() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        bangDanhMuc.doRongCot(0, 140);
        bangDanhMuc.doRongCot(1, 190);
        bangDanhMuc.doRongCot(2, 90);
        bangDanhMuc.doRongCot(3, 300);
        bangDanhMuc.cotNhan(2);
        p.add(bangDanhMuc.trongKhungCuon(), BorderLayout.CENTER);
        return p;
    }

    // ======================= TẢI DỮ LIỆU =======================
    @Override
    public void taiLai() {
        Api.chayNen(this, () -> Api.get("/api/admin/phan-quyen/quyen"), kq -> {
            dangNapQuyen = true;
            danhMucQuyen = new ArrayList<>();
            int chonCu = Math.max(0, cbQuyen.getSelectedIndex());
            cbQuyen.removeAllItems();
            bangDanhMuc.xoaHet();
            for (JsonElement el : Api.arr(kq)) {
                JsonObject q = el.getAsJsonObject();
                danhMucQuyen.add(q);
                cbQuyen.addItem(Api.s(q, "tenQuyen"));
                bangDanhMuc.themDong(Api.s(q, "maQuyen"), Api.s(q, "tenQuyen"), Api.s(q, "nhom"),
                        Api.s(q, "moTa"), Api.s(q, "vaiTroMacDinh"));
            }
            if (cbQuyen.getItemCount() > chonCu) cbQuyen.setSelectedIndex(chonCu);
            dangNapQuyen = false;
            taiDanhSachTk();
            if (tabBar.getChon() == 1) traCuu();
        });
    }
}
