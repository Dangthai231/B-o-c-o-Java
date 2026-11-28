package com.eventmanager.fe.gui.Admin;

import com.eventmanager.fe.api.Api;

import com.eventmanager.fe.gui.Admin.common.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * MÀN HÌNH "GIAO CÔNG VIỆC" của Admin (Admin -> Cán bộ)
 *
 * Admin tạo công việc, chọn cán bộ phụ trách -> lưu bảng cong_viec với nguoiGiao = "admin".
 * Cán bộ mở "Quản lý công việc" sẽ thấy việc đó (tick "Việc giao cho tôi" để chỉ xem việc của mình)
 * và tự cập nhật tiến độ / trạng thái; Admin xem lại tiến độ ở đây.
 */
public class QuanLyCongViecPanel extends TrangAdmin {

    private static final String[] UU_TIEN = {"Trung bình", "Cao", "Thấp"};

    private final BangDep bang = new BangDep("ID", "Mã CV", "Công việc", "Sự kiện", "Cán bộ phụ trách",
            "Hạn chót", "Tiến độ", "Trạng thái", "Thao tác") {
        @Override
        public boolean getScrollableTracksViewportWidth() {
            return getParent() instanceof JViewport viewport && viewport.getWidth() >= getPreferredSize().width;
        }
    };
    private record CanBo(String tenDangNhap, String hienThi) {
        @Override public String toString() { return hienThi; }

        boolean khop(String nhanSu) {
            return hienThi.equalsIgnoreCase(nhanSu) || (!tenDangNhap.isEmpty()
                    && (tenDangNhap.equalsIgnoreCase(nhanSu)
                    || nhanSu.toLowerCase(Locale.ROOT).endsWith(" (" + tenDangNhap.toLowerCase(Locale.ROOT) + ")")));
        }
    }

    private record SuKienChon(long id, String hienThi) {
        @Override public String toString() { return hienThi; }
    }

    private static final CanBo TAT_CA_CAN_BO = new CanBo("", "Tất cả cán bộ");
    private final JComboBox<CanBo> cbLocCanBo = UI.hopChon(new CanBo[]{TAT_CA_CAN_BO}, Icons.USER);
    private final UI.TextFieldDep oTim = UI.oTimKiem("Tìm theo mã, tên công việc...");

    private final List<JsonObject> tatCa = new ArrayList<>();
    private final List<JsonObject> dangHien = new ArrayList<>();
    private final List<CanBo> dsCanBo = new ArrayList<>();
    private final List<SuKienChon> dsSuKien = new ArrayList<>();
    private final UI.FlatButton btnGiao = UI.nut("Giao việc mới", Icons.PLUS, UI.Kieu.CHINH);
    private boolean daTaiDuLieu;
    private int lanTai;

    public QuanLyCongViecPanel(DieuHuong dieuHuong) {
        super(dieuHuong);
        JPanel goc = new JPanel(new BorderLayout(0, 16));
        goc.setOpaque(false);

        // Tiêu đề + nút giao việc
        JPanel dau = new JPanel(new BorderLayout());
        dau.setOpaque(false);
        dau.add(UI.tieuDeTrang("Giao công việc",
                "Giao việc cho cán bộ và theo dõi tiến độ"), BorderLayout.WEST);
        btnGiao.setEnabled(false);
        btnGiao.addActionListener(e -> formCongViec(null));
        UI.FlatButton btnLamMoi = UI.nut("Làm mới", Icons.REFRESH, UI.Kieu.VIEN);
        btnLamMoi.addActionListener(e -> taiLai());
        dau.add(UI.hangPhai(10, btnLamMoi, btnGiao), BorderLayout.EAST);
        goc.add(dau, BorderLayout.NORTH);

        // Thẻ chứa bộ lọc + bảng
        UI.RoundPanel the = UI.card(new BorderLayout(0, 12), 16);
        cbLocCanBo.setPreferredSize(new Dimension(260, 36));
        cbLocCanBo.addActionListener(e -> hienBang());
        oTim.setPreferredSize(new Dimension(280, 36));
        oTim.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { hienBang(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { hienBang(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { hienBang(); }
        });
        the.add(UI.hang(10, cbLocCanBo, oTim), BorderLayout.NORTH);

        bang.setRowHeight(52);
        bang.anCot(0);
        bang.coDinhCot(1, 78);
        bang.cotHaiDong(2);
        bang.doRongCot(2, 240);
        bang.doRongCot(3, 170);
        bang.doRongCot(4, 170);
        bang.coDinhCot(5, 100);
        bang.coDinhCot(6, 80);
        bang.cotNhan(7);
        bang.coDinhCot(8, 110);
        bang.cotThaoTac(8, List.of(
                        new BangDep.NutThaoTac("sua", null, Icons.EDIT, "Sửa / đổi cán bộ"),
                        new BangDep.NutThaoTac("xoa", null, Icons.TRASH, "Xóa công việc")),
                (ma, dong) -> daTaiDuLieu && dong < dangHien.size(),
                (ma, dong) -> {
                    JsonObject cv = dangHien.get(dong);
                    if ("sua".equals(ma)) formCongViec(cv);
                    else xoa(cv);
                });
        the.add(UI.cuon(bang), BorderLayout.CENTER);
        goc.add(the, BorderLayout.CENTER);

        vungNoiDung.add(goc, BorderLayout.CENTER);
    }

    // ======================= TẢI DỮ LIỆU =======================
    @Override
    public void taiLai() {
        int lan = ++lanTai;
        daTaiDuLieu = false;
        btnGiao.setEnabled(false);
        bang.repaint();
        Api.chayNen(this, () -> List.of(Api.get("/api/cong-viec/nhan-su"),
                Api.get("/api/cong-viec/su-kien"), Api.get("/api/cong-viec/danh-sach")), kq -> {
            if (lan != lanTai) return;
            CanBo chon = (CanBo) cbLocCanBo.getSelectedItem();
            dsCanBo.clear();
            var loc = new DefaultComboBoxModel<>(new CanBo[]{TAT_CA_CAN_BO});
            for (JsonElement el : Api.arr(kq.get(0))) {
                JsonObject o = el.getAsJsonObject();
                CanBo cb = new CanBo(Api.s(o, "tenDangNhap"), Api.s(o, "hienThi"));
                dsCanBo.add(cb);
                loc.addElement(cb);
                if (chon != null && cb.tenDangNhap().equals(chon.tenDangNhap())) loc.setSelectedItem(cb);
            }
            cbLocCanBo.setModel(loc);
            dsSuKien.clear();
            for (JsonElement el : Api.arr(kq.get(1))) {
                JsonObject o = el.getAsJsonObject();
                String ma = Api.s(o, "maSuKien");
                String ten = Api.s(o, "tenSuKien");
                dsSuKien.add(new SuKienChon(Api.n(o, "id"), ma.isEmpty() ? ten : ma + " – " + ten));
            }
            tatCa.clear();
            for (JsonElement el : Api.arr(kq.get(2))) tatCa.add(el.getAsJsonObject());
            daTaiDuLieu = true;
            btnGiao.setEnabled(true);
            hienBang();
        });
    }

    private void hienBang() {
        CanBo canBo = (CanBo) cbLocCanBo.getSelectedItem();
        String tu = oTim.getText().trim().toLowerCase(Locale.ROOT);
        dangHien.clear();
        bang.xoaHet();
        for (JsonObject o : tatCa) {
            if (canBo != null && canBo != TAT_CA_CAN_BO && !canBo.khop(Api.s(o, "nhanSu"))) continue;
            String chu = (Api.s(o, "maCv") + " " + Api.s(o, "tenCongViec")).toLowerCase(Locale.ROOT);
            if (!tu.isEmpty() && !chu.contains(tu)) continue;
            dangHien.add(o);
            String nguoiGiao = "admin".equalsIgnoreCase(Api.s(o, "nguoiGiao")) ? "Admin giao" : "Cán bộ tự lập";
            String ns = Api.s(o, "nhanSu");
            String hc = Api.s(o, "hanChot");
            bang.themDong(Api.n(o, "id"), Api.s(o, "maCv"),
                    Api.s(o, "tenCongViec") + "\n" + nguoiGiao + "  |  Ưu tiên: " + Api.s(o, "mucDoUuTien"),
                    Api.s(o, "tenSuKien").isEmpty() ? "—" : Api.s(o, "tenSuKien"),
                    ns.isEmpty() ? "Chưa phân công" : ns,
                    hc.isEmpty() ? "—" : hc,
                    Api.n(o, "tienDo") + "%",
                    Api.s(o, "trangThai"), "");
        }
        hienSoKetQua(dangHien.size());
    }

    // ======================= GIAO / SỬA =======================
    private void formCongViec(JsonObject cv) {
        boolean sua = cv != null;
        HopThoai.FormDialog d = new HopThoai.FormDialog(this, sua ? "Sửa công việc " + Api.s(cv, "maCv") : "Giao việc mới",
                "Cán bộ được chọn sẽ thấy công việc này trong mục Quản lý công việc", 600);

        UI.TextFieldDep tfTen = UI.oNhap("VD: Chuẩn bị hội trường");
        JComboBox<SuKienChon> cbSuKien = UI.hopChon(new SuKienChon[]{new SuKienChon(0, "(Không gắn sự kiện)")}, Icons.CALENDAR);
        dsSuKien.forEach(cbSuKien::addItem);
        JComboBox<CanBo> cbCanBo = UI.hopChon(dsCanBo.toArray(CanBo[]::new), Icons.USER);
        cbCanBo.setSelectedIndex(-1);
        JComboBox<String> cbUuTien = UI.hopChon(UU_TIEN, Icons.LIST);
        UI.TextFieldDep tfHan = UI.oNhap("yyyy-MM-dd, VD: " + LocalDate.now().plusDays(7));
        JTextArea taMoTa = UI.vungChu(4);

        if (sua) {
            tfTen.setText(Api.s(cv, "tenCongViec"));
            long id = Api.n(cv, "suKienId");
            if (id > 0) {
                SuKienChon suKien = dsSuKien.stream().filter(s -> s.id() == id).findFirst().orElse(null);
                if (suKien == null) {
                    suKien = new SuKienChon(id, "Sự kiện không còn trong danh sách (#" + id + ")");
                    cbSuKien.addItem(suKien);
                }
                cbSuKien.setSelectedItem(suKien);
            }
            String nhanSu = Api.s(cv, "nhanSu");
            dsCanBo.stream().filter(cb -> cb.khop(nhanSu)).findFirst().ifPresent(cbCanBo::setSelectedItem);
            if (!Api.s(cv, "mucDoUuTien").isBlank()) cbUuTien.setSelectedItem(Api.s(cv, "mucDoUuTien"));
            tfHan.setText(Api.s(cv, "hanChot"));
            taMoTa.setText(Api.s(cv, "moTa"));
        }

        d.truong("Tên công việc *", tfTen, true);
        d.truong("Cán bộ phụ trách *", cbCanBo, false);
        d.truong("Sự kiện", cbSuKien, false);
        d.truong("Mức ưu tiên", cbUuTien, false);
        d.truong("Hạn chót", tfHan, false);
        d.truong("Mô tả / yêu cầu", UI.cuonVungChu(taMoTa), true);
        JLabel loi = UI.nhan(" ", Theme.SMALL, Theme.DANGER);
        d.khoi(loi);

        UI.FlatButton huy = UI.nut("Hủy", UI.Kieu.VIEN);
        huy.addActionListener(e -> d.dispose());
        UI.FlatButton luu = UI.nut(sua ? "Lưu" : "Giao việc", Icons.SEND, UI.Kieu.CHINH);
        luu.addActionListener(e -> {
            String ten = tfTen.getText().trim();
            String han = tfHan.getText().trim();
            if (ten.isEmpty()) { HopThoai.loi(d, "Vui lòng nhập tên công việc"); return; }
            if (ten.length() > 255) { HopThoai.loi(d, "Tên công việc tối đa 255 ký tự"); return; }
            CanBo canBo = (CanBo) cbCanBo.getSelectedItem();
            if (canBo == null) { HopThoai.loi(d, "Vui lòng chọn cán bộ phụ trách"); return; }
            if (!han.isEmpty()) {
                try { LocalDate.parse(han); } catch (DateTimeParseException ex) {
                    HopThoai.loi(d, "Hạn chót phải có dạng yyyy-MM-dd"); return;
                }
            }
            JsonObject body = new JsonObject();
            body.addProperty("tenCongViec", ten);
            body.addProperty("nhanSu", canBo.hienThi());
            body.addProperty("mucDoUuTien", String.valueOf(cbUuTien.getSelectedItem()));
            body.addProperty("moTa", taMoTa.getText().trim());
            if (!han.isEmpty()) body.addProperty("hanChot", han);
            SuKienChon suKien = (SuKienChon) cbSuKien.getSelectedItem();
            if (suKien != null && suKien.id() > 0) {
                body.addProperty("suKienId", suKien.id());
                if (sua && suKien.id() == Api.n(cv, "suKienId")) {
                    body.addProperty("tenSuKien", Api.s(cv, "tenSuKien"));
                }
            }
            if (sua) {
                // cap-nhat ghi đè toàn bộ -> giữ nguyên mã, trạng thái, tiến độ cán bộ đã cập nhật
                body.addProperty("maCv", Api.s(cv, "maCv"));
                body.addProperty("trangThai", Api.s(cv, "trangThai"));
                body.addProperty("tienDo", Api.n(cv, "tienDo"));
            } else {
                body.addProperty("nguoiGiao", "admin");
            }
            loi.setText(" ");
            luu.setEnabled(false);
            SuKienDialogs.guiForm(d, luu, loi, () -> sua ? Api.put("/api/cong-viec/cap-nhat/" + Api.n(cv, "id"), body)
                    : Api.post("/api/cong-viec/them", body), () -> {
                HopThoai.thanhCong(this, sua ? "Đã lưu công việc" : "Đã giao công việc cho " + canBo.hienThi());
                taiLai();
            });
        });
        d.nut(huy);
        d.nut(luu);
        d.hien();
    }

    private void xoa(JsonObject cv) {
        if (!HopThoai.xacNhan(this, "Xóa công việc", "Xóa công việc <b>" + Api.s(cv, "tenCongViec") + "</b>?",
                "Xóa", UI.Kieu.NGUY_HIEM)) return;
        Api.chayThaoTac(this, () -> Api.delete("/api/cong-viec/xoa/" + Api.n(cv, "id")), r -> {
            HopThoai.thanhCong(this, "Đã xóa công việc");
            taiLai();
        });
    }
}
